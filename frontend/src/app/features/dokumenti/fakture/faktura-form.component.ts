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
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';

import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EMPTY } from 'rxjs';
import { catchError, exhaustMap, finalize } from 'rxjs/operators';

import { NotificationService } from '../../../core/services/notification.service';
import { FaktureService } from '../services/fakture.service';
import { DobavljaciService } from '../../sifarnici/dobavljaci/dobavljaci.service';
import { Dobavljac } from '../../sifarnici/dobavljaci/dobavljaci.models';
import { CreateFakturaDTO, UlaznaFakturaDetail } from '../models/dokumenti.models';

export interface FakturaFormData {
  fakturaId: number | null;
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
    MatProgressSpinnerModule,
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
  private readonly notification = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<FakturaFormComponent>);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly dialogData = inject<FakturaFormData>(MAT_DIALOG_DATA);

  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly dobavljaci = signal<Dobavljac[]>([]);

  readonly form = this.fb.group({
    idDobavljaca: this.fb.control<number | null>(null, Validators.required),
    broj: ['', [Validators.required, Validators.maxLength(50)]],
    datum: [new Date(), Validators.required],
    datumValute: this.fb.control<Date | null>(null),
    ukupnoBezPdv: this.fb.control<number | null>(null),
    ukupno: this.fb.control<number | null>(null),
    napomena: [''],
  });

  get isEditMode(): boolean {
    return this.dialogData.fakturaId !== null;
  }

  get dialogTitle(): string {
    return this.isEditMode ? 'Uredi fakturu' : 'Nova ulazna faktura';
  }

  ngOnInit(): void {
    this.ucitajDobavljace();

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
          ukupnoBezPdv: faktura.ukupnoBezPdv || null,
          ukupno: faktura.ukupno || null,
          napomena: faktura.napomena ?? '',
        });
        this.cdr.markForCheck();
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = this.form.getRawValue();
    const dto: CreateFakturaDTO = {
      idDobavljaca: formValue.idDobavljaca as number,
      broj: formValue.broj,
      datum: this.formatDateToIso(formValue.datum),
      datumValute: formValue.datumValute ? this.formatDateToIso(formValue.datumValute) : null,
      ukupnoBezPdv: formValue.ukupnoBezPdv ?? null,
      ukupno: formValue.ukupno ?? null,
      napomena: formValue.napomena || null,
    };

    const request$ = this.isEditMode
      ? this.faktureService.update(this.dialogData.fakturaId!, dto)
      : this.faktureService.create(dto);

    this.isSaving.set(true);
    request$.pipe(
      exhaustMap(result => {
        const msg = this.isEditMode ? 'Faktura je uspješno ažurirana.' : 'Faktura je uspješno kreirana.';
        this.notification.success(msg);
        this.dialogRef.close(true);
        if (!this.isEditMode) {
          void this.router.navigate(['/dokumenti/fakture', result.id]);
        }
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
}
