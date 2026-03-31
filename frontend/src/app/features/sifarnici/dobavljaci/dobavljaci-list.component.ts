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
import { DobavljaciService } from './dobavljaci.service';
import { Dobavljac, CreateDobavljac, UpdateDobavljac } from './dobavljaci.models';

@Component({
  selector: 'app-dobavljaci-list',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, CrudTableComponent],
  templateUrl: './dobavljaci-list.component.html',
  styleUrl: './dobavljaci-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class DobavljaciListComponent implements OnInit {
  private readonly dobavljaciService = inject(DobavljaciService);
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

  items: Dobavljac[] = [];
  isLoading = false;
  isSaving = false;

  readonly tableHeaders: string[] = ['naziv', 'adresa', 'grad', 'telefon', 'email', 'pib', 'aktivan'];

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
      key: 'adresa',
      label: 'Adresa',
      type: 'text',
    },
    {
      key: 'grad',
      label: 'Grad',
      type: 'text',
    },
    {
      key: 'telefon',
      label: 'Telefon',
      type: 'text',
    },
    {
      key: 'email',
      label: 'E-mail',
      type: 'email',
      pattern: '^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$',
      patternMessage: 'Unesite ispravnu email adresu.',
    },
    {
      key: 'pib',
      label: 'PIB',
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
    this.dobavljaciService.getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju dobavljača.');
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
    const dto: CreateDobavljac = {
      naziv: row['naziv'],
      adresa: row['adresa'] || undefined,
      grad: row['grad'] || undefined,
      telefon: row['telefon'] || undefined,
      email: row['email'] || undefined,
      pib: row['pib'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : true,
    };

    this.isSaving = true;
    this.dobavljaciService.create(dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju dobavljača.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Dobavljač uspješno kreiran.');
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const id: number = row['id'];
    const dto: UpdateDobavljac = {
      naziv: row['naziv'],
      adresa: row['adresa'] || undefined,
      grad: row['grad'] || undefined,
      telefon: row['telefon'] || undefined,
      email: row['email'] || undefined,
      pib: row['pib'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : false,
    };

    this.isSaving = true;
    this.dobavljaciService.update(id, dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju dobavljača.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Dobavljač uspješno ažuriran.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    const id: number = row['id'];

    this.dobavljaciService.deactivate(id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji dobavljača.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Dobavljač uspješno deaktiviran.');
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (rows.length === 0) return;

    const requests = rows.map(row =>
      this.dobavljaciService.deactivate(row['id']).pipe(
        catchError(err => {
          console.error(`Greška pri deaktivaciji dobavljača ${row['id']}:`, err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} dobavljač(a) uspješno deaktivirano.`);
        this.loadData();
      });
  }
}
