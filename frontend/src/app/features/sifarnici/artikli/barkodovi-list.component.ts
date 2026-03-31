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
import { PoslovnicaService, PoslovnicaOption } from '../../korisnici/poslovnica.service';
import { ArtikliService } from './artikli.service';
import { ArtikalKompanija, Barkod, CreateBarkod, UpdateBarkod } from './artikli.models';

@Component({
  selector: 'app-barkodovi-list',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, CrudTableComponent],
  templateUrl: './barkodovi-list.component.html',
  styleUrl: './barkodovi-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class BarkodoviListComponent implements OnInit {
  private readonly artikliService = inject(ArtikliService);
  private readonly authService = inject(AuthService);
  private readonly poslovnicaService = inject(PoslovnicaService);
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
      poslovnicaNaziv: k.poslovnicaId ? k.poslovnicaNaziv : undefined,
      poslovnicaAdresa: k.poslovnicaId ? k.poslovnicaAdresa : undefined,
      poslovnicaGrad: k.poslovnicaId ? k.poslovnicaGrad : undefined,
    };
  });

  items: Barkod[] = [];
  isLoading = false;
  isSaving = false;
  fields: CrudFieldConfig[] = [];

  private artikliSvi: ArtikalKompanija[] = [];
  private poslovnice: PoslovnicaOption[] = [];

  readonly tableHeaders: string[] = [
    'artikalNaziv', 'artikalSifra', 'barkod', 'poslovnicaNaziv', 'aktivan',
  ];

  readonly tableActions: CrudActionsConfig = {
    add: true,
    edit: true,
    delete: true,
    export: true,
  };

  private get hasPoslovnica(): boolean {
    return !!this.authService.korisnik()?.poslovnicaId;
  }

  ngOnInit(): void {
    const artikli$ = this.artikliService.getAll().pipe(catchError(() => of([] as ArtikalKompanija[])));
    const poslovnice$ = this.poslovnicaService.getAll().pipe(catchError(() => of([] as PoslovnicaOption[])));

    forkJoin({ artikli: artikli$, poslovnice: poslovnice$ })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ artikli, poslovnice }) => {
        this.artikliSvi = artikli;
        this.poslovnice = poslovnice;
        this.fields = this.buildFields();
        this.loadData();
      });
  }

  private buildFields(): CrudFieldConfig[] {
    const artikliOptions: CrudFieldOption[] = this.artikliSvi
      .filter(a => a.aktivan)
      .map(a => ({ label: `${a.sifra} — ${a.naziv}`, value: a.id }));

    const poslovnicaField: CrudFieldConfig = {
      key: 'idPoslovnice',
      label: 'Poslovnica',
      type: 'select',
      defaultValue: null,
      hideEmptyOption: true,
      options: [
        { label: 'Sve poslovnice', value: null },
        ...this.poslovnice.map(p => ({ label: p.naziv, value: p.id })),
      ],
    };

    return [
      { key: 'id',              label: 'ID',         type: 'number', visible: false },
      { key: 'artikalNaziv',    label: 'Naziv',       type: 'text',   visible: false },
      { key: 'artikalSifra',    label: 'Šifra',       type: 'text',   visible: false },
      { key: 'poslovnicaNaziv', label: 'Poslovnica',  type: 'text',   visible: false },
      {
        key: 'idArtikla',
        label: 'Artikal',
        type: 'select',
        required: true,
        requiredMessage: 'Artikal je obavezan.',
        options: artikliOptions,
        readOnlyOnEdit: true,
      },
      {
        key: 'barkod',
        label: 'Barkod',
        type: 'text',
        required: true,
        requiredMessage: 'Barkod je obavezan.',
      },
      poslovnicaField,
      { key: 'aktivan', label: 'Aktivan', type: 'boolean', defaultValue: true },
    ];
  }

  loadData(): void {
    this.isLoading = true;
    this.artikliService.getAllBarkodovi()
      .pipe(
        finalize(() => { this.isLoading = false; this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju barkodova.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(data => {
        this.items = data;
        this.cdr.markForCheck();
      });
  }

  onCreate(row: any): void {
    const dto: CreateBarkod = {
      barkod: row['barkod'],
      idArtikla: row['idArtikla'],
      idPoslovnice: row['idPoslovnice'] ?? null,
    };

    this.isSaving = true;
    this.artikliService.createBarkod(dto)
      .pipe(
        finalize(() => { this.isSaving = false; this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju barkoda.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        const scope = dto.idPoslovnice ? 'za odabranu poslovnicu' : 'za sve poslovnice';
        this.notification.success(`Barkod uspješno kreiran ${scope}.`);
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const dto: UpdateBarkod = {
      barkod: row['barkod'],
      idPoslovnice: row['idPoslovnice'] ?? null,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : true,
    };

    this.isSaving = true;
    this.artikliService.updateBarkod(row['id'], dto)
      .pipe(
        finalize(() => { this.isSaving = false; this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju barkoda.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.notification.success('Barkod uspješno ažuriran.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    this.artikliService.deactivateBarkod(row['id'])
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji barkoda.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.notification.success('Barkod uspješno deaktiviran.');
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (!rows.length) return;
    const requests = rows.map(r =>
      this.artikliService.deactivateBarkod(r['id']).pipe(catchError(() => EMPTY))
    );
    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} barkod(a) uspješno deaktivirano.`);
        this.loadData();
      });
  }
}
