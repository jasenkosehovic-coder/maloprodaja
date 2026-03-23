import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-fiskalni',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>receipt_long</mat-icon>
        <h3>Fiskalni uređaj</h3>
        <p>Status, dnevni izvještaj, zatvori fiskalnu traku.</p>
        <button mat-raised-button color="primary">
          <mat-icon>refresh</mat-icon>
          Provjeri status
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FiskalniComponent {}
