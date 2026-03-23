import { Routes } from '@angular/router';

export const DOKUMENTI_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./dokumenti.component').then((m) => m.DokumentiComponent),
  },
];
