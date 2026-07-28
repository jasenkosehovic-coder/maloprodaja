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
import { CommonModule } from '@angular/common';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
  FormArray,
  FormGroup,
  AbstractControl,
} from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { NotificationService } from '../../../core/services/notification.service';
import { PrometService } from '../services/promet.service';
import { PoslovnicaService, PoslovnicaOption } from '../../korisnici/poslovnica.service';
import { CreateMedjuskladisnicaDTO, CreateStavkaDTO } from '../models/promet.models';

interface StavkaFormValue {
  idVarijante: number | null;
  kolicina: number | null;
  vpc: number | null;
}

@Component({
  selector: 'app-medjuskladisnica-form',
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
    MatTableModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './medjuskladisnica-form.component.html',
  styleUrl: './medjuskladisnica-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MedjuskladisnicaFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly prometService = inject(PrometService);
  private readonly poslovnicaService = inject(PoslovnicaService);
  private readonly notification = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<MedjuskladisnicaFormComponent>);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly isSaving = signal(false);
  readonly poslovnice = signal<PoslovnicaOption[]>([]);

  readonly stavkeColumns = ['idVarijante', 'kolicina', 'vpc', 'akcije'];

  readonly form = this.fb.group({
    izvorPoslovnicaId: this.fb.control<number | null>(null, Validators.required),
    odredistePoslovnicaId: this.fb.control<number | null>(null, Validators.required),
    datum: [new Date(), Validators.required],
    napomena: [''],
    stavke: this.fb.array<FormGroup>([]),
  });

  get stavkeArray(): FormArray {
    return this.form.controls['stavke'] as FormArray;
  }

  ngOnInit(): void {
    this.ucitajPoslovnice();
  }

  private ucitajPoslovnice(): void {
    this.poslovnicaService.getAll()
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri učitavanju poslovnica.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.poslovnice.set(data);
        this.cdr.markForCheck();
      });
  }

  dodajStavku(): void {
    const group = this.kreirajStavkuGrupu();
    this.stavkeArray.push(group);
    this.cdr.markForCheck();
  }

  ukloniStavku(index: number): void {
    this.stavkeArray.removeAt(index);
    this.cdr.markForCheck();
  }

  getStavkaControl(index: number, field: string): AbstractControl {
    return (this.stavkeArray.at(index) as FormGroup).get(field)!;
  }

  onSubmit(): void {
    if (this.form.invalid || this.stavkeArray.length === 0) {
      this.form.markAllAsTouched();
      if (this.stavkeArray.length === 0) {
        this.notification.warn('Međuskladišnica mora imati najmanje jednu stavku.');
      }
      return;
    }

    const formValue = this.form.getRawValue();

    if (formValue.izvorPoslovnicaId === formValue.odredistePoslovnicaId) {
      this.notification.warn('Izvorna i odredišna poslovnica moraju biti različite.');
      return;
    }

    const stavke: CreateStavkaDTO[] = (formValue.stavke as StavkaFormValue[]).map(s => ({
      idVarijante: s.idVarijante as number,
      kolicina: s.kolicina as number,
      vpc: s.vpc as number,
    }));

    const dto: CreateMedjuskladisnicaDTO = {
      izvorPoslovnicaId: formValue.izvorPoslovnicaId as number,
      odredistePoslovnicaId: formValue.odredistePoslovnicaId as number,
      datum: this.formatDateToIso(formValue.datum),
      napomena: formValue.napomena || undefined,
      stavke,
    };

    this.isSaving.set(true);
    this.prometService.createMedjuskladisnica(dto)
      .pipe(
        finalize(() => {
          this.isSaving.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          const msg = err?.error?.message ?? 'Greška pri kreiranju međuskladišnice.';
          this.notification.error(msg);
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Međuskladišnica je uspješno kreirana i potvrđena.');
        this.dialogRef.close(true);
      });
  }

  odustani(): void {
    this.dialogRef.close(false);
  }

  private kreirajStavkuGrupu(): FormGroup {
    return this.fb.group({
      idVarijante: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
      kolicina: this.fb.control<number | null>(1, [Validators.required, Validators.min(0.001)]),
      vpc: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    });
  }

  private formatDateToIso(date: Date): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
}
