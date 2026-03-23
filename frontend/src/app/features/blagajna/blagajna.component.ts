import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-blagajna',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatCardModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>point_of_sale</mat-icon>
        <h3>Blagajna</h3>
        <p>POS terminal — prodaja, storno, posudba i ponude.</p>
        <button mat-raised-button color="primary">
          <mat-icon>add_shopping_cart</mat-icon>
          Nova prodaja
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BlagajnaComponent {}
