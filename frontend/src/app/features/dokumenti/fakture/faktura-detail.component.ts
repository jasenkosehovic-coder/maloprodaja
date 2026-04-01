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
import { EMPTY, forkJoin } from 'rxjs';
import { catchError, filter, finalize, switchMap } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyBamPipe } from '../../../shared/pipes/currency-bam.pipe';
import { FaktureService } from '../services/fakture.service';
import { AddFakturaStavkaDTO, UlaznaFakturaDetail } from '../models/dokumenti.models';
import { ArtikliService } from '../../sifarnici/artikli/artikli.service';
import { ArtikalKompanija, ArtikalPoslovnica } from '../../sifarnici/artikli/artikli.models';

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
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly faktureService = inject(FaktureService);
  private readonly artikliService = inject(ArtikliService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly faktura = signal<UlaznaFakturaDetail | null>(null);
  readonly isLoading = signal(true);
  readonly isAddingStavka = signal(false);
  readonly isSavingStavka = signal(false);

  readonly artikliKompanija = signal<ArtikalKompanija[]>([]);
  readonly artikliPoslovnica = signal<ArtikalPoslovnica[]>([]);

  readonly stavkeColumns = ['redniBroj', 'sifra', 'naziv', 'kolicina', 'vpc', 'pdvStopa', 'iznosPdv', 'ukupno'];
  readonly stavkeNacrtColumns = ['redniBroj', 'sifra', 'naziv', 'kolicina', 'vpc', 'pdvStopa', 'iznosPdv', 'ukupno', 'ukloni'];

  readonly novStavkaForm = this.fb.group({
    idArtikla: this.fb.control<number | null>(null, Validators.required),
    kolicina: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.001)]),
    vpc: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    pdvStopa: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
  });

  readonly izracunatoUkupnoBezPdv = computed(() => {
    const f = this.faktura();
    if (!f) return null;
    return f.stavke.reduce((sum, s) => sum + (s.vpc * s.kolicina), 0);
  });

  readonly izracunatoUkupno = computed(() => {
    const f = this.faktura();
    if (!f) return null;
    return f.stavke.reduce((sum, s) => sum + s.ukupno, 0);
  });

  readonly iznosiSeSlazu = computed(() => {
    const f = this.faktura();
    if (!f || f.unesenoUkupnoBezPdv == null || f.unesenoUkupno == null) return false;
    const TOL = 0.01;
    return Math.abs((this.izracunatoUkupnoBezPdv() ?? 0) - f.unesenoUkupnoBezPdv) <= TOL
        && Math.abs((this.izracunatoUkupno() ?? 0) - f.unesenoUkupno) <= TOL;
  });

  readonly mpcKalkulacija = computed(() => {
    const vpc = this.novStavkaForm.controls.vpc.value;
    const pdv = this.novStavkaForm.controls.pdvStopa.value;
    if (vpc == null || pdv == null || vpc < 0 || pdv < 0) return null;
    return vpc * (1 + pdv / 100);
  });

  readonly iznosSaPdvKalkulacija = computed(() => {
    const vpc = this.novStavkaForm.controls.vpc.value;
    const kolicina = this.novStavkaForm.controls.kolicina.value;
    const pdv = this.novStavkaForm.controls.pdvStopa.value;
    if (vpc == null || kolicina == null || pdv == null) return null;
    const osnova = vpc * kolicina;
    return osnova + osnova * (pdv / 100);
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.params['id'];
    const id = Number(idParam);
    if (!id || isNaN(id)) {
      this.snackBar.open('Neispravni ID fakture.', 'Zatvori', { duration: 3000 });
      void this.router.navigate(['/dokumenti/fakture']);
      return;
    }
    this.ucitajFakturu(id);

    this.novStavkaForm.controls.idArtikla.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(idArtikla => this.onArtikalChange(idArtikla));
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
        if (data.statusFakture === 'NACRT') {
          this.ucitajArtikle();
        }
        this.cdr.markForCheck();
      });
  }

  private ucitajArtikle(): void {
    forkJoin({
      kompanija: this.artikliService.getAll().pipe(catchError(() => [])),
      poslovnica: this.artikliService.getAllByPoslovnica(0).pipe(catchError(() => [])),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ kompanija, poslovnica }) => {
        this.artikliKompanija.set(
          (kompanija as ArtikalKompanija[]).filter(a => a.aktivan)
        );
        this.artikliPoslovnica.set(poslovnica as ArtikalPoslovnica[]);
        this.cdr.markForCheck();
      });
  }

  private onArtikalChange(idArtikla: number | null): void {
    if (idArtikla == null) return;

    const artikal = this.artikliKompanija().find(a => a.id === idArtikla);
    if (artikal) {
      this.novStavkaForm.controls.pdvStopa.setValue(artikal.pdv);
    }

    const artPoslov = this.artikliPoslovnica().find(ap => ap.idArtikla === idArtikla);
    if (artPoslov?.vpc != null) {
      this.novStavkaForm.controls.vpc.setValue(artPoslov.vpc);
    }

    this.cdr.markForCheck();
  }

  dodajStavku(): void {
    if (this.novStavkaForm.invalid) {
      this.novStavkaForm.markAllAsTouched();
      return;
    }

    const f = this.faktura();
    if (!f) return;

    const formValue = this.novStavkaForm.getRawValue();
    const dto: AddFakturaStavkaDTO = {
      idArtikla: formValue.idArtikla as number,
      kolicina: formValue.kolicina as number,
      vpc: formValue.vpc as number,
      pdvStopa: formValue.pdvStopa as number,
    };

    this.isSavingStavka.set(true);
    this.faktureService.addStavka(f.id, dto)
      .pipe(
        finalize(() => {
          this.isSavingStavka.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.snackBar.open('Greška pri dodavanju stavke.', 'Zatvori', { duration: 3000 });
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(updated => {
        this.faktura.set(updated);
        this.novStavkaForm.reset();
        this.isAddingStavka.set(false);
        if (updated.statusFakture === 'POTVRDJENO') {
          this.snackBar.open('Faktura je automatski potvrđena — iznosi se slažu.', 'Zatvori', { duration: 4000 });
        } else {
          this.snackBar.open('Stavka je uspješno dodana.', 'Zatvori', { duration: 2000 });
        }
        this.cdr.markForCheck();
      });
  }

  ukloniStavku(stavkaId: number): void {
    const f = this.faktura();
    if (!f) return;

    const stavka = f.stavke.find(s => s.id === stavkaId);
    const dialogData: ConfirmDialogData = {
      title: 'Uklanjanje stavke',
      message: `Jeste li sigurni da želite ukloniti stavku "${stavka?.nazivArtikla ?? ''}"?`,
      confirmLabel: 'Ukloni',
      cancelLabel: 'Odustani',
      confirmColor: 'warn',
      icon: 'delete',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.faktureService.removeStavka(f.id, stavkaId).pipe(
          catchError(err => {
            this.snackBar.open('Greška pri uklanjanju stavke.', 'Zatvori', { duration: 3000 });
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(updated => {
        this.faktura.set(updated);
        this.snackBar.open('Stavka je uspješno uklonjena.', 'Zatvori', { duration: 2000 });
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
