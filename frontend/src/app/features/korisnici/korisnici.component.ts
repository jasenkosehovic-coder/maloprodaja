import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-korisnici',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>group</mat-icon>
        <h3>Korisnici</h3>
        <p>Upravljajte korisnicima, ulogama i dozvolama.</p>
        <button mat-raised-button color="primary">
          <mat-icon>person_add</mat-icon>
          Novi korisnik
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KorisniciComponent {}
