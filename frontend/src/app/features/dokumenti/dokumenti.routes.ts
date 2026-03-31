import { Routes } from '@angular/router';

export const DOKUMENTI_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./dokumenti.component').then((m) => m.DokumentiComponent),
    children: [
      {
        path: '',
        redirectTo: 'fakture',
        pathMatch: 'full',
      },
      {
        path: 'fakture',
        loadComponent: () =>
          import('./fakture/fakture-list.component').then((m) => m.FaktureListComponent),
        title: 'Ulazne fakture — Dokumenti',
      },
      {
        path: 'fakture/:id',
        loadComponent: () =>
          import('./fakture/faktura-detail.component').then((m) => m.FakturaDetailComponent),
        title: 'Faktura — Dokumenti',
      },
      {
        path: 'nivelacije',
        loadComponent: () =>
          import('./nivelacije/nivelacije-list.component').then((m) => m.NivelacijeListComponent),
        title: 'Nivelacije — Dokumenti',
      },
      {
        path: 'nivelacije/:id',
        loadComponent: () =>
          import('./nivelacije/nivelacija-detail.component').then((m) => m.NivelacijaDetailComponent),
        title: 'Nivelacija — Dokumenti',
      },
      {
        path: 'otpremnice',
        loadComponent: () =>
          import('./otpremnice/otpremnice-list.component').then((m) => m.OtpremnicaListComponent),
        title: 'Otpremnice — Dokumenti',
      },
      {
        path: 'otpremnice/:id',
        loadComponent: () =>
          import('./otpremnice/otpremnica-detail.component').then((m) => m.OtpremnicaDetailComponent),
        title: 'Otpremnica — Dokumenti',
      },
    ],
  },
];
