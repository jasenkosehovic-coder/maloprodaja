import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-artikli-list',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>inventory_2</mat-icon>
        <h3>Artikli</h3>
        <p>Upravljajte artiklima, cijenama i barkodovima.</p>
        <button mat-raised-button color="primary">
          <mat-icon>add</mat-icon>
          Novi artikal
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtikliListComponent {}
