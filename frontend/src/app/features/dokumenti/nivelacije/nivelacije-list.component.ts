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
import { catchError, finalize } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { NotificationService } from '../../../core/services/notification.service';
import { NivelacijeService } from '../services/nivelacije.service';
import { NivelacijaListItem } from '../models/dokumenti.models';
import { NivelacijaRucnaFormComponent } from './nivelacija-rucna-form.component';

@Component({
  selector: 'app-nivelacije-list',
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
  templateUrl: './nivelacije-list.component.html',
  styleUrl: './nivelacije-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NivelacijeListComponent implements OnInit {
  private readonly nivelacijeService = inject(NivelacijeService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly nivelacije = signal<NivelacijaListItem[]>([]);
  readonly isLoading = signal(false);
  readonly filterVrsta = signal<string>('');

  readonly filtrirane = computed(() => {
    const vrsta = this.filterVrsta();
    const sve = this.nivelacije();
    return vrsta ? sve.filter(n => n.vrsta === vrsta) : sve;
  });

  readonly displayedColumns = ['broj', 'datum', 'vrsta', 'idFakture', 'idOtpremnice', 'brojStavki', 'akcije'];

  readonly vrstaOptions = [
    { value: '', label: 'Sve' },
    { value: 'AUTOMATSKA_FAKTURA', label: 'Automatska (Faktura)' },
    { value: 'AUTOMATSKA_OTPREMNICA', label: 'Automatska (Otpremnica)' },
    { value: 'RUCNA', label: 'Ručna' },
  ];

  ngOnInit(): void {
    this.ucitajPodatke();
  }

  ucitajPodatke(): void {
    this.isLoading.set(true);
    this.nivelacijeService.list()
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju nivelacija.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.nivelacije.set(data);
        this.cdr.markForCheck();
      });
  }

  otvoriDetalj(id: number): void {
    void this.router.navigate(['/dokumenti/nivelacije', id]);
  }

  otvoriRucnuFormu(): void {
    const dialogRef = this.dialog.open(NivelacijaRucnaFormComponent, {
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
      case 'AUTOMATSKA_FAKTURA': return 'Aut. Faktura';
      case 'AUTOMATSKA_OTPREMNICA': return 'Aut. Otpremnica';
      case 'RUCNA': return 'Ručna';
      default: return vrsta;
    }
  }

  formatDatum(datum: string): string {
    if (!datum) return '';
    const d = new Date(datum);
    if (isNaN(d.getTime())) return datum;
    return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`;
  }
}
