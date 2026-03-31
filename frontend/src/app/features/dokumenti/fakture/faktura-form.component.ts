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
import { CommonModule } from '@angular/common';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
  FormArray,
  FormGroup,
  AbstractControl,
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';

import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EMPTY } from 'rxjs';
import { catchError, exhaustMap, finalize } from 'rxjs/operators';

import { NotificationService } from '../../../core/services/notification.service';
import { FaktureService } from '../services/fakture.service';
import { DobavljaciService } from '../../sifarnici/dobavljaci/dobavljaci.service';
import { ArtikliService } from '../../sifarnici/artikli/artikli.service';
import { Dobavljac } from '../../sifarnici/dobavljaci/dobavljaci.models';
import { ArtikalKompanija } from '../../sifarnici/artikli/artikli.models';
import { CreateFakturaDTO, UlaznaFakturaDetail } from '../models/dokumenti.models';

export interface FakturaFormData {
  fakturaId: number | null;
}

interface StavkaFormValue {
  idArtikla: number | null;
  naziv: string;
  sifra: string;
  kolicina: number | null;
  vpc: number | null;
  pdvStopa: number | null;
  iznosPdv: number | null;
  ukupno: number | null;
}

@Component({
  selector: 'app-faktura-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
  ],
  templateUrl: './faktura-form.component.html',
  styleUrl: './faktura-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FakturaFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly faktureService = inject(FaktureService);
  private readonly dobavljaciService = inject(DobavljaciService);
  private readonly artikliService = inject(ArtikliService);
  private readonly notification = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<FakturaFormComponent>);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly dialogData = inject<FakturaFormData>(MAT_DIALOG_DATA);

  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly dobavljaci = signal<Dobavljac[]>([]);
  readonly artikli = signal<ArtikalKompanija[]>([]);

  readonly stavkeColumns = ['sifra', 'naziv', 'kolicina', 'vpc', 'pdvStopa', 'iznosPdv', 'ukupno', 'akcije'];

  readonly form = this.fb.group({
    idDobavljaca: this.fb.control<number | null>(null, Validators.required),
    broj: ['', [Validators.required, Validators.maxLength(50)]],
    datum: [new Date(), Validators.required],
    datumValute: this.fb.control<Date | null>(null),
    napomena: [''],
    stavke: this.fb.array<FormGroup>([]),
  });

  get stavkeArray(): FormArray {
    return this.form.controls['stavke'] as FormArray;
  }

  readonly ukupnoBezPdv = computed(() => {
    const stavke = this.stavkeSignal();
    return stavke.reduce((sum, s) => sum + (s.vpc ?? 0) * (s.kolicina ?? 0), 0);
  });

  readonly ukupnoPdv = computed(() => {
    const stavke = this.stavkeSignal();
    return stavke.reduce((sum, s) => sum + (s.iznosPdv ?? 0), 0);
  });

  readonly ukupnoFaktura = computed(() => this.ukupnoBezPdv() + this.ukupnoPdv());

  private readonly stavkeSignal = signal<StavkaFormValue[]>([]);

  get isEditMode(): boolean {
    return this.dialogData.fakturaId !== null;
  }

  get dialogTitle(): string {
    return this.isEditMode ? 'Uredi fakturu' : 'Nova ulazna faktura';
  }

  ngOnInit(): void {
    this.ucitajDobavljace();
    this.ucitajArtikle();

    if (this.isEditMode) {
      this.ucitajFakturu(this.dialogData.fakturaId!);
    }
  }

  private ucitajDobavljace(): void {
    this.dobavljaciService.getAll()
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri učitavanju dobavljača.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.dobavljaci.set(data);
        this.cdr.markForCheck();
      });
  }

  private ucitajArtikle(): void {
    this.artikliService.getAll()
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri učitavanju artikala.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.artikli.set(data);
        this.cdr.markForCheck();
      });
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
          this.notification.error('Greška pri učitavanju fakture.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((faktura: UlaznaFakturaDetail) => {
        this.form.patchValue({
          idDobavljaca: faktura.idDobavljaca,
          broj: faktura.broj,
          datum: new Date(faktura.datum),
          datumValute: faktura.datumValute ? new Date(faktura.datumValute) : null,
          napomena: faktura.napomena ?? '',
        });

        faktura.stavke.forEach(s => {
          const stavkaGroup = this.kreirajStavkuGrupu();
          stavkaGroup.patchValue({
            idArtikla: s.idArtikla,
            naziv: s.nazivArtikla,
            sifra: s.sifraArtikla,
            kolicina: s.kolicina,
            vpc: s.vpc,
            pdvStopa: s.pdvStopa,
            iznosPdv: s.iznosPdv,
            ukupno: s.ukupno,
          });
          this.stavkeArray.push(stavkaGroup);
        });

        this.osvjeziStavkeSignal();
        this.cdr.markForCheck();
      });
  }

  dodajStavku(): void {
    const group = this.kreirajStavkuGrupu();
    this.stavkeArray.push(group);
    this.osvjeziStavkeSignal();
    this.cdr.markForCheck();
  }

  ukloniStavku(index: number): void {
    this.stavkeArray.removeAt(index);
    this.osvjeziStavkeSignal();
    this.cdr.markForCheck();
  }

  onArtikalChange(index: number): void {
    const group = this.stavkeArray.at(index) as FormGroup;
    const idArtikla = group.get('idArtikla')?.value as number | null;
    if (!idArtikla) return;

    const artikal = this.artikli().find(a => a.id === idArtikla);
    if (artikal) {
      group.patchValue({
        naziv: artikal.naziv,
        sifra: artikal.sifra,
        pdvStopa: artikal.pdv,
      });
      this.rekalkulirajStavku(index);
    }
  }

  onStavkaValueChange(index: number): void {
    this.rekalkulirajStavku(index);
  }

  private rekalkulirajStavku(index: number): void {
    const group = this.stavkeArray.at(index) as FormGroup;
    const kolicina = Number(group.get('kolicina')?.value ?? 0);
    const vpc = Number(group.get('vpc')?.value ?? 0);
    const pdvStopa = Number(group.get('pdvStopa')?.value ?? 0);

    const osnovica = kolicina * vpc;
    const iznosPdv = Math.round(osnovica * pdvStopa) / 100;
    const ukupno = osnovica + iznosPdv;

    group.patchValue({ iznosPdv, ukupno }, { emitEvent: false });
    this.osvjeziStavkeSignal();
    this.cdr.markForCheck();
  }

  private osvjeziStavkeSignal(): void {
    const stavke = this.stavkeArray.controls.map(ctrl => (ctrl as FormGroup).getRawValue() as StavkaFormValue);
    this.stavkeSignal.set(stavke);
  }

  private kreirajStavkuGrupu(): FormGroup {
    const group = this.fb.group({
      idArtikla: this.fb.control<number | null>(null, Validators.required),
      naziv: [{ value: '', disabled: true }],
      sifra: [{ value: '', disabled: true }],
      kolicina: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.001)]),
      vpc: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
      pdvStopa: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
      iznosPdv: [{ value: 0, disabled: true }],
      ukupno: [{ value: 0, disabled: true }],
    });
    return group;
  }

  getStavkaControl(index: number, field: string): AbstractControl {
    return (this.stavkeArray.at(index) as FormGroup).get(field)!;
  }

  onSubmit(): void {
    if (this.form.invalid || this.stavkeArray.length === 0) {
      this.form.markAllAsTouched();
      if (this.stavkeArray.length === 0) {
        this.notification.warn('Faktura mora imati najmanje jednu stavku.');
      }
      return;
    }

    const formValue = this.form.getRawValue();
    const dto: CreateFakturaDTO = {
      idDobavljaca: formValue.idDobavljaca as number,
      broj: formValue.broj,
      datum: this.formatDateToIso(formValue.datum),
      datumValute: formValue.datumValute ? this.formatDateToIso(formValue.datumValute) : null,
      napomena: formValue.napomena || null,
      stavke: (formValue.stavke as StavkaFormValue[]).map(s => ({
        idArtikla: s.idArtikla as number,
        kolicina: s.kolicina as number,
        vpc: s.vpc as number,
        pdvStopa: s.pdvStopa as number,
      })),
    };

    const request$ = this.isEditMode
      ? this.faktureService.update(this.dialogData.fakturaId!, dto)
      : this.faktureService.create(dto);

    this.isSaving.set(true);
    request$.pipe(
      exhaustMap(result => {
        this.notification.success(this.isEditMode ? 'Faktura je uspješno ažurirana.' : 'Faktura je uspješno kreirana.');
        this.dialogRef.close(true);
        return [result];
      }),
      finalize(() => {
        this.isSaving.set(false);
        this.cdr.markForCheck();
      }),
      catchError(err => {
        this.notification.error('Greška pri snimanju fakture.');
        console.error(err);
        return EMPTY;
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();
  }

  odustani(): void {
    this.dialogRef.close(false);
  }

  private formatDateToIso(date: Date): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  formatBroj(value: number | null): string {
    if (value === null || value === undefined) return '0,00';
    return new Intl.NumberFormat('bs-BA', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value);
  }
}
