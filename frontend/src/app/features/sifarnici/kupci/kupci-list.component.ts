import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-kupci-list',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>people</mat-icon>
        <h3>Kupci</h3>
        <p>Upravljajte listom kupaca i kartičnim popustima.</p>
        <button mat-raised-button color="primary">
          <mat-icon>add</mat-icon>
          Novi kupac
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KupciListComponent {}
