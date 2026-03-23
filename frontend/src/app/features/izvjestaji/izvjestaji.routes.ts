import { Routes } from '@angular/router';

export const IZVJESTAJI_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./izvjestaji.component').then((m) => m.IzvjestajiComponent),
  },
];
