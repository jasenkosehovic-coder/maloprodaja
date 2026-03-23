import { Injectable, OnDestroy, inject, signal } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/auth/services/auth.service';
import { ChatMessage, SendMessageRequest } from '../models/chat.models';

export type ConnectionStatus = 'DISCONNECTED' | 'CONNECTING' | 'CONNECTED' | 'ERROR';

@Injectable()
export class ChatWebSocketService implements OnDestroy {
  private readonly authService = inject(AuthService);

  private client: Client | null = null;
  private subscriptions: StompSubscription[] = [];

  readonly messages = signal<ChatMessage[]>([]);
  readonly connectionStatus = signal<ConnectionStatus>('DISCONNECTED');

  connect(roomId = 'global'): void {
    if (this.client?.active) return;

    this.connectionStatus.set('CONNECTING');

    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      connectHeaders: {
        Authorization: `Bearer ${this.authService.accessToken() ?? ''}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        this.connectionStatus.set('CONNECTED');
        this.subscribeToRoom(roomId);
      },
      onDisconnect: () => {
        this.connectionStatus.set('DISCONNECTED');
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame);
        this.connectionStatus.set('ERROR');
      },
    });

    this.client.activate();
  }

  disconnect(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.subscriptions = [];
    this.client?.deactivate();
    this.connectionStatus.set('DISCONNECTED');
  }

  sendMessage(request: SendMessageRequest, roomId = 'global'): void {
    if (!this.client?.active) return;
    this.client.publish({
      destination: `/app/chat/${roomId}/send`,
      body: JSON.stringify(request),
    });
  }

  private subscribeToRoom(roomId: string): void {
    if (!this.client) return;
    const sub = this.client.subscribe(
      `/topic/chat/${roomId}`,
      (message: IMessage) => {
        try {
          const chatMessage = JSON.parse(message.body) as ChatMessage;
          this.messages.update((msgs) => [...msgs, chatMessage]);
        } catch {
          console.error('Failed to parse chat message');
        }
      }
    );
    this.subscriptions.push(sub);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
