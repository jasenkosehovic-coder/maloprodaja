import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { EMPTY } from 'rxjs';
import { catchError, filter, finalize, switchMap } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyBamPipe } from '../../../shared/pipes/currency-bam.pipe';
import { OtpremnicaService } from '../services/otpremnice.service';
import { OtpremnicaDetail } from '../models/dokumenti.models';

@Component({
  selector: 'app-otpremnica-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatCardModule,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    CurrencyBamPipe,
  ],
  templateUrl: './otpremnica-detail.component.html',
  styleUrl: './otpremnica-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OtpremnicaDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly otpremnicaService = inject(OtpremnicaService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly otpremnica = signal<OtpremnicaDetail | null>(null);
  readonly isLoading = signal(true);

  readonly stavkeColumns = ['sifra', 'naziv', 'kolicina', 'vpc', 'mpc'];

  ngOnInit(): void {
    const idParam = this.route.snapshot.params['id'];
    const id = Number(idParam);
    if (!id || isNaN(id)) {
      this.snackBar.open('Neispravni ID otpremnice.', 'Zatvori', { duration: 3000 });
      void this.router.navigate(['/dokumenti/otpremnice']);
      return;
    }
    this.ucitajOtpremnicu(id);
  }

  private ucitajOtpremnicu(id: number): void {
    this.isLoading.set(true);
    this.otpremnicaService.findById(id)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.snackBar.open('Greška pri učitavanju otpremnice.', 'Zatvori', { duration: 3000 });
          console.error(err);
          void this.router.navigate(['/dokumenti/otpremnice']);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.otpremnica.set(data);
        this.cdr.markForCheck();
      });
  }

  posalji(): void {
    const o = this.otpremnica();
    if (!o) return;

    const dialogData: ConfirmDialogData = {
      title: 'Slanje otpremnice',
      message: `Jeste li sigurni da želite poslati otpremnicu broj "${o.broj}"?`,
      confirmLabel: 'Pošalji',
      cancelLabel: 'Odustani',
      confirmColor: 'primary',
      icon: 'send',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.otpremnicaService.posalji(o.id).pipe(
          catchError(err => {
            const msg = err?.error?.message ?? 'Greška pri slanju otpremnice.';
            this.snackBar.open(msg, 'Zatvori', { duration: 5000 });
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.snackBar.open('Otpremnica je uspješno poslana.', 'Zatvori', { duration: 3000 });
        this.ucitajOtpremnicu(o.id);
      });
  }

  potvrdiPrijem(): void {
    const o = this.otpremnica();
    if (!o) return;

    const dialogData: ConfirmDialogData = {
      title: 'Potvrda prijema',
      message: `Jeste li sigurni da želite potvrditi prijem otpremnice broj "${o.broj}"?`,
      confirmLabel: 'Potvrdi prijem',
      cancelLabel: 'Odustani',
      confirmColor: 'primary',
      icon: 'check_circle',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.otpremnicaService.potvrdiPrijem(o.id).pipe(
          catchError(err => {
            const msg = err?.error?.message ?? 'Greška pri potvrdi prijema otpremnice.';
            this.snackBar.open(msg, 'Zatvori', { duration: 5000 });
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.snackBar.open('Prijem otpremnice je uspješno potvrđen.', 'Zatvori', { duration: 3000 });
        this.ucitajOtpremnicu(o.id);
      });
  }

  storno(): void {
    const o = this.otpremnica();
    if (!o) return;

    const dialogData: ConfirmDialogData = {
      title: 'Storniranje otpremnice',
      message: `Jeste li sigurni da želite stornirati otpremnicu broj "${o.broj}"? Ova akcija se ne može poništiti.`,
      confirmLabel: 'Storniraj',
      cancelLabel: 'Odustani',
      confirmColor: 'warn',
      icon: 'cancel',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.otpremnicaService.storno(o.id).pipe(
          catchError(err => {
            const msg = err?.error?.message ?? 'Greška pri storniranju otpremnice.';
            this.snackBar.open(msg, 'Zatvori', { duration: 5000 });
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.snackBar.open('Otpremnica je uspješno stornirana.', 'Zatvori', { duration: 3000 });
        this.ucitajOtpremnicu(o.id);
      });
  }

  downloadPdf(): void {
    const o = this.otpremnica();
    if (!o) return;

    this.otpremnicaService.downloadPdf(o.id)
      .pipe(
        catchError(err => {
          this.snackBar.open('Greška pri preuzimanju PDF-a.', 'Zatvori', { duration: 3000 });
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `otpremnica-${o.broj}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      });
  }

  nazad(): void {
    void this.router.navigate(['/dokumenti/otpremnice']);
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

  formatDatum(datum: string | null | undefined): string {
    if (!datum) return '—';
    const d = new Date(datum);
    if (isNaN(d.getTime())) return datum;
    return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`;
  }
}
