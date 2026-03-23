import { Routes } from '@angular/router';

export const KORISNICI_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./korisnici.component').then((m) => m.KorisniciComponent),
  },
];
