import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { exhaustMap, Subject } from 'rxjs';

import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  isSubmitting = signal(false);
  showPassword = signal(false);
  loginError = signal<string | null>(null);

  private readonly submit$ = new Subject<void>();

  form = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(4)]],
  });

  get usernameControl() { return this.form.controls.username; }
  get passwordControl() { return this.form.controls.password; }

  constructor() {
    this.submit$
      .pipe(
        exhaustMap(() => {
          this.isSubmitting.set(true);
          this.loginError.set(null);
          return this.authService.login(this.form.getRawValue());
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.isSubmitting.set(false);
          void this.router.navigate(['/']);
        },
        error: (err) => {
          this.isSubmitting.set(false);
          const message =
            err?.error?.message ?? 'Pogrešno korisničko ime ili lozinka.';
          this.loginError.set(message);
        },
      });
  }

  onSubmit(): void {
    if (this.form.invalid || this.isSubmitting()) return;
    this.submit$.next();
  }

  togglePasswordVisibility(): void {
    this.showPassword.update((v) => !v);
  }
}
