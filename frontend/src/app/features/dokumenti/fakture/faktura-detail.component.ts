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
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
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
import { ArtikalKompanija, ArtikalPoslovnica, ArtikalVarijanta } from '../../sifarnici/artikli/artikli.models';

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
  readonly artikliPoslovnica = signal<ArtikalPoslovnica[]>([]);
  readonly varijante = signal<ArtikalVarijanta[]>([]);

  private readonly baseStavkeColumns = [
    'redniBroj', 'naziv', 'velicina', 'boja', 'kolicina',
    'vpc', 'popustProcenat', 'iznosPopusta', 'pdvProcenat', 'iznosPdv', 'marzaProcenat',
    'mpc', 'iznosMpc',
  ];

  readonly aktiveStavkeColumns = computed(() =>
    this.dokument()?.status === 'NACRT'
      ? [...this.baseStavkeColumns, 'ukloni']
      : this.baseStavkeColumns
  );

  readonly novStavkaForm = this.fb.group({
    idArtikla: this.fb.control<number | null>(null, Validators.required),
    idVarijante: this.fb.control<number | null>(null, Validators.required),
    kolicina: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.001)]),
    vpc: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    pdvProcenat: this.fb.control<number>(0),
    marzaProcenat: this.fb.control<number>(0),
    popustProcenat: this.fb.control<number>(0, [Validators.min(0), Validators.max(100)]),
  });

  private readonly idArtiklaSignal = toSignal(
    this.novStavkaForm.controls.idArtikla.valueChanges,
    { initialValue: null }
  );

  private readonly formValue = toSignal(
    this.novStavkaForm.valueChanges,
    { initialValue: this.novStavkaForm.getRawValue() }
  );

  readonly stavkaPreview = computed(() => {
    const val = this.formValue();
    const vpc    = val.vpc ?? 0;
    const marza  = val.marzaProcenat ?? 0;
    const pdv    = val.pdvProcenat ?? 0;
    const popust = val.popustProcenat ?? 0;

    const popustPerUnit = this.round4(vpc * popust / 100);
    const vpcNeto       = vpc - popustPerUnit;                        // bruto VPC (sadrži PDV)
    const vpcBezPdv     = this.round4(vpcNeto * 100 / (100 + pdv));  // za prikaz iznosPdv
    const pdvPerUnit    = this.round4(vpcNeto - vpcBezPdv);          // izvučeni PDV iz VPC
    const marzaPerUnit  = this.round4(vpcNeto * marza / 100);        // marža na bruto VPC
    const ukupnaCijena  = vpcNeto + marzaPerUnit;
    const mpcRaw        = ukupnaCijena * (1 + pdv / 100);            // PDV na ukupnu cijenu
    const mpc           = Math.round(mpcRaw * 100) / 100;

    return { iznosPopusta: popustPerUnit, iznosMarze: marzaPerUnit, iznosPdv: pdvPerUnit, mpc };
  });

  private round4(n: number): number {
    return Math.round(n * 10000) / 10000;
  }

  readonly artikalPretragaTekst = signal('');

  readonly filteredArtikliKompanija = computed(() => {
    const tekst = this.artikalPretragaTekst().toLowerCase().trim();
    const lista = this.artikliKompanija();
    if (!tekst) return lista;
    return lista.filter(a =>
      a.naziv.toLowerCase().includes(tekst) || a.sifra.toLowerCase().includes(tekst)
    );
  });

  private readonly dodaneVarijanteIds = computed(() =>
    new Set((this.dokument()?.stavke ?? []).map(s => s.idVarijante))
  );

  readonly varijanteDrugogArtikla = computed(() => {
    const idArtikla = this.idArtiklaSignal();
    if (!idArtikla) return [];
    const dodane = this.dodaneVarijanteIds();
    return this.varijante().filter(v => v.idArtikla === idArtikla && v.aktivan && !dodane.has(v.id));
  });

  readonly ukupnoStavki = computed(() => {
    const d = this.dokument();
    if (!d) return 0;
    return d.stavke.reduce((sum, s) => sum + s.iznosMpc, 0);
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
          const artikal = this.artikliKompanija().find(a => a.id === idArtikla);
          if (artikal) {
            this.novStavkaForm.controls.pdvProcenat.setValue(artikal.pdv ?? 0);
          }
          const artPoslovnica = this.artikliPoslovnica().find(a => a.idArtikla === idArtikla);
          if (artPoslovnica?.marza != null) {
            this.novStavkaForm.controls.marzaProcenat.setValue(artPoslovnica.marza);
          }
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
          this.ucitajArtikle(data.idPoslovnice);
        }
        this.cdr.markForCheck();
      });
  }

  private ucitajArtikle(idPoslovnice: number): void {
    this.artikliService.getAll()
      .pipe(catchError(() => []), takeUntilDestroyed(this.destroyRef))
      .subscribe(artikli => {
        this.artikliKompanija.set((artikli as ArtikalKompanija[]).filter(a => a.aktivan));
        this.cdr.markForCheck();
      });

    this.artikliService.getAllByPoslovnica(idPoslovnice)
      .pipe(catchError(() => []), takeUntilDestroyed(this.destroyRef))
      .subscribe(ap => {
        this.artikliPoslovnica.set(ap as ArtikalPoslovnica[]);
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
        const dostupne = this.varijanteDrugogArtikla();
        if (dostupne.length === 1) {
          this.novStavkaForm.controls.idVarijante.setValue(dostupne[0].id);
        }
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
      vpc: formValue.vpc as number,
      pdvProcenat: formValue.pdvProcenat,
      marzaProcenat: formValue.marzaProcenat,
      popustProcenat: formValue.popustProcenat,
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

    this.prometService.downloadFakturaPdf(d.id)
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

  onMpcChange(value: string): void {
    const mpcValue = parseFloat(value);
    if (isNaN(mpcValue) || mpcValue <= 0) return;

    const val = this.novStavkaForm.getRawValue();
    const vpc    = val.vpc ?? 0;
    const pdv    = val.pdvProcenat ?? 0;
    const popust = val.popustProcenat ?? 0;
    const vpcNeto = vpc * (1 - popust / 100);  // bruto nakon popusta (sadrži PDV)

    if (vpcNeto <= 0) return;

    // mpc = vpcNeto × (1 + marza/100) × (1 + pdv/100)  →  isolate marza
    const marza = (mpcValue / (vpcNeto * (1 + pdv / 100)) - 1) * 100;
    this.novStavkaForm.controls.marzaProcenat.setValue(this.round4(marza));
    this.cdr.markForCheck();
  }
}
