import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { CrudTableComponent } from '../../../shared/components/crud-table/crud-table.component';
import { CrudFieldConfig, CrudActionsConfig } from '../../../shared/components/crud-table/crud-field-config';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { CrudPdfHeader } from '../../../shared/components/crud-table/crud-field-config';
import { FaktureService } from '../services/fakture.service';
import { UlaznaFakturaListItem } from '../models/dokumenti.models';
import { FakturaFormComponent } from './faktura-form.component';

@Component({
  selector: 'app-fakture-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatIconModule,
    MatButtonModule,
    PageHeaderComponent,
    CrudTableComponent,
  ],
  templateUrl: './fakture-list.component.html',
  styleUrl: './fakture-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FaktureListComponent implements OnInit {
  private readonly faktureService = inject(FaktureService);
  private readonly notification = inject(NotificationService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
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

  readonly fakture = signal<UlaznaFakturaListItem[]>([]);
  readonly isLoading = signal(false);

  readonly filterOd = signal<Date | null>(null);
  readonly filterDo = signal<Date | null>(null);

  readonly filtriraneFakture = computed(() => {
    let result = this.fakture();

    const od = this.filterOd();
    const do_ = this.filterDo();

    if (od) {
      const odMs = this.dayStart(od);
      const doMs = do_ ? this.dayEnd(do_) : this.dayEnd(od);
      result = result.filter(f => {
        const d = this.parseDate(f.datum);
        return d !== null && d >= odMs && d <= doMs;
      });
    }

    return result;
  });

  readonly fields: CrudFieldConfig[] = [
    { key: 'id', label: 'R.br.', type: 'number', visible: false },
    { key: 'broj', label: 'Broj fakture', type: 'text', readOnly: true },
    { key: 'datum', label: 'Datum', type: 'date', readOnly: true },
    { key: 'datumValute', label: 'Datum valute', type: 'date', readOnly: true },
    { key: 'nazivDobavljaca', label: 'Dobavljač', type: 'text', readOnly: true },
    {
      key: 'statusFakture', label: 'Status', type: 'select', readOnly: true,
      options: [
        { value: 'NACRT', label: 'Nacrt' },
        { value: 'POTVRDJENO', label: 'Potvrđeno' },
        { value: 'STORNIRANO', label: 'Stornirano' },
      ],
    },
    { key: 'ukupnoBezPdv', label: 'Bez PDV (KM)', type: 'number', readOnly: true },
    { key: 'ukupno', label: 'Ukupno (KM)', type: 'number', readOnly: true },
  ];

  readonly actions: CrudActionsConfig = { add: true, edit: true, delete: true, export: true };

  readonly filterableColumns = ['statusFakture', 'nazivDobavljaca', 'datumValute', 'datum', 'broj'];

  readonly rowStyleClass = (row: UlaznaFakturaListItem) => ({
    'row-potvrdjeno': row.statusFakture === 'POTVRDJENO',
    'row-stornirano': row.statusFakture === 'STORNIRANO',
  });

  ngOnInit(): void {
    this.ucitajPodatke();
  }

  ucitajPodatke(): void {
    this.isLoading.set(true);
    this.faktureService.list()
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju faktura.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.fakture.set([...data].sort((a, b) => b.id - a.id));
        this.cdr.markForCheck();
      });
  }

  onOdChange(date: Date | null): void {
    this.filterOd.set(date);
    if (date && this.filterDo() && this.filterDo()! < date) {
      this.filterDo.set(null);
    }
  }

  resetFilters(): void {
    this.filterOd.set(null);
    this.filterDo.set(null);
  }

  onAddClick(): void {
    const dialogRef = this.dialog.open(FakturaFormComponent, {
      width: '900px',
      maxWidth: '95vw',
      disableClose: true,
      data: { fakturaId: null },
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(saved => {
        if (saved) {
          this.ucitajPodatke();
        }
      });
  }

  onEditClick(row: UlaznaFakturaListItem): void {
    void this.router.navigate(['/dokumenti/fakture', row.id]);
  }

  onDelete(row: UlaznaFakturaListItem): void {
    this.faktureService.findById(row.id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri provjeri fakture.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(detail => {
        if (detail.stavke && detail.stavke.length > 0) {
          this.notification.error(
            `Faktura "${row.broj}" ima ${detail.stavke.length} stavku/stavki i ne može se brisati.`
          );
          return;
        }
        this.faktureService.delete(row.id)
          .pipe(
            catchError(err => {
              this.notification.error('Greška pri brisanju fakture.');
              console.error(err);
              return EMPTY;
            }),
            takeUntilDestroyed(this.destroyRef)
          )
          .subscribe(() => {
            this.notification.success(`Faktura "${row.broj}" je uspješno obrisana.`);
            this.ucitajPodatke();
          });
      });
  }

  private dayStart(d: Date): number {
    return new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  }

  private dayEnd(d: Date): number {
    return new Date(d.getFullYear(), d.getMonth(), d.getDate(), 23, 59, 59, 999).getTime();
  }

  private parseDate(val: string | null | undefined): number | null {
    if (!val) return null;
    const d = new Date(val);
    return isNaN(d.getTime()) ? null : d.getTime();
  }
}
