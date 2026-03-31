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
import { catchError, filter, finalize, switchMap } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { NotificationService } from '../../../core/services/notification.service';
import { OtpremnicaService } from '../services/otpremnice.service';
import { OtpremnicaListItem } from '../models/dokumenti.models';
import { OtpremnicaFormComponent } from './otpremnica-form.component';

@Component({
  selector: 'app-otpremnice-list',
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
  templateUrl: './otpremnice-list.component.html',
  styleUrl: './otpremnice-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OtpremnicaListComponent implements OnInit {
  private readonly otpremnicaService = inject(OtpremnicaService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly otpremnice = signal<OtpremnicaListItem[]>([]);
  readonly isLoading = signal(false);
  readonly filterStatus = signal<string>('');

  readonly filtrirane = computed(() => {
    const status = this.filterStatus();
    const sve = this.otpremnice();
    return status ? sve.filter(o => o.status === status) : sve;
  });

  readonly displayedColumns = ['broj', 'datum', 'status', 'posiljalac', 'primalac', 'brojStavki', 'akcije'];

  readonly statusOptions = [
    { value: '', label: 'Svi statusi' },
    { value: 'KREIRANA', label: 'Kreirana' },
    { value: 'POSLANA', label: 'Poslana' },
    { value: 'PRIMLJENA', label: 'Primljena' },
    { value: 'STORNIRANA', label: 'Stornirana' },
  ];

  ngOnInit(): void {
    this.ucitajPodatke();
  }

  ucitajPodatke(): void {
    this.isLoading.set(true);
    this.otpremnicaService.list()
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju otpremnica.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.otpremnice.set(data);
        this.cdr.markForCheck();
      });
  }

  otvoriFormu(): void {
    const dialogRef = this.dialog.open(OtpremnicaFormComponent, {
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

  otvoriDetalj(id: number): void {
    void this.router.navigate(['/dokumenti/otpremnice', id]);
  }

  posalji(otpremnica: OtpremnicaListItem): void {
    const dialogData: ConfirmDialogData = {
      title: 'Slanje otpremnice',
      message: `Jeste li sigurni da želite poslati otpremnicu broj "${otpremnica.broj}"?`,
      confirmLabel: 'Pošalji',
      cancelLabel: 'Odustani',
      confirmColor: 'primary',
      icon: 'send',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.otpremnicaService.posalji(otpremnica.id).pipe(
          catchError(err => {
            this.notification.error('Greška pri slanju otpremnice.');
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Otpremnica je uspješno poslana.');
        this.ucitajPodatke();
      });
  }

  potvrdiPrijem(otpremnica: OtpremnicaListItem): void {
    const dialogData: ConfirmDialogData = {
      title: 'Potvrda prijema',
      message: `Jeste li sigurni da želite potvrditi prijem otpremnice broj "${otpremnica.broj}"?`,
      confirmLabel: 'Potvrdi prijem',
      cancelLabel: 'Odustani',
      confirmColor: 'primary',
      icon: 'check_circle',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.otpremnicaService.potvrdiPrijem(otpremnica.id).pipe(
          catchError(err => {
            this.notification.error('Greška pri potvrdi prijema otpremnice.');
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Prijem otpremnice je uspješno potvrđen.');
        this.ucitajPodatke();
      });
  }

  storno(otpremnica: OtpremnicaListItem): void {
    const dialogData: ConfirmDialogData = {
      title: 'Storniranje otpremnice',
      message: `Jeste li sigurni da želite stornirati otpremnicu broj "${otpremnica.broj}"? Ova akcija se ne može poništiti.`,
      confirmLabel: 'Storniraj',
      cancelLabel: 'Odustani',
      confirmColor: 'warn',
      icon: 'cancel',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.otpremnicaService.storno(otpremnica.id).pipe(
          catchError(err => {
            this.notification.error('Greška pri storniranju otpremnice.');
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Otpremnica je uspješno stornirana.');
        this.ucitajPodatke();
      });
  }

  downloadPdf(otpremnica: OtpremnicaListItem): void {
    this.otpremnicaService.downloadPdf(otpremnica.id)
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
        a.download = `otpremnica-${otpremnica.broj}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'KREIRANA': return 'chip-default';
      case 'POSLANA': return 'chip-blue';
      case 'PRIMLJENA': return 'chip-success';
      case 'STORNIRANA': return 'chip-error';
      default: return 'chip-default';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'KREIRANA': return 'Kreirana';
      case 'POSLANA': return 'Poslana';
      case 'PRIMLJENA': return 'Primljena';
      case 'STORNIRANA': return 'Stornirana';
      default: return status;
    }
  }

  formatDatum(datum: string): string {
    if (!datum) return '';
    const d = new Date(datum);
    if (isNaN(d.getTime())) return datum;
    return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`;
  }
}
