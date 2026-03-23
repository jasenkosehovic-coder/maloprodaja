import { Routes } from '@angular/router';

export const SIFARNICI_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./sifarnici-shell.component').then((m) => m.SifarniciShellComponent),
    children: [
      {
        path: '',
        redirectTo: 'artikli',
        pathMatch: 'full',
      },
      {
        path: 'artikli',
        loadComponent: () =>
          import('./artikli/artikli-list.component').then((m) => m.ArtikliListComponent),
        title: 'Artikli — Šifarnici',
      },
      {
        path: 'grupe',
        loadComponent: () =>
          import('./grupe/grupe-list.component').then((m) => m.GrupeListComponent),
        title: 'Grupe artikala',
      },
      {
        path: 'dobavljaci',
        loadComponent: () =>
          import('./dobavljaci/dobavljaci-list.component').then((m) => m.DobavljaciListComponent),
        title: 'Dobavljači',
      },
      {
        path: 'kupci',
        loadComponent: () =>
          import('./kupci/kupci-list.component').then((m) => m.KupciListComponent),
        title: 'Kupci',
      },
    ],
  },
];
