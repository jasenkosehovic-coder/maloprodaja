import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
} from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AuthService } from '../../core/auth/services/auth.service';
import { Uloga } from '../../core/auth/models/auth.models';

interface NavItem {
  label: string;
  route: string;
  roles: Uloga[];
}

const ALL: Uloga[] = ['SUPER_ADMIN', 'ADMIN', 'MENADZER', 'BLAGAJNIK', 'SKLADISTAR'];
const ADMIN: Uloga[] = ['SUPER_ADMIN', 'ADMIN'];
const ADMIN_MEN: Uloga[] = ['SUPER_ADMIN', 'ADMIN', 'MENADZER'];
const ADMIN_BLAG: Uloga[] = ['SUPER_ADMIN', 'ADMIN', 'BLAGAJNIK'];
const ADMIN_MEN_BLAG: Uloga[] = ['SUPER_ADMIN', 'ADMIN', 'MENADZER', 'BLAGAJNIK'];

const SIFARNICI_ITEMS: NavItem[] = [
  { label: 'Artikli', route: '/sifarnici/artikli', roles: ADMIN_MEN },
  { label: 'Grupe artikala', route: '/sifarnici/grupe-artikala', roles: ADMIN_MEN },
  { label: 'Artikli u poslovnici', route: '/sifarnici/artikli-poslovnica', roles: ALL },
  { label: 'Barkodovi', route: '/sifarnici/barkodovi', roles: ALL },
  { label: 'Proizvođači', route: '/sifarnici/proizvodjaci', roles: ADMIN_MEN },
  { label: 'Dobavljači', route: '/sifarnici/dobavljaci', roles: ADMIN_MEN },
  { label: 'Kupci', route: '/sifarnici/kupci', roles: ALL },
  { label: 'Korisnici', route: '/sifarnici/korisnici', roles: ADMIN },
];

const BLAGAJNA_ITEMS: NavItem[] = [
  { label: 'Blagajna', route: '/blagajna', roles: ADMIN_BLAG },
  { label: 'Izvještaji blagajne', route: '/blagajna/izvjestaji', roles: ADMIN_BLAG },
];

const FISKALNI_ITEMS: NavItem[] = [
  { label: 'Fiskalni printer', route: '/fiskalni/printer', roles: ADMIN_BLAG },
  { label: 'Dnevni izvještaj', route: '/fiskalni/dnevni', roles: ADMIN_BLAG },
  { label: 'Presijek stanja', route: '/fiskalni/presijek', roles: ADMIN_BLAG },
  { label: 'Periodični izvještaj', route: '/fiskalni/periodicni', roles: ADMIN_BLAG },
  { label: 'Test printera', route: '/fiskalni/test', roles: ADMIN_BLAG },
];

const DOKUMENTI_ITEMS: NavItem[] = [
  { label: 'Ulazne fakture', route: '/dokumenti/ulazne-fakture', roles: ADMIN_MEN },
  { label: 'Otpremnica', route: '/dokumenti/otpremnica', roles: ADMIN_MEN },
  { label: 'Nivelacije', route: '/dokumenti/nivelacije', roles: ADMIN_MEN },
];

const IZVJESTAJI_ITEMS: NavItem[] = [
  { label: 'Knjiga blagajni', route: '/izvjestaji/knjiga-blagajni', roles: ADMIN_MEN_BLAG },
  { label: 'Trgovačka knjiga', route: '/izvjestaji/trgovacka-knjiga', roles: ADMIN_MEN },
  { label: 'Stanje zaliha', route: '/izvjestaji/stanje-zaliha', roles: ADMIN_MEN },
  { label: 'Lager lista', route: '/izvjestaji/lager-lista', roles: ADMIN_MEN },
  { label: 'Pregled dokumenata', route: '/izvjestaji/dokumenti', roles: ADMIN_MEN_BLAG },
];

const POSTAVKE_ITEMS: NavItem[] = [
  { label: 'Programski parametri', route: '/postavke/parametri', roles: ADMIN },
];

function filterItems(items: NavItem[], uloga: Uloga): NavItem[] {
  return items.filter((item) => item.roles.includes(uloga));
}

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule,
  ],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopbarComponent {
  private readonly authService = inject(AuthService);

  readonly displayName = this.authService.displayName;
  readonly korisnik = this.authService.korisnik;
  readonly poslovnicaNaziv = computed(() => this.authService.korisnik()?.poslovnicaNaziv ?? '');
  readonly showChat = computed(() => this.authService.isLoggedIn());

  readonly navGroups = computed(() => {
    const uloga = this.authService.currentUloga();
    if (!uloga) {
      return {
        sifarnici: [],
        blagajna: [],
        fiskalni: [],
        dokumenti: [],
        izvjestaji: [],
        postavke: [],
      };
    }
    return {
      sifarnici: filterItems(SIFARNICI_ITEMS, uloga),
      blagajna: filterItems(BLAGAJNA_ITEMS, uloga),
      fiskalni: filterItems(FISKALNI_ITEMS, uloga),
      dokumenti: filterItems(DOKUMENTI_ITEMS, uloga),
      izvjestaji: filterItems(IZVJESTAJI_ITEMS, uloga),
      postavke: filterItems(POSTAVKE_ITEMS, uloga),
    };
  });

  readonly showSifarnici = computed(() => this.navGroups().sifarnici.length > 0);
  readonly showBlagajna = computed(() => this.navGroups().blagajna.length > 0);
  readonly showFiskalni = computed(() => this.navGroups().fiskalni.length > 0);
  readonly showDokumenti = computed(() => this.navGroups().dokumenti.length > 0);
  readonly showIzvjestaji = computed(() => this.navGroups().izvjestaji.length > 0);
  readonly showPostavke = computed(() => this.navGroups().postavke.length > 0);

  onLogout(): void {
    this.authService.logout();
  }
}
