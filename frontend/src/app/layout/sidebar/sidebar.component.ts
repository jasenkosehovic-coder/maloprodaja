import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
} from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AuthService } from '../../core/auth/services/auth.service';
import { Uloga } from '../../core/auth/models/auth.models';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: Uloga[];
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Blagajna',    icon: 'point_of_sale',   route: '/blagajna' },
  { label: 'Dokumenti',   icon: 'description',      route: '/dokumenti' },
  { label: 'Šifarnici',   icon: 'category',         route: '/sifarnici' },
  { label: 'Fiskalni',    icon: 'receipt_long',     route: '/fiskalni',    roles: ['ADMIN', 'SUPER_ADMIN', 'MENADZER'] },
  { label: 'Izvještaji',  icon: 'bar_chart',        route: '/izvjestaji',  roles: ['ADMIN', 'SUPER_ADMIN', 'MENADZER'] },
  { label: 'Chat',        icon: 'chat',             route: '/chat' },
  { label: 'Korisnici',   icon: 'group',            route: '/korisnici',   roles: ['ADMIN', 'SUPER_ADMIN'] },
  { label: 'Postavke',    icon: 'settings',         route: '/postavke',    roles: ['ADMIN', 'SUPER_ADMIN'] },
];

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatListModule, MatIconModule, MatTooltipModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarComponent {
  private readonly authService = inject(AuthService);

  collapsed = input(false);

  visibleItems = computed(() => {
    const uloga = this.authService.currentUloga();
    return NAV_ITEMS.filter(
      (item) =>
        !item.roles || (uloga !== null && item.roles.includes(uloga))
    );
  });
}
