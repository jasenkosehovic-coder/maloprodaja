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
import { EMPTY, forkJoin } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { CrudTableComponent } from '../../../shared/components/crud-table/crud-table.component';
import { CrudActionsConfig, CrudFieldConfig, CrudPdfHeader } from '../../../shared/components/crud-table/crud-field-config';
import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PopustiService } from './popusti.service';
import { Popust, CreatePopust, UpdatePopust } from './popusti.models';
import { PopustArtikliListComponent } from './popust-artikli-list.component';

@Component({
  selector: 'app-popusti-list',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, CrudTableComponent, PopustArtikliListComponent],
  templateUrl: './popusti-list.component.html',
  styleUrl: './popusti-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class PopustiListComponent implements OnInit {
  private readonly popustiService = inject(PopustiService);
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

  items: Popust[] = [];
  isLoading = false;
  isSaving = false;

  readonly tableHeaders: string[] = ['naziv', 'procenat', 'datumOd', 'datumDo', 'poslovnicaNaziv', 'aktivan'];

  readonly tableActions: CrudActionsConfig = {
    add: true,
    edit: true,
    delete: true,
    export: true,
  };

  readonly fields: CrudFieldConfig[] = [
    {
      key: 'id',
      label: 'ID',
      type: 'number',
      visible: false,
    },
    {
      key: 'naziv',
      label: 'Naziv',
      type: 'text',
      required: true,
      requiredMessage: 'Naziv je obavezan.',
    },
    {
      key: 'procenat',
      label: 'Procenat (%)',
      type: 'number',
      required: true,
      requiredMessage: 'Procenat je obavezan.',
      min: 0,
      max: 100,
      minMessage: 'Procenat ne može biti manji od 0.',
      maxMessage: 'Procenat ne može biti veći od 100.',
    },
    {
      key: 'datumOd',
      label: 'Datum od',
      required: true,
      requiredMessage: 'Datum je obavezan.',
      type: 'date',
      dateFilterMode: 'gte',
    },
    {
      key: 'datumDo',
      label: 'Datum do',
      required: true,
      requiredMessage: 'Datum je obavezan.',
      type: 'date',
      dateFilterMode: 'lte',
    },
    {
      key: 'poslovnicaNaziv',
      label: 'Poslovnica',
      type: 'text',
      visible: false,
    },
    {
      key: 'aktivan',
      label: 'Aktivan',
      type: 'boolean',
      defaultValue: true,
    },
  ];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.popustiService.getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju popusta.');
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
    const dto: CreatePopust = {
      naziv: row['naziv'],
      procenat: Number(row['procenat']),
      datumOd: row['datumOd'] || undefined,
      datumDo: row['datumDo'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : true,
    };

    this.isSaving = true;
    this.popustiService.create(dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju popusta.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Popust uspješno kreiran.');
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const id: number = row['id'];
    const dto: UpdatePopust = {
      naziv: row['naziv'],
      procenat: Number(row['procenat']),
      datumOd: row['datumOd'] || undefined,
      datumDo: row['datumDo'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : false,
    };

    this.isSaving = true;
    this.popustiService.update(id, dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju popusta.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Popust uspješno ažuriran.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    const id: number = row['id'];

    this.popustiService.deactivate(id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji popusta.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Popust uspješno deaktiviran.');
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (rows.length === 0) return;

    const requests = rows.map(row =>
      this.popustiService.deactivate(row['id']).pipe(
        catchError(err => {
          console.error(`Greška pri deaktivaciji popusta ${row['id']}:`, err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} popust(a) uspješno deaktivirano.`);
        this.loadData();
      });
  }
}
