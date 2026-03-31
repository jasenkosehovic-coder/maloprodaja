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
import { GrupeService } from './grupe.service';
import { GrupaArtikala, CreateGrupaArtikala, UpdateGrupaArtikala } from './grupe.models';

@Component({
  selector: 'app-grupe-list',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, CrudTableComponent],
  templateUrl: './grupe-list.component.html',
  styleUrl: './grupe-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class GrupeListComponent implements OnInit {
  private readonly grupeService = inject(GrupeService);
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

  items: GrupaArtikala[] = [];
  isLoading = false;
  isSaving = false;

  readonly tableHeaders: string[] = ['naziv', 'opis', 'aktivan'];

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

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.grupeService.getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju grupa artikala.');
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
    const dto: CreateGrupaArtikala = {
      naziv: row['naziv'],
      opis: row['opis'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : true,
    };

    this.isSaving = true;
    this.grupeService.create(dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju grupe artikala.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Grupa artikala uspješno kreirana.');
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const id: number = row['id'];
    const dto: UpdateGrupaArtikala = {
      naziv: row['naziv'],
      opis: row['opis'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : false,
    };

    this.isSaving = true;
    this.grupeService.update(id, dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju grupe artikala.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Grupa artikala uspješno ažurirana.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    const id: number = row['id'];

    this.grupeService.deactivate(id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji grupe artikala.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Grupa artikala uspješno deaktivirana.');
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (rows.length === 0) return;

    const requests = rows.map(row =>
      this.grupeService.deactivate(row['id']).pipe(
        catchError(err => {
          console.error(`Greška pri deaktivaciji grupe ${row['id']}:`, err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} grupa(e) uspješno deaktivirano.`);
        this.loadData();
      });
  }
}
