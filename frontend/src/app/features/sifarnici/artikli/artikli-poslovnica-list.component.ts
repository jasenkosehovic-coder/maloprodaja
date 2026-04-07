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
import {
  ArtikalKompanija,
  ArtikalPoslovnica,
  CreateArtikalPoslovnica,
  UpdateArtikalPoslovnica,
} from './artikli.models';

@Component({
  selector: 'app-artikli-poslovnica-list',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, CrudTableComponent],
  templateUrl: './artikli-poslovnica-list.component.html',
  styleUrl: './artikli-poslovnica-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class ArtikliPoslovnicaListComponent implements OnInit {
  private readonly artikliService = inject(ArtikliService);
  private readonly authService = inject(AuthService);
  private readonly optionsService = inject(OptionsService);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly pdfHeader = computed<CrudPdfHeader | null>(() => {
    const k = this.authService.korisnik();
    if (!k) return null;
    return {
      kompanijaNaziv: k.kompanijaNaziv,
      kompanijaAdresa: k.kompanijaAdresa,
      kompanijaGrad: k.kompanijaGrad,
      poslovnicaNaziv: k.poslovnicaNaziv,
      poslovnicaAdresa: k.poslovnicaAdresa,
      poslovnicaGrad: k.poslovnicaGrad,
    };
  });

  items: ArtikalPoslovnica[] = [];
  isLoading = false;
  isSaving = false;
  fields: CrudFieldConfig[] = [];

  // Cached for fields rebuild after each data change
  private artikliSvi: ArtikalKompanija[] = [];
  private appOptions: AppOptions | null = null;

  readonly tableHeaders: string[] = [
    'artikalNaziv', 'artikalSifra', 'vpc', 'mpc', 'marza', 'tipMarze', 'popustProcenat', 'ukupnaKolicina', 'aktivan',
  ];

  readonly tableActions: CrudActionsConfig = {
    add: true,
    edit: true,
    delete: true,
    export: true,
  };

  private get idPoslovnice(): number | null {
    return this.authService.korisnik()?.poslovnicaId ?? null;
  }

  ngOnInit(): void {
    forkJoin([
      this.artikliService.getAll().pipe(catchError(() => of([] as ArtikalKompanija[]))),
      this.optionsService.getOptions(),
    ]).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(([artikli, options]) => {
        this.artikliSvi = artikli;
        this.appOptions = options;
        this.loadData();
      });
  }

  loadData(): void {
    const idPosl = this.idPoslovnice;
    if (!idPosl) {
      this.items = [];
      this.rebuildFields();
      return;
    }

    this.isLoading = true;
    this.artikliService.getAllByPoslovnica(idPosl)
      .pipe(
        finalize(() => { this.isLoading = false; this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju artikala u poslovnici.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(data => {
        this.items = data;
        this.rebuildFields();
        this.cdr.markForCheck();
      });
  }

  private rebuildFields(): void {
    if (this.appOptions) {
      this.fields = this.buildFields(this.artikliSvi, this.appOptions);
    }
  }

  private buildFields(artikli: ArtikalKompanija[], options: AppOptions): CrudFieldConfig[] {
    // options = all active articles (needed so edit modal shows the current article name)
    // addOptions = filtered — only articles not yet in this branch (active or inactive)
    const existingIds = new Set(this.items.map(i => i.idArtikla));

    const sviAktivni: CrudFieldOption[] = artikli
      .filter(a => a.aktivan)
      .map(a => ({ label: `${a.sifra} — ${a.naziv}`, value: a.id }));

    const artikliOptions = sviAktivni;

    const artikliAddOptions: CrudFieldOption[] = sviAktivni
      .filter(o => !existingIds.has(o.value));

    return [
      { key: 'id',          label: 'ID',          type: 'number',  visible: false },
      { key: 'artikalNaziv', label: 'Naziv',       type: 'text',   visible: false },
      { key: 'artikalSifra', label: 'Šifra',       type: 'text',   visible: false },
      {
        key: 'idArtikla',
        label: 'Artikal',
        type: 'select',
        required: true,
        requiredMessage: 'Artikal je obavezan.',
        options: artikliOptions,         // all active — used in edit modal for display
        addOptions: artikliAddOptions,   // filtered — used in add modal (excludes existing)
        readOnlyOnEdit: true,
      },
      // vpc and mpc are set via incoming documents — hidden from form, shown in table
      { key: 'vpc', label: 'VPC', type: 'number', visible: false },
      { key: 'mpc', label: 'MPC', type: 'number', visible: false },
      {
        key: 'marza',
        label: 'Marža (%)',
        type: 'number',
        min: 0,
        minMessage: 'Marža ne može biti negativna.',
      },
      {
        key: 'tipMarze',
        label: 'Tip marže',
        type: 'select',
        required: true,
        requiredMessage: 'Tip marže je obavezan.',
        defaultValue: 'SLOBODNA',
        options: options.tipoviMarze,
      },
      {
        key: 'popustProcenat',
        label: 'Popust (%)',
        type: 'number',
        min: 0,
        max: 100,
        minMessage: 'Popust ne može biti negativan.',
        maxMessage: 'Popust ne može biti veći od 100.',
        decimals: 2,
        defaultValue: 0,
      },
      {
        key: 'ukupnaKolicina',
        label: 'Ukupna količina',
        type: 'number',
        visible: false,
        decimals: 2,
        cellClass: (value: number) => value === 0 ? 'cell-kolicina-nula' : '',
      },
      { key: 'aktivan', label: 'Aktivan', type: 'boolean', defaultValue: true },
    ];
  }

  onCreate(row: any): void {
    const idPosl = this.idPoslovnice;
    if (!idPosl) {
      this.notification.error('Korisnik nema dodijeljenu poslovnicu.');
      return;
    }

    const existingIds = new Set(this.items.map(i => i.idArtikla));
    if (existingIds.has(row['idArtikla'])) {
      this.notification.error('Ovaj artikal je već dodan u ovu poslovnicu.');
      return;
    }

    const dto: CreateArtikalPoslovnica = {
      idArtikla: row['idArtikla'],
      idPoslovnice: idPosl,
      marza: row['marza'] != null ? Number(row['marza']) : undefined,
      tipMarze: row['tipMarze'] || 'SLOBODNA',
    };

    this.isSaving = true;
    this.artikliService.createPoslovnica(dto)
      .pipe(
        finalize(() => { this.isSaving = false; this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju artikla u poslovnici.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.notification.success('Artikal u poslovnici uspješno kreiran.');
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const dto: UpdateArtikalPoslovnica = {
      marza: row['marza'] != null ? Number(row['marza']) : undefined,
      tipMarze: row['tipMarze'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : true,
      popustProcenat: row['popustProcenat'] != null ? Number(row['popustProcenat']) : undefined,
    };

    this.isSaving = true;
    this.artikliService.updatePoslovnica(row['id'], dto)
      .pipe(
        finalize(() => { this.isSaving = false; this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju artikla u poslovnici.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.notification.success('Artikal u poslovnici uspješno ažuriran.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    this.artikliService.deactivatePoslovnica(row['id'])
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.notification.success('Artikal u poslovnici uspješno deaktiviran.');
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (!rows.length) return;
    const requests = rows.map(r =>
      this.artikliService.deactivatePoslovnica(r['id']).pipe(catchError(() => EMPTY))
    );
    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} artikal(a) uspješno deaktivirano.`);
        this.loadData();
      });
  }
}
