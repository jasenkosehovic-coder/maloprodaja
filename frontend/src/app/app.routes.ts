import { Routes } from '@angular/router';
import { authGuard } from './core/auth/guards/auth.guard';
import { roleGuard } from './core/auth/guards/role.guard';

export const APP_ROUTES: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent
      ),
    title: 'Prijava — Maloprodaja',
  },
  {
    path: '',
    loadComponent: () =>
      import('./layout/main-layout/main-layout.component').then(
        (m) => m.MainLayoutComponent
      ),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'blagajna',
        pathMatch: 'full',
      },
      {
        path: 'sifarnici',
        loadChildren: () =>
          import('./features/sifarnici/sifarnici.routes').then(
            (m) => m.SIFARNICI_ROUTES
          ),
        title: 'Šifarnici — Maloprodaja',
      },
      {
        path: 'blagajna',
        loadChildren: () =>
          import('./features/blagajna/blagajna.routes').then(
            (m) => m.BLAGAJNA_ROUTES
          ),
        title: 'Blagajna — Maloprodaja',
      },
      {
        path: 'dokumenti',
        loadChildren: () =>
          import('./features/dokumenti/dokumenti.routes').then(
            (m) => m.DOKUMENTI_ROUTES
          ),
        title: 'Dokumenti — Maloprodaja',
      },
      {
        path: 'fiskalni',
        loadChildren: () =>
          import('./features/fiskalni/fiskalni.routes').then(
            (m) => m.FISKALNI_ROUTES
          ),
        canActivate: [roleGuard('SUPER_ADMIN', 'ADMIN', 'MENADZER')],
        title: 'Fiskalni uređaj — Maloprodaja',
      },
      {
        path: 'izvjestaji',
        loadChildren: () =>
          import('./features/izvjestaji/izvjestaji.routes').then(
            (m) => m.IZVJESTAJI_ROUTES
          ),
        canActivate: [roleGuard('SUPER_ADMIN', 'ADMIN', 'MENADZER')],
        title: 'Izvještaji — Maloprodaja',
      },
      {
        path: 'chat',
        loadChildren: () =>
          import('./features/chat/chat.routes').then(
            (m) => m.CHAT_ROUTES
          ),
        title: 'Chat — Maloprodaja',
      },
      {
        path: 'korisnici',
        loadChildren: () =>
          import('./features/korisnici/korisnici.routes').then(
            (m) => m.KORISNICI_ROUTES
          ),
        canActivate: [roleGuard('SUPER_ADMIN', 'ADMIN')],
        title: 'Korisnici — Maloprodaja',
      },
      {
        path: 'postavke',
        loadChildren: () =>
          import('./features/postavke/postavke.routes').then(
            (m) => m.POSTAVKE_ROUTES
          ),
        canActivate: [roleGuard('SUPER_ADMIN', 'ADMIN')],
        title: 'Postavke — Maloprodaja',
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
