import {
  AfterViewChecked,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DatePipe } from '@angular/common';

import { ChatWebSocketService } from './services/chat-websocket.service';
import { AuthService } from '../../core/auth/services/auth.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    DatePipe,
  ],
  providers: [ChatWebSocketService],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  private readonly wsService = inject(ChatWebSocketService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(NonNullableFormBuilder);

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef<HTMLDivElement>;

  messages = this.wsService.messages;
  connectionStatus = this.wsService.connectionStatus;

  isConnected = computed(() => this.connectionStatus() === 'CONNECTED');
  currentUsername = computed(() => this.authService.korisnik()?.username ?? '');

  messageForm = this.fb.group({
    sadrzaj: ['', [Validators.required, Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    this.wsService.connect('global');
  }

  private previousMessageCount = 0;

  ngAfterViewChecked(): void {
    const current = this.messages().length;
    if (current !== this.previousMessageCount) {
      this.previousMessageCount = current;
      this.scrollToBottom();
    }
  }

  ngOnDestroy(): void {
    this.wsService.disconnect();
  }

  onSendMessage(): void {
    const value = this.messageForm.getRawValue();
    if (this.messageForm.invalid || !this.isConnected()) return;

    this.wsService.sendMessage({ sadrzaj: value.sadrzaj });
    this.messageForm.reset();
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSendMessage();
    }
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const el = this.messagesContainer.nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }
}
