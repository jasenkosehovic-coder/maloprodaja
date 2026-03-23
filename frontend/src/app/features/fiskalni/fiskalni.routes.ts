import { Routes } from '@angular/router';

export const FISKALNI_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./fiskalni.component').then((m) => m.FiskalniComponent),
  },
];
