import {
  ChangeDetectionStrategy,
  Component,
  inject,
  output,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';

import { AuthService } from '../../core/auth/services/auth.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
  ],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopbarComponent {
  private readonly authService = inject(AuthService);

  menuToggle = output<void>();

  displayName = this.authService.displayName;
  korisnik = this.authService.korisnik;
  poslovnicaNaziv = () => this.authService.korisnik()?.poslovnicaNaziv ?? '';

  onMenuToggle(): void {
    this.menuToggle.emit();
  }

  onLogout(): void {
    this.authService.logout();
  }
}
