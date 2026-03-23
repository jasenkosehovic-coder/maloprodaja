import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-dobavljaci-list',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="page-container">
      <div class="empty-state">
        <mat-icon>local_shipping</mat-icon>
        <h3>Dobavljači</h3>
        <p>Upravljajte listom dobavljača.</p>
        <button mat-raised-button color="primary">
          <mat-icon>add</mat-icon>
          Novi dobavljač
        </button>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DobavljaciListComponent {}
