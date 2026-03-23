import { Routes } from '@angular/router';

export const BLAGAJNA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./blagajna.component').then((m) => m.BlagajnaComponent),
  },
];
