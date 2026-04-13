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
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EMPTY } from 'rxjs';
import { catchError, filter, finalize, switchMap } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyBamPipe } from '../../../shared/pipes/currency-bam.pipe';
import { AuthService } from '../../../core/auth/services/auth.service';
import { PrometService } from '../services/promet.service';
import { CreateStavkaDTO, DokumentDetail } from '../models/promet.models';
import { ArtikliService } from '../../sifarnici/artikli/artikli.service';
import { ArtikalKompanija, ArtikalVarijanta } from '../../sifarnici/artikli/artikli.models';

@Component({
  selector: 'app-faktura-detail',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatCardModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    CurrencyBamPipe,
  ],
  templateUrl: './faktura-detail.component.html',
  styleUrl: './faktura-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FakturaDetailComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly prometService = inject(PrometService);
  private readonly artikliService = inject(ArtikliService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly dokument = signal<DokumentDetail | null>(null);
  readonly isLoading = signal(true);
  readonly isAddingStavka = signal(false);
  readonly isSavingStavka = signal(false);

  readonly artikliKompanija = signal<ArtikalKompanija[]>([]);
  readonly varijante = signal<ArtikalVarijanta[]>([]);

  readonly stavkeColumns = ['redniBroj', 'naziv', 'velicina', 'boja', 'kolicina', 'cijena', 'popust', 'ukupno'];
  readonly stavkeNacrtColumns = ['redniBroj', 'naziv', 'velicina', 'boja', 'kolicina', 'cijena', 'popust', 'ukupno', 'ukloni'];

  readonly novStavkaForm = this.fb.group({
    idArtikla: this.fb.control<number | null>(null, Validators.required),
    idVarijante: this.fb.control<number | null>(null, Validators.required),
    kolicina: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.001)]),
    cijena: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    popust: this.fb.control<number | null>(null, [Validators.min(0), Validators.max(100)]),
  });

  readonly artikalPretragaTekst = signal('');

  readonly filteredArtikliKompanija = computed(() => {
    const tekst = this.artikalPretragaTekst().toLowerCase().trim();
    const lista = this.artikliKompanija();
    if (!tekst) return lista;
    return lista.filter(a =>
      a.naziv.toLowerCase().includes(tekst) || a.sifra.toLowerCase().includes(tekst)
    );
  });

  readonly varijanteDrugogArtikla = computed(() => {
    const idArtikla = this.novStavkaForm.controls.idArtikla.value;
    if (!idArtikla) return [];
    return this.varijante().filter(v => v.idArtikla === idArtikla && v.aktivan);
  });

  readonly ukupnoStavki = computed(() => {
    const d = this.dokument();
    if (!d) return 0;
    return d.stavke.reduce((sum, s) => sum + s.ukupno, 0);
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.params['id'];
    const id = Number(idParam);
    if (!id || isNaN(id)) {
      this.snackBar.open('Neispravni ID fakture.', 'Zatvori', { duration: 3000 });
      void this.router.navigate(['/dokumenti/fakture']);
      return;
    }
    this.ucitajDokument(id);

    this.novStavkaForm.controls.idArtikla.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(idArtikla => {
        this.novStavkaForm.controls.idVarijante.setValue(null);
        this.varijante.set([]);
        if (idArtikla != null) {
          this.ucitajVarijante(idArtikla);
        }
        this.cdr.markForCheck();
      });
  }

  private ucitajDokument(id: number): void {
    this.isLoading.set(true);
    this.prometService.findById(id)
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
        this.dokument.set(data);
        if (data.status === 'NACRT') {
          this.ucitajArtikle();
        }
        this.cdr.markForCheck();
      });
  }

  private ucitajArtikle(): void {
    this.artikliService.getAll()
      .pipe(
        catchError(() => []),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(artikli => {
        this.artikliKompanija.set(
          (artikli as ArtikalKompanija[]).filter(a => a.aktivan)
        );
        this.cdr.markForCheck();
      });
  }

  ucitajVarijante(idArtikla: number): void {
    this.artikliService.getVarijante(idArtikla)
      .pipe(
        catchError(() => []),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(varijante => {
        this.varijante.set(varijante as ArtikalVarijanta[]);
        this.cdr.markForCheck();
      });
  }

  dodajStavku(): void {
    if (this.novStavkaForm.invalid) {
      this.novStavkaForm.markAllAsTouched();
      return;
    }

    const d = this.dokument();
    if (!d) return;

    const formValue = this.novStavkaForm.getRawValue();
    const dto: CreateStavkaDTO = {
      idVarijante: formValue.idVarijante as number,
      kolicina: formValue.kolicina as number,
      cijena: formValue.cijena as number,
      popust: formValue.popust ?? 0,
    };

    this.isSavingStavka.set(true);
    this.prometService.addStavka(d.id, dto)
      .pipe(
        finalize(() => {
          this.isSavingStavka.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          const msg = err?.error?.message ?? 'Greška pri dodavanju stavke.';
          this.snackBar.open(msg, 'Zatvori', { duration: 5000 });
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(updated => {
        this.dokument.set(updated);
        this.novStavkaForm.reset();
        this.artikalPretragaTekst.set('');
        this.isAddingStavka.set(false);
        this.snackBar.open('Stavka je uspješno dodana.', 'Zatvori', { duration: 2000 });
        this.cdr.markForCheck();
      });
  }

  ukloniStavku(stavkaId: number): void {
    const d = this.dokument();
    if (!d) return;

    const stavka = d.stavke.find(s => s.id === stavkaId);
    const dialogData: ConfirmDialogData = {
      title: 'Uklanjanje stavke',
      message: `Jeste li sigurni da želite ukloniti stavku "${stavka?.artikalNaziv ?? ''}"?`,
      confirmLabel: 'Ukloni',
      cancelLabel: 'Odustani',
      confirmColor: 'warn',
      icon: 'delete',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.prometService.deleteStavka(stavkaId).pipe(
          catchError(err => {
            const msg = err?.error?.message ?? 'Greška pri uklanjanju stavke.';
            this.snackBar.open(msg, 'Zatvori', { duration: 5000 });
            console.error(err);
            return EMPTY;
          })
        )),
        switchMap(() => this.prometService.findById(d.id)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(updated => {
        this.dokument.set(updated);
        this.snackBar.open('Stavka je uspješno uklonjena.', 'Zatvori', { duration: 2000 });
        this.cdr.markForCheck();
      });
  }

  potvrdi(): void {
    const d = this.dokument();
    if (!d) return;

    const dialogData: ConfirmDialogData = {
      title: 'Potvrda fakture',
      message: `Jeste li sigurni da želite potvrditi ovu fakturu?`,
      confirmLabel: 'Potvrdi',
      cancelLabel: 'Odustani',
      confirmColor: 'primary',
      icon: 'check_circle',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.prometService.potvrdi(d.id).pipe(
          catchError(err => {
            const msg = err?.error?.message ?? 'Greška pri potvrđivanju fakture.';
            this.snackBar.open(msg, 'Zatvori', { duration: 5000 });
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.snackBar.open('Faktura je uspješno potvrđena.', 'Zatvori', { duration: 3000 });
        this.ucitajDokument(d.id);
      });
  }

  storno(): void {
    const d = this.dokument();
    if (!d) return;

    const dialogData: ConfirmDialogData = {
      title: 'Storniranje fakture',
      message: `Jeste li sigurni da želite stornirati ovu fakturu? Ova akcija se ne može poništiti.`,
      confirmLabel: 'Storniraj',
      cancelLabel: 'Odustani',
      confirmColor: 'warn',
      icon: 'cancel',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.prometService.storniraj(d.id).pipe(
          catchError(err => {
            const msg = err?.error?.message ?? 'Greška pri storniranju fakture.';
            this.snackBar.open(msg, 'Zatvori', { duration: 5000 });
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.snackBar.open('Faktura je uspješno stornirana.', 'Zatvori', { duration: 3000 });
        this.ucitajDokument(d.id);
      });
  }

  downloadPdf(): void {
    const d = this.dokument();
    if (!d) return;

    this.prometService.downloadPdf(d.id)
      .pipe(
        catchError(err => {
          this.snackBar.open('Greška pri preuzimanju PDF-a.', 'Zatvori', { duration: 3000 });
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(blob => {
        const poslovnicaId = this.authService.korisnik()?.poslovnicaId;
        const naziv = poslovnicaId
          ? `Ulazna_faktura_${poslovnicaId}_${d.id}`
          : `Ulazna_faktura_${d.id}`;
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${naziv}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      });
  }

  nazad(): void {
    void this.router.navigate(['/dokumenti/fakture']);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'POTVRĐEN': return 'chip-success';
      case 'STORNIRAN': return 'chip-error';
      default: return 'chip-default';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'NACRT': return 'Nacrt';
      case 'POTVRĐEN': return 'Potvrđen';
      case 'STORNIRAN': return 'Storniran';
      default: return status;
    }
  }

  formatDatum(datum: string | null | undefined): string {
    if (!datum) return '—';
    const d = new Date(datum);
    if (isNaN(d.getTime())) return datum;
    return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`;
  }

  absKolicina(kolicina: number): number {
    return Math.abs(kolicina);
  }
}
