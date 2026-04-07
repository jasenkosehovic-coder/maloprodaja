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
import { MatSelectModule } from '@angular/material/select';
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
import { PrometService } from '../services/promet.service';
import { DokumentListItem, DokumentStatus } from '../models/promet.models';
import { IzlaznaFakturaFormComponent } from './izlazna-faktura-form.component';

@Component({
  selector: 'app-izlazne-fakture-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatSelectModule,
    MatIconModule,
    MatButtonModule,
    PageHeaderComponent,
    CrudTableComponent,
  ],
  templateUrl: './izlazne-fakture-list.component.html',
  styleUrl: './izlazne-fakture-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IzlazneFaktureListComponent implements OnInit {
  private readonly prometService = inject(PrometService);
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

  readonly dokumenti = signal<DokumentListItem[]>([]);
  readonly isLoading = signal(false);

  readonly filterStatus = signal<DokumentStatus | null>(null);
  readonly filterOd = signal<Date | null>(null);
  readonly filterDo = signal<Date | null>(null);

  readonly statusOptions: { value: DokumentStatus | null; label: string }[] = [
    { value: null, label: 'Svi statusi' },
    { value: 'NACRT', label: 'Nacrt' },
    { value: 'POTVRĐEN', label: 'Potvrđen' },
    { value: 'STORNIRAN', label: 'Storniran' },
  ];

  readonly filtrirani = computed(() => {
    let result = this.dokumenti();

    const od = this.filterOd();
    const do_ = this.filterDo();

    if (od) {
      const odMs = this.dayStart(od);
      const doMs = do_ ? this.dayEnd(do_) : this.dayEnd(od);
      result = result.filter(d => {
        const dt = this.parseDate(d.datum);
        return dt !== null && dt >= odMs && dt <= doMs;
      });
    }

    return result;
  });

  readonly fields: CrudFieldConfig[] = [
    { key: 'id', label: 'R.br.', type: 'number', visible: false },
    { key: 'brojDokumenta', label: 'Broj', type: 'text', readOnly: true },
    { key: 'datum', label: 'Datum', type: 'date', readOnly: true },
    { key: 'kupacNaziv', label: 'Kupac', type: 'text', readOnly: true },
    { key: 'poslovnicaNaziv', label: 'Poslovnica', type: 'text', readOnly: true },
    {
      key: 'status', label: 'Status', type: 'select', readOnly: true,
      options: [
        { value: 'NACRT', label: 'Nacrt' },
        { value: 'POTVRĐEN', label: 'Potvrđen' },
        { value: 'STORNIRAN', label: 'Storniran' },
      ],
    },
    { key: 'ukupno', label: 'Ukupno (KM)', type: 'number', readOnly: true },
  ];

  readonly actions: CrudActionsConfig = { add: true, edit: true, delete: false, export: true };

  readonly filterableColumns = ['status', 'kupacNaziv', 'datum', 'brojDokumenta', 'poslovnicaNaziv'];

  readonly rowStyleClass = (row: DokumentListItem) => ({
    'row-potvrdjeno': row.status === 'POTVRĐEN',
    'row-stornirano': row.status === 'STORNIRAN',
  });

  ngOnInit(): void {
    this.ucitajPodatke();
  }

  ucitajPodatke(): void {
    this.isLoading.set(true);
    this.prometService.list({ tipKod: 'IF' })
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju izlaznih faktura.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.dokumenti.set([...data].sort((a, b) => b.id - a.id));
        this.cdr.markForCheck();
      });
  }

  onStatusChange(status: DokumentStatus | null): void {
    this.filterStatus.set(status);
  }

  onOdChange(date: Date | null): void {
    this.filterOd.set(date);
    if (date && this.filterDo() && this.filterDo()! < date) {
      this.filterDo.set(null);
    }
  }

  resetFilters(): void {
    this.filterStatus.set(null);
    this.filterOd.set(null);
    this.filterDo.set(null);
  }

  onAddClick(): void {
    const dialogRef = this.dialog.open(IzlaznaFakturaFormComponent, {
      width: '900px',
      maxWidth: '95vw',
      disableClose: true,
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(saved => {
        if (saved) {
          this.ucitajPodatke();
        }
      });
  }

  onEditClick(row: DokumentListItem): void {
    void this.router.navigate(['/dokumenti/izlazne-fakture', row.id]);
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
