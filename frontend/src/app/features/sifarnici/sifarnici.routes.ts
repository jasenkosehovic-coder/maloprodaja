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
          import('./artikli/artikli-shell.component').then((m) => m.ArtikliShellComponent),
        title: 'Artikli — Šifarnici',
        children: [
          {
            path: '',
            redirectTo: 'lista',
            pathMatch: 'full',
          },
          {
            path: 'lista',
            loadComponent: () =>
              import('./artikli/artikli-list.component').then((m) => m.ArtikliListComponent),
            title: 'Artikli — Šifarnici',
          },
          {
            path: 'poslovnica',
            loadComponent: () =>
              import('./artikli/artikli-poslovnica-list.component').then(
                (m) => m.ArtikliPoslovnicaListComponent
              ),
            title: 'Artikli po poslovnici — Šifarnici',
          },
          {
            path: 'barkodovi',
            loadComponent: () =>
              import('./artikli/barkodovi-list.component').then((m) => m.BarkodoviListComponent),
            title: 'Barkodovi — Šifarnici',
          },
          {
            path: 'grupe',
            loadComponent: () =>
              import('./grupe/grupe-list.component').then((m) => m.GrupeListComponent),
            title: 'Grupe artikala — Šifarnici',
          },
          {
            path: 'popusti',
            loadComponent: () =>
              import('./popusti/popusti-list.component').then((m) => m.PopustiListComponent),
            title: 'Popusti — Šifarnici',
          },
          {
            path: 'tipovi-velicina',
            loadComponent: () =>
              import('./tipovi-velicina/tipovi-velicina-list.component').then(
                (m) => m.TipoviVelicinaListComponent
              ),
            title: 'Tipovi veličina — Šifarnici',
          },
          {
            path: 'definicije-atributa',
            loadComponent: () =>
              import('./definicije-atributa/definicije-atributa-list.component').then(
                (m) => m.DefinicijeAtributaListComponent
              ),
            title: 'Definicije atributa — Šifarnici',
          },
          {
            path: 'boje',
            loadComponent: () =>
              import('./boje/boje-list.component').then((m) => m.BojeListComponent),
            title: 'Boje — Šifarnici',
          },
        ],
      },
      {
        path: 'kupci',
        loadComponent: () =>
          import('./kupci/kupci-list.component').then((m) => m.KupciListComponent),
        title: 'Kupci — Šifarnici',
      },
      {
        path: 'dobavljaci',
        loadComponent: () =>
          import('./dobavljaci/dobavljaci-list.component').then((m) => m.DobavljaciListComponent),
        title: 'Dobavljači — Šifarnici',
      },
      {
        path: 'proizvodjaci',
        loadComponent: () =>
          import('./proizvodjaci/proizvodjaci-list.component').then(
            (m) => m.ProizvodjaciListComponent
          ),
        title: 'Proizvođači — Šifarnici',
      },
    ],
  },
];
