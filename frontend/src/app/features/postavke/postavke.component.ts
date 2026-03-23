import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-postavke',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>settings</mat-icon>
        <h3>Postavke</h3>
        <p>Konfiguracija aplikacije, poslovnice i fiskalnog uređaja.</p>
        <button mat-raised-button color="primary">
          <mat-icon>tune</mat-icon>
          Uredi postavke
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PostavkeComponent {}
