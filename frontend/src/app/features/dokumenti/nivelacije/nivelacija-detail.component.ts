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
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyBamPipe } from '../../../shared/pipes/currency-bam.pipe';
import { NivelacijeService } from '../services/nivelacije.service';
import { NivelacijaDetail } from '../models/dokumenti.models';

@Component({
  selector: 'app-nivelacija-detail',
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
  templateUrl: './nivelacija-detail.component.html',
  styleUrl: './nivelacija-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NivelacijaDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly nivelacijeService = inject(NivelacijeService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly nivelacija = signal<NivelacijaDetail | null>(null);
  readonly isLoading = signal(true);

  readonly stavkeColumns = ['sifra', 'naziv', 'kolicina', 'vpc', 'mpcStara', 'mpcNova', 'iznosNivelacije'];

  ngOnInit(): void {
    const idParam = this.route.snapshot.params['id'];
    const id = Number(idParam);
    if (!id || isNaN(id)) {
      this.snackBar.open('Neispravni ID nivelacije.', 'Zatvori', { duration: 3000 });
      void this.router.navigate(['/dokumenti/nivelacije']);
      return;
    }
    this.ucitajNivelaciju(id);
  }

  private ucitajNivelaciju(id: number): void {
    this.isLoading.set(true);
    this.nivelacijeService.findById(id)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.snackBar.open('Greška pri učitavanju nivelacije.', 'Zatvori', { duration: 3000 });
          console.error(err);
          void this.router.navigate(['/dokumenti/nivelacije']);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.nivelacija.set(data);
        this.cdr.markForCheck();
      });
  }

  downloadPdf(): void {
    const n = this.nivelacija();
    if (!n) return;

    this.nivelacijeService.downloadPdf(n.id)
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
        a.download = `nivelacija-${n.broj}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      });
  }

  nazad(): void {
    void this.router.navigate(['/dokumenti/nivelacije']);
  }

  getVrstaClass(vrsta: string): string {
    switch (vrsta) {
      case 'AUTOMATSKA_FAKTURA': return 'chip-blue';
      case 'AUTOMATSKA_OTPREMNICA': return 'chip-purple';
      case 'RUCNA': return 'chip-orange';
      default: return 'chip-default';
    }
  }

  getVrstaLabel(vrsta: string): string {
    switch (vrsta) {
      case 'AUTOMATSKA_FAKTURA': return 'Automatska (Faktura)';
      case 'AUTOMATSKA_OTPREMNICA': return 'Automatska (Otpremnica)';
      case 'RUCNA': return 'Ručna';
      default: return vrsta;
    }
  }

  formatDatum(datum: string | null | undefined): string {
    if (!datum) return '—';
    const d = new Date(datum);
    if (isNaN(d.getTime())) return datum;
    return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`;
  }
}
