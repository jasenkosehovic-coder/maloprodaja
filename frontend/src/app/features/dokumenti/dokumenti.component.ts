import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-dokumenti',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>description</mat-icon>
        <h3>Dokumenti</h3>
        <p>Ulazne fakture, nivelacije cijena i otpremnice.</p>
        <button mat-raised-button color="primary">
          <mat-icon>add</mat-icon>
          Novi dokument
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DokumentiComponent {}
