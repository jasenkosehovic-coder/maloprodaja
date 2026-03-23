import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-izvjestaji',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>bar_chart</mat-icon>
        <h3>Izvještaji</h3>
        <p>Prometni izvještaji, zalihe, prodaja po artiklima i kasirima.</p>
        <button mat-raised-button color="primary">
          <mat-icon>assessment</mat-icon>
          Generiši izvještaj
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IzvjestajiComponent {}
