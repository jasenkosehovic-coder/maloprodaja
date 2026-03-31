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
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { EMPTY } from 'rxjs';
import { catchError, finalize, filter, switchMap } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { NotificationService } from '../../../core/services/notification.service';
import { FaktureService } from '../services/fakture.service';
import { UlaznaFakturaListItem } from '../models/dokumenti.models';
import { FakturaFormComponent } from './faktura-form.component';

@Component({
  selector: 'app-fakture-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    MatTooltipModule,
    PageHeaderComponent,
    LoadingSpinnerComponent,
  ],
  templateUrl: './fakture-list.component.html',
  styleUrl: './fakture-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FaktureListComponent implements OnInit {
  private readonly faktureService = inject(FaktureService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly fakture = signal<UlaznaFakturaListItem[]>([]);
  readonly isLoading = signal(false);
  readonly filterStatus = signal<string>('');

  readonly filtriraneFakture = computed(() => {
    const status = this.filterStatus();
    const sve = this.fakture();
    return status ? sve.filter(f => f.statusFakture === status) : sve;
  });

  readonly displayedColumns = ['broj', 'datum', 'nazivDobavljaca', 'statusFakture', 'ukupno', 'akcije'];

  readonly statusOptions = [
    { value: '', label: 'Svi statusi' },
    { value: 'NACRT', label: 'Nacrt' },
    { value: 'POTVRDJENO', label: 'Potvrđeno' },
    { value: 'STORNIRANO', label: 'Stornirano' },
  ];

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
        this.fakture.set(data);
        this.cdr.markForCheck();
      });
  }

  otvoriFormu(fakturaId?: number): void {
    const dialogRef = this.dialog.open(FakturaFormComponent, {
      width: '900px',
      maxWidth: '95vw',
      disableClose: true,
      data: { fakturaId: fakturaId ?? null },
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(saved => {
        if (saved) {
          this.ucitajPodatke();
        }
      });
  }

  otvoriDetalj(id: number): void {
    void this.router.navigate(['/dokumenti/fakture', id]);
  }

  potvrdi(faktura: UlaznaFakturaListItem): void {
    const dialogData: ConfirmDialogData = {
      title: 'Potvrda fakture',
      message: `Jeste li sigurni da želite potvrditi fakturu broj "${faktura.broj}"?`,
      confirmLabel: 'Potvrdi',
      cancelLabel: 'Odustani',
      confirmColor: 'primary',
      icon: 'check_circle',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.faktureService.potvrdi(faktura.id).pipe(
          catchError(err => {
            this.notification.error('Greška pri potvrđivanju fakture.');
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Faktura je uspješno potvrđena.');
        this.ucitajPodatke();
      });
  }

  storno(faktura: UlaznaFakturaListItem): void {
    const dialogData: ConfirmDialogData = {
      title: 'Storniranje fakture',
      message: `Jeste li sigurni da želite stornirati fakturu broj "${faktura.broj}"? Ova akcija se ne može poništiti.`,
      confirmLabel: 'Storniraj',
      cancelLabel: 'Odustani',
      confirmColor: 'warn',
      icon: 'cancel',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.faktureService.storno(faktura.id).pipe(
          catchError(err => {
            this.notification.error('Greška pri storniranju fakture.');
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Faktura je uspješno stornirana.');
        this.ucitajPodatke();
      });
  }

  downloadPdf(faktura: UlaznaFakturaListItem): void {
    this.faktureService.downloadPdf(faktura.id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri preuzimanju PDF-a.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `faktura-${faktura.broj}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'POTVRDJENO': return 'chip-success';
      case 'STORNIRANO': return 'chip-error';
      default: return 'chip-default';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'NACRT': return 'Nacrt';
      case 'POTVRDJENO': return 'Potvrđeno';
      case 'STORNIRANO': return 'Stornirano';
      default: return status;
    }
  }

  formatBroj(value: number): string {
    return new Intl.NumberFormat('bs-BA', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value);
  }

  formatDatum(datum: string): string {
    if (!datum) return '';
    const d = new Date(datum);
    if (isNaN(d.getTime())) return datum;
    return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`;
  }
}
