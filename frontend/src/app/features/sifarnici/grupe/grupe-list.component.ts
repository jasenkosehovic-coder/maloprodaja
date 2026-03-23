import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-grupe-list',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>category</mat-icon>
        <h3>Grupe artikala</h3>
        <p>Organizujte artikle u grupe i podgrupe.</p>
        <button mat-raised-button color="primary">
          <mat-icon>add</mat-icon>
          Nova grupa
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GrupeListComponent {}
