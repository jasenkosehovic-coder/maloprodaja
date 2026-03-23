import { Routes } from '@angular/router';

export const POSTAVKE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./postavke.component').then((m) => m.PostavkeComponent),
  },
];
