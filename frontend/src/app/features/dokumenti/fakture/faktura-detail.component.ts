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
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EMPTY } from 'rxjs';
import { catchError, filter, finalize, switchMap } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyBamPipe } from '../../../shared/pipes/currency-bam.pipe';
import { FaktureService } from '../services/fakture.service';
import { UlaznaFakturaDetail } from '../models/dokumenti.models';

@Component({
  selector: 'app-faktura-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatCardModule,
    MatDividerModule,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    CurrencyBamPipe,
  ],
  templateUrl: './faktura-detail.component.html',
  styleUrl: './faktura-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FakturaDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly faktureService = inject(FaktureService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly faktura = signal<UlaznaFakturaDetail | null>(null);
  readonly isLoading = signal(true);

  readonly stavkeColumns = ['redniBroj', 'sifra', 'naziv', 'kolicina', 'vpc', 'pdvStopa', 'iznosPdv', 'ukupno'];

  ngOnInit(): void {
    const idParam = this.route.snapshot.params['id'];
    const id = Number(idParam);
    if (!id || isNaN(id)) {
      this.snackBar.open('Neispravni ID fakture.', 'Zatvori', { duration: 3000 });
      void this.router.navigate(['/dokumenti/fakture']);
      return;
    }
    this.ucitajFakturu(id);
  }

  private ucitajFakturu(id: number): void {
    this.isLoading.set(true);
    this.faktureService.findById(id)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.snackBar.open('Greška pri učitavanju fakture.', 'Zatvori', { duration: 3000 });
          console.error(err);
          void this.router.navigate(['/dokumenti/fakture']);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.faktura.set(data);
        this.cdr.markForCheck();
      });
  }

  potvrdi(): void {
    const f = this.faktura();
    if (!f) return;

    const dialogData: ConfirmDialogData = {
      title: 'Potvrda fakture',
      message: `Jeste li sigurni da želite potvrditi fakturu broj "${f.broj}"?`,
      confirmLabel: 'Potvrdi',
      cancelLabel: 'Odustani',
      confirmColor: 'primary',
      icon: 'check_circle',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.faktureService.potvrdi(f.id).pipe(
          catchError(err => {
            this.snackBar.open('Greška pri potvrđivanju fakture.', 'Zatvori', { duration: 3000 });
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.snackBar.open('Faktura je uspješno potvrđena.', 'Zatvori', { duration: 3000 });
        this.ucitajFakturu(f.id);
      });
  }

  storno(): void {
    const f = this.faktura();
    if (!f) return;

    const dialogData: ConfirmDialogData = {
      title: 'Storniranje fakture',
      message: `Jeste li sigurni da želite stornirati fakturu broj "${f.broj}"? Ova akcija se ne može poništiti.`,
      confirmLabel: 'Storniraj',
      cancelLabel: 'Odustani',
      confirmColor: 'warn',
      icon: 'cancel',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.faktureService.storno(f.id).pipe(
          catchError(err => {
            this.snackBar.open('Greška pri storniranju fakture.', 'Zatvori', { duration: 3000 });
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.snackBar.open('Faktura je uspješno stornirana.', 'Zatvori', { duration: 3000 });
        this.ucitajFakturu(f.id);
      });
  }

  downloadPdf(): void {
    const f = this.faktura();
    if (!f) return;

    this.faktureService.downloadPdf(f.id)
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
        a.download = `faktura-${f.broj}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      });
  }

  nazad(): void {
    void this.router.navigate(['/dokumenti/fakture']);
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

  formatDatum(datum: string | null | undefined): string {
    if (!datum) return '—';
    const d = new Date(datum);
    if (isNaN(d.getTime())) return datum;
    return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`;
  }
}
