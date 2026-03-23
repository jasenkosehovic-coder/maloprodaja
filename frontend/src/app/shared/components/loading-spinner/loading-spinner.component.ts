import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [MatProgressSpinnerModule],
  template: `
    <div class="spinner-wrapper" [style.min-height]="minHeight()">
      <mat-progress-spinner
        mode="indeterminate"
        [diameter]="diameter()"
        aria-label="Učitavanje"
        role="status"
      ></mat-progress-spinner>
      @if (message()) {
        <p class="spinner-message">{{ message() }}</p>
      }
    </div>
  `,
  styles: [`
    .spinner-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
    }
    .spinner-message {
      font-size: 14px;
      color: #616161;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadingSpinnerComponent {
  diameter = input(48);
  message = input('Učitavanje...');
  minHeight = input('200px');
}
