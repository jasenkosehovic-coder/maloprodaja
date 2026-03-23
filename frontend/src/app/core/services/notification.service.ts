import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string, duration = 3000): void {
    this.snackBar.open(message, 'Zatvori', {
      duration,
      panelClass: ['snack-success'],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }

  error(message: string, duration = 5000): void {
    this.snackBar.open(message, 'Zatvori', {
      duration,
      panelClass: ['snack-error'],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }

  info(message: string, duration = 3000): void {
    this.snackBar.open(message, 'Zatvori', {
      duration,
      panelClass: ['snack-info'],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }

  warn(message: string, duration = 4000): void {
    this.snackBar.open(message, 'Zatvori', {
      duration,
      panelClass: ['snack-warn'],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }
}
