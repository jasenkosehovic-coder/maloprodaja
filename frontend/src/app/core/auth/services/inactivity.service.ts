import { Injectable, effect, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Subscription, fromEvent, merge } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { environment } from '../../../../environments/environment';
import { AuthService } from './auth.service';

const INACTIVITY_TIMEOUT_MS: number =
  (environment as { inactivityTimeoutMs?: number }).inactivityTimeoutMs ?? 3600000;

const ACTIVITY_EVENTS = [
  'mousemove',
  'mousedown',
  'keydown',
  'touchstart',
  'scroll',
] as const;

@Injectable({ providedIn: 'root' })
export class InactivityService {
  private readonly authService = inject(AuthService);
  private readonly document = inject(DOCUMENT);

  private activitySubscription: Subscription | null = null;
  private timeoutHandle: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    effect(() => {
      if (this.authService.isLoggedIn()) {
        this.startMonitoring();
      } else {
        this.stopMonitoring();
      }
    });
  }

  startMonitoring(): void {
    if (this.activitySubscription) {
      return;
    }

    this.resetTimeout();

    const activityStreams = ACTIVITY_EVENTS.map((eventName) =>
      fromEvent(this.document, eventName)
    );

    this.activitySubscription = merge(...activityStreams)
      .pipe(debounceTime(300))
      .subscribe(() => this.resetTimeout());
  }

  stopMonitoring(): void {
    if (this.timeoutHandle !== null) {
      clearTimeout(this.timeoutHandle);
      this.timeoutHandle = null;
    }

    if (this.activitySubscription) {
      this.activitySubscription.unsubscribe();
      this.activitySubscription = null;
    }
  }

  private resetTimeout(): void {
    if (this.timeoutHandle !== null) {
      clearTimeout(this.timeoutHandle);
    }

    this.timeoutHandle = setTimeout(() => {
      this.stopMonitoring();
      this.authService.logout();
    }, INACTIVITY_TIMEOUT_MS);
  }
}
