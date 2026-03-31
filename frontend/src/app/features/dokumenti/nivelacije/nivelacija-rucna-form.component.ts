import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  inject,
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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EMPTY } from 'rxjs';
import { catchError, exhaustMap, finalize } from 'rxjs/operators';

import { NotificationService } from '../../../core/services/notification.service';
import { NivelacijeService } from '../services/nivelacije.service';
import { ArtikliService } from '../../sifarnici/artikli/artikli.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { CreateNivelacijaRucnaDTO } from '../models/dokumenti.models';
import { ArtikalPoslovnica } from '../../sifarnici/artikli/artikli.models';

interface StavkaRucnaFormValue {
  idArtikla: number | null;
  naziv: string;
  mpcStara: number;
  mpcNova: number | null;
}

@Component({
  selector: 'app-nivelacija-rucna-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './nivelacija-rucna-form.component.html',
  styleUrl: './nivelacija-rucna-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NivelacijaRucnaFormComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly nivelacijeService = inject(NivelacijeService);
  private readonly artikliService = inject(ArtikliService);
  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<NivelacijaRucnaFormComponent>);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  isSavingState = false;

  readonly stavkeColumns = ['idArtikla', 'naziv', 'mpcStara', 'mpcNova', 'akcije'];

  readonly form = this.fb.group({
    broj: ['', Validators.required],
    datum: [new Date(), Validators.required],
    napomena: [''],
    stavke: this.fb.array<FormGroup>([]),
  });

  get stavkeArray(): FormArray {
    return this.form.controls['stavke'] as FormArray;
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

  onIdArtiklaChange(index: number): void {
    const group = this.stavkeArray.at(index) as FormGroup;
    const idArtikla = group.get('idArtikla')?.value as number | null;
    if (!idArtikla) return;

    const idPoslovnice = this.authService.currentPoslovnicaId();
    if (!idPoslovnice) return;

    this.artikliService.getAllByPoslovnica(idPoslovnice)
      .pipe(
        catchError(() => {
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((artikli: ArtikalPoslovnica[]) => {
        const artikal = artikli.find(a => a.idArtikla === idArtikla);
        if (artikal) {
          group.patchValue({
            naziv: artikal.artikalNaziv,
            mpcStara: artikal.mpc ?? 0,
          });
        }
        this.cdr.markForCheck();
      });
  }

  getStavkaControl(index: number, field: string): AbstractControl {
    return (this.stavkeArray.at(index) as FormGroup).get(field)!;
  }

  onSubmit(): void {
    if (this.form.invalid || this.stavkeArray.length === 0) {
      this.form.markAllAsTouched();
      if (this.stavkeArray.length === 0) {
        this.notification.warn('Nivelacija mora imati najmanje jednu stavku.');
      }
      return;
    }

    const formValue = this.form.getRawValue();
    const dto: CreateNivelacijaRucnaDTO = {
      broj: formValue.broj,
      datum: this.formatDateToIso(formValue.datum),
      napomena: formValue.napomena || null,
      stavke: (formValue.stavke as StavkaRucnaFormValue[]).map(s => ({
        idArtikla: s.idArtikla as number,
        mpcNova: s.mpcNova as number,
      })),
    };

    this.isSavingState = true;
    this.cdr.markForCheck();

    this.nivelacijeService.kreirajRucnu(dto)
      .pipe(
        exhaustMap(result => {
          this.notification.success('Nivelacija je uspješno kreirana.');
          this.dialogRef.close(true);
          return [result];
        }),
        finalize(() => {
          this.isSavingState = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri snimanju nivelacije.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe();
  }

  odustani(): void {
    this.dialogRef.close(false);
  }

  private kreirajStavkuGrupu(): FormGroup {
    return this.fb.group({
      idArtikla: this.fb.control<number | null>(null, Validators.required),
      naziv: [{ value: '', disabled: true }],
      mpcStara: [{ value: 0, disabled: true }],
      mpcNova: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    });
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
