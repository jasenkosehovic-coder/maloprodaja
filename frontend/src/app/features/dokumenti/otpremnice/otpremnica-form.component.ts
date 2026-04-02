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
import { catchError, exhaustMap, finalize } from 'rxjs/operators';

import { NotificationService } from '../../../core/services/notification.service';
import { OtpremnicaService } from '../services/otpremnice.service';
import { ArtikliService } from '../../sifarnici/artikli/artikli.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { CreateOtpremnicaDTO } from '../models/dokumenti.models';
import { ArtikalPoslovnica } from '../../sifarnici/artikli/artikli.models';

interface PoslovnicaOption {
  id: number;
  naziv: string;
}

interface StavkaFormValue {
  idArtikla: number | null;
  naziv: string;
  kolicina: number | null;
}

@Component({
  selector: 'app-otpremnica-form',
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
  templateUrl: './otpremnica-form.component.html',
  styleUrl: './otpremnica-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OtpremnicaFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly otpremnicaService = inject(OtpremnicaService);
  private readonly artikliService = inject(ArtikliService);
  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<OtpremnicaFormComponent>);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly isSaving = signal(false);
  readonly poslovnice = signal<PoslovnicaOption[]>([]);
  readonly artikliPoslovnice = signal<ArtikalPoslovnica[]>([]);

  readonly stavkeColumns = ['idArtikla', 'naziv', 'kolicina', 'akcije'];

  readonly form = this.fb.group({
    idPoslovnicePrimaoca: this.fb.control<number | null>(null, Validators.required),
    broj: ['', [Validators.required, Validators.maxLength(50)]],
    datum: [new Date(), Validators.required],
    napomena: [''],
    stavke: this.fb.array<FormGroup>([]),
  });

  get stavkeArray(): FormArray {
    return this.form.controls['stavke'] as FormArray;
  }

  ngOnInit(): void {
    this.ucitajPoslovnice();
    this.ucitajArtikle();
  }

  private ucitajPoslovnice(): void {
    const idVlasnicePoslovnice = this.authService.currentPoslovnicaId();

    this.otpremnicaService.list()
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri učitavanju poslovnica.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(otpremnice => {
        const unique = new Map<number, string>();
        otpremnice.forEach(o => {
          if (o.idPoslovnicePrimaoca !== idVlasnicePoslovnice) {
            unique.set(o.idPoslovnicePrimaoca, o.nazivPoslovnicePrimaoca);
          }
          if (o.idPoslovnicePosiljaoca !== idVlasnicePoslovnice) {
            unique.set(o.idPoslovnicePosiljaoca, o.nazivPoslovnicePosiljaoca);
          }
        });
        const lista: PoslovnicaOption[] = Array.from(unique.entries()).map(([id, naziv]) => ({ id, naziv }));
        this.poslovnice.set(lista);
        this.cdr.markForCheck();
      });
  }

  private ucitajArtikle(): void {
    const idPoslovnice = this.authService.currentPoslovnicaId();
    if (!idPoslovnice) return;

    this.artikliService.getAllByPoslovnica(idPoslovnice)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri učitavanju artikala.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.artikliPoslovnice.set(data);
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

  onArtikalChange(index: number): void {
    const group = this.stavkeArray.at(index) as FormGroup;
    const idArtikla = group.get('idArtikla')?.value as number | null;
    if (!idArtikla) return;

    const artikal = this.artikliPoslovnice().find(a => a.idArtikla === idArtikla);
    if (artikal) {
      group.patchValue({ naziv: artikal.artikalNaziv });
      this.cdr.markForCheck();
    }
  }

  getStavkaControl(index: number, field: string): AbstractControl {
    return (this.stavkeArray.at(index) as FormGroup).get(field)!;
  }

  onSubmit(): void {
    if (this.form.invalid || this.stavkeArray.length === 0) {
      this.form.markAllAsTouched();
      if (this.stavkeArray.length === 0) {
        this.notification.warn('Otpremnica mora imati najmanje jednu stavku.');
      }
      return;
    }

    const formValue = this.form.getRawValue();
    const dto: CreateOtpremnicaDTO = {
      idPoslovnicePrimaoca: formValue.idPoslovnicePrimaoca as number,
      broj: formValue.broj,
      datum: this.formatDateToIso(formValue.datum),
      napomena: formValue.napomena || null,
      stavke: (formValue.stavke as StavkaFormValue[]).map(s => ({
        idArtikla: s.idArtikla as number,
        kolicina: s.kolicina as number,
      })),
    };

    this.isSaving.set(true);
    this.otpremnicaService.create(dto)
      .pipe(
        exhaustMap(result => {
          this.notification.success('Otpremnica je uspješno kreirana.');
          this.dialogRef.close(true);
          return [result];
        }),
        finalize(() => {
          this.isSaving.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri snimanju otpremnice.');
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
      kolicina: this.fb.control<number | null>(1, [Validators.required, Validators.min(0.001)]),
    });
  }

  private formatDateToIso(date: Date): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
}
