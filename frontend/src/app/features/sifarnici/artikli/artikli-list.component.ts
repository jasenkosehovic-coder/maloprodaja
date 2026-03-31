import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EMPTY, forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { CrudTableComponent } from '../../../shared/components/crud-table/crud-table.component';
import { CrudActionsConfig, CrudFieldConfig, CrudFieldOption, CrudPdfHeader } from '../../../shared/components/crud-table/crud-field-config';
import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { OptionsService, AppOptions } from '../../../core/services/options.service';
import { ArtikliService } from './artikli.service';
import { GrupeService } from '../grupe/grupe.service';
import { ProizvodjaciService } from '../proizvodjaci/proizvodjaci.service';
import { DobavljaciService } from '../dobavljaci/dobavljaci.service';
import { ArtikalKompanija, CreateArtikalKompanija, UpdateArtikalKompanija } from './artikli.models';
import { GrupaArtikala } from '../grupe/grupe.models';
import { Proizvodjac } from '../proizvodjaci/proizvodjaci.models';
import { Dobavljac } from '../dobavljaci/dobavljaci.models';

@Component({
  selector: 'app-artikli-list',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, CrudTableComponent],
  templateUrl: './artikli-list.component.html',
  styleUrl: './artikli-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class ArtikliListComponent implements OnInit {
  private readonly artikliService = inject(ArtikliService);
  private readonly grupeService = inject(GrupeService);
  private readonly proizvodjaciService = inject(ProizvodjaciService);
  private readonly dobavljaciService = inject(DobavljaciService);
  private readonly optionsService = inject(OptionsService);
  private readonly notification = inject(NotificationService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly pdfHeader = computed<CrudPdfHeader | null>(() => {
    const k = this.authService.korisnik();
    if (!k) return null;
    return {
      kompanijaNaziv: k.kompanijaNaziv,
      kompanijaAdresa: k.kompanijaAdresa,
      kompanijaGrad: k.kompanijaGrad,
      poslovnicaNaziv: k.poslovnicaId ? k.poslovnicaNaziv : undefined,
      poslovnicaAdresa: k.poslovnicaId ? k.poslovnicaAdresa : undefined,
      poslovnicaGrad: k.poslovnicaId ? k.poslovnicaGrad : undefined,
    };
  });

  items: ArtikalKompanija[] = [];
  isLoading = false;
  isSaving = false;
  fields: CrudFieldConfig[] = [];

  readonly tableHeaders: string[] = [
    'sifra', 'naziv', 'jedin', 'pdv',
    'nazivGrupe', 'nazivProizvodjaca', 'nazivDobavljaca', 'aktivan',
  ];

  readonly tableActions: CrudActionsConfig = {
    add: true,
    edit: true,
    delete: true,
    export: true,
  };

  ngOnInit(): void {
    forkJoin([
      this.grupeService.getAll().pipe(catchError(() => of([] as GrupaArtikala[]))),
      this.proizvodjaciService.getAll().pipe(catchError(() => of([] as Proizvodjac[]))),
      this.dobavljaciService.getAll().pipe(catchError(() => of([] as Dobavljac[]))),
      this.optionsService.getOptions(),
    ]).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(([grupe, proizvodjaci, dobavljaci, options]) => {
        this.fields = this.buildFields(grupe, proizvodjaci, dobavljaci, options);
        this.loadData();
      });
  }

  private buildFields(
    grupe: GrupaArtikala[],
    proizvodjaci: Proizvodjac[],
    dobavljaci: Dobavljac[],
    options: AppOptions,
  ): CrudFieldConfig[] {
    const grupeOptions: CrudFieldOption[] = grupe
      .filter(g => g.aktivan)
      .map(g => ({ label: g.naziv, value: g.id }));

    const proizvodjaciOptions: CrudFieldOption[] = proizvodjaci
      .filter(p => p.aktivan)
      .map(p => ({ label: p.naziv, value: p.id }));

    const dobavljaciOptions: CrudFieldOption[] = dobavljaci
      .filter(d => d.aktivan)
      .map(d => ({ label: d.naziv, value: d.id }));

    return [
      {
        key: 'id',
        label: 'ID',
        type: 'number',
        visible: false,
      },
      {
        key: 'sifra',
        label: 'Šifra',
        type: 'text',
        required: true,
        requiredMessage: 'Šifra je obavezna.',
      },
      {
        key: 'naziv',
        label: 'Naziv',
        type: 'text',
        required: true,
        requiredMessage: 'Naziv je obavezan.',
      },
      {
        key: 'jedin',
        label: 'Jedinica mjere',
        type: 'select',
        defaultValue: 'KOM',
        options: options.jediniceMjere,
      },
      {
        key: 'pdv',
        label: 'PDV (%)',
        type: 'number',
        min: 0,
        minMessage: 'PDV ne može biti negativan.',
        defaultValue: 17,
      },
      {
        key: 'nazivGrupe',
        label: 'Grupa',
        type: 'text',
        visible: false,
      },
      {
        key: 'idGrupe',
        label: 'Grupa',
        type: 'select',
        options: grupeOptions,
      },
      {
        key: 'nazivProizvodjaca',
        label: 'Proizvođač',
        type: 'text',
        visible: false,
      },
      {
        key: 'idProizvodjaca',
        label: 'Proizvođač',
        type: 'select',
        options: proizvodjaciOptions,
      },
      {
        key: 'nazivDobavljaca',
        label: 'Dobavljač',
        type: 'text',
        visible: false,
      },
      {
        key: 'idDobavljaca',
        label: 'Dobavljač',
        type: 'select',
        options: dobavljaciOptions,
      },
      {
        key: 'opis',
        label: 'Opis',
        type: 'text',
      },
      {
        key: 'aktivan',
        label: 'Aktivan',
        type: 'boolean',
        defaultValue: true,
      },
    ];
  }

  loadData(): void {
    this.isLoading = true;
    this.artikliService.getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju artikala.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.items = data;
        this.cdr.markForCheck();
      });
  }

  onCreate(row: any): void {
    const dto: CreateArtikalKompanija = {
      sifra: row['sifra'],
      naziv: row['naziv'],
      jedin: row['jedin'] || 'KOM',
      pdv: Number(row['pdv']) || 17,
      opis: row['opis'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : true,
      idGrupe: row['idGrupe'] || undefined,
      idProizvodjaca: row['idProizvodjaca'] || undefined,
      idDobavljaca: row['idDobavljaca'] || undefined,
    };

    this.isSaving = true;
    this.artikliService.create(dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju artikla.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Artikal uspješno kreiran.');
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const id: number = row['id'];

    const dto: UpdateArtikalKompanija = {
      sifra: row['sifra'],
      naziv: row['naziv'],
      jedin: row['jedin'] || 'KOM',
      pdv: Number(row['pdv']) || 17,
      opis: row['opis'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : false,
      idGrupe: row['idGrupe'] || undefined,
      idProizvodjaca: row['idProizvodjaca'] || undefined,
      idDobavljaca: row['idDobavljaca'] || undefined,
    };

    this.isSaving = true;
    this.artikliService.update(id, dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju artikla.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Artikal uspješno ažuriran.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    const id: number = row['id'];

    this.artikliService.deactivate(id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji artikla.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Artikal uspješno deaktiviran.');
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (rows.length === 0) return;

    const requests = rows.map(row =>
      this.artikliService.deactivate(row['id']).pipe(
        catchError(err => {
          console.error(`Greška pri deaktivaciji artikla ${row['id']}:`, err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} artikal(a) uspješno deaktivirano.`);
        this.loadData();
      });
  }
}
