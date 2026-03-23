import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmColor?: 'primary' | 'accent' | 'warn';
  icon?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div mat-dialog-title class="dialog-title" id="confirm-dialog-title">
      @if (data.icon) {
        <mat-icon [class]="'dialog-icon ' + (data.confirmColor ?? 'warn')">
          {{ data.icon }}
        </mat-icon>
      }
      {{ data.title }}
    </div>

    <mat-dialog-content aria-describedby="confirm-dialog-description">
      <p id="confirm-dialog-description">{{ data.message }}</p>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" cdkFocusInitial>
        {{ data.cancelLabel ?? 'Odustani' }}
      </button>
      <button
        mat-raised-button
        [color]="data.confirmColor ?? 'warn'"
        (click)="onConfirm()"
      >
        {{ data.confirmLabel ?? 'Potvrdi' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .dialog-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;

      &.warn { color: #f44336; }
      &.primary { color: #3f51b5; }
      &.accent { color: #00bcd4; }
    }

    mat-dialog-content p {
      color: #616161;
      margin: 0;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDialogComponent {
  readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
