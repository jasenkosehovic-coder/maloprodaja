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
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { NotificationService } from '../../../core/services/notification.service';
import { PrometService } from '../services/promet.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { CreateDokumentDTO } from '../models/promet.models';

@Component({
  selector: 'app-izlazna-faktura-form',
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
    MatProgressSpinnerModule,
  ],
  templateUrl: './izlazna-faktura-form.component.html',
  styleUrl: './izlazna-faktura-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IzlaznaFakturaFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly prometService = inject(PrometService);
  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<IzlaznaFakturaFormComponent>);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly isSaving = signal(false);
  readonly tipId = signal<number | null>(null);

  readonly form = this.fb.group({
    datum: [new Date(), Validators.required],
    napomena: [''],
  });

  ngOnInit(): void {
    this.ucitajTipIF();
  }

  private ucitajTipIF(): void {
    this.prometService.getTipoviDokumenata()
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri učitavanju tipova dokumenata.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(tipovi => {
        const tip = tipovi.find(t => t.kod === 'IF');
        this.tipId.set(tip?.id ?? null);
        this.cdr.markForCheck();
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const tipId = this.tipId();
    if (!tipId) {
      this.notification.error('Tip dokumenta IF nije pronađen.');
      return;
    }

    const poslovnicaId = this.authService.currentPoslovnicaId();
    if (!poslovnicaId) {
      this.notification.error('Poslovnica nije definisana za korisnika.');
      return;
    }

    const formValue = this.form.getRawValue();
    const dto: CreateDokumentDTO = {
      idTipa: tipId,
      idPoslovnice: poslovnicaId,
      datum: this.formatDateToIso(formValue.datum),
      napomena: formValue.napomena || undefined,
    };

    this.isSaving.set(true);
    this.prometService.create(dto)
      .pipe(
        finalize(() => {
          this.isSaving.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju izlazne fakture.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(result => {
        this.notification.success('Izlazna faktura je uspješno kreirana.');
        this.dialogRef.close(true);
        void this.router.navigate(['/dokumenti/izlazne-fakture', result.id]);
      });
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
