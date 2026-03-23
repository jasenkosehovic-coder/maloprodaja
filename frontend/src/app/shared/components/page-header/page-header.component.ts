import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [MatButtonModule, MatIconModule],
  template: `
    <header class="page-header">
      <div class="header-left">
        @if (icon()) {
          <mat-icon class="header-icon">{{ icon() }}</mat-icon>
        }
        <div class="header-text">
          <h1 class="header-title">{{ title() }}</h1>
          @if (subtitle()) {
            <p class="header-subtitle">{{ subtitle() }}</p>
          }
        </div>
      </div>
      <div class="header-actions">
        <ng-content />
      </div>
    </header>
  `,
  styles: [`
    @use '../../../../../styles/variables' as vars;

    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 24px;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .header-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #303f9f;
    }

    .header-title {
      font-size: 20px;
      font-weight: 600;
      color: #212121;
      margin: 0;
    }

    .header-subtitle {
      font-size: 13px;
      color: #616161;
      margin: 2px 0 0;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageHeaderComponent {
  title = input.required<string>();
  subtitle = input('');
  icon = input('');
}
