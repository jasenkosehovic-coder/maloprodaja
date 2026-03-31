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
import { ProizvodjaciService } from './proizvodjaci.service';
import { Proizvodjac, CreateProizvodjac, UpdateProizvodjac } from './proizvodjaci.models';

@Component({
  selector: 'app-proizvodjaci-list',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, CrudTableComponent],
  templateUrl: './proizvodjaci-list.component.html',
  styleUrl: './proizvodjaci-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class ProizvodjaciListComponent implements OnInit {
  private readonly proizvodjaciService = inject(ProizvodjaciService);
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

  items: Proizvodjac[] = [];
  isLoading = false;
  isSaving = false;

  readonly tableHeaders: string[] = ['naziv', 'drzava', 'kontaktOsoba', 'telefon', 'email', 'aktivan'];

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
      key: 'drzava',
      label: 'Država',
      type: 'text',
    },
    {
      key: 'kontaktOsoba',
      label: 'Kontakt osoba',
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
    this.proizvodjaciService.getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju proizvođača.');
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
    const dto: CreateProizvodjac = {
      naziv: row['naziv'],
      drzava: row['drzava'] || undefined,
      kontaktOsoba: row['kontaktOsoba'] || undefined,
      telefon: row['telefon'] || undefined,
      email: row['email'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : true,
    };

    this.isSaving = true;
    this.proizvodjaciService.create(dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju proizvođača.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Proizvođač uspješno kreiran.');
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const id: number = row['id'];
    const dto: UpdateProizvodjac = {
      naziv: row['naziv'],
      drzava: row['drzava'] || undefined,
      kontaktOsoba: row['kontaktOsoba'] || undefined,
      telefon: row['telefon'] || undefined,
      email: row['email'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : false,
    };

    this.isSaving = true;
    this.proizvodjaciService.update(id, dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju proizvođača.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Proizvođač uspješno ažuriran.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    const id: number = row['id'];

    this.proizvodjaciService.deactivate(id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji proizvođača.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Proizvođač uspješno deaktiviran.');
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (rows.length === 0) return;

    const requests = rows.map(row =>
      this.proizvodjaciService.deactivate(row['id']).pipe(
        catchError(err => {
          console.error(`Greška pri deaktivaciji proizvođača ${row['id']}:`, err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} proizvođač(a) uspješno deaktivirano.`);
        this.loadData();
      });
  }
}
