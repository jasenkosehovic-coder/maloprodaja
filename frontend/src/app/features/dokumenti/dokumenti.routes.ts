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

      // ===== Ulazne fakture (UF) =====
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
        title: 'Ulazna faktura — Dokumenti',
      },

      // ===== Povrat dobavljaču (PD) =====
      {
        path: 'povrat-dobavljacu',
        loadComponent: () =>
          import('./povrat-dobavljacu/povrat-dobavljacu-list.component').then(
            (m) => m.PovratDobavljacuListComponent
          ),
        title: 'Povrat dobavljaču — Dokumenti',
      },
      {
        path: 'povrat-dobavljacu/:id',
        loadComponent: () =>
          import('./povrat-dobavljacu/povrat-dobavljacu-detail.component').then(
            (m) => m.PovratDobavljacuDetailComponent
          ),
        title: 'Povrat dobavljaču — Dokumenti',
      },

      // ===== Izlazne fakture / Veleprodaja (IF) =====
      {
        path: 'izlazne-fakture',
        loadComponent: () =>
          import('./izlazne-fakture/izlazne-fakture-list.component').then(
            (m) => m.IzlazneFaktureListComponent
          ),
        title: 'Izlazne fakture — Dokumenti',
      },
      {
        path: 'izlazne-fakture/:id',
        loadComponent: () =>
          import('./izlazne-fakture/izlazna-faktura-detail.component').then(
            (m) => m.IzlaznaFakturaDetailComponent
          ),
        title: 'Izlazna faktura — Dokumenti',
      },

      // ===== Međuskladišnica (MSI/MSU) =====
      {
        path: 'medjuskladisnica',
        loadComponent: () =>
          import('./medjuskladisnica/medjuskladisnica-list.component').then(
            (m) => m.MedjuskladisnicaListComponent
          ),
        title: 'Međuskladišnica — Dokumenti',
      },

      // ===== Nivelacije (unchanged) =====
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
    ],
  },
];
