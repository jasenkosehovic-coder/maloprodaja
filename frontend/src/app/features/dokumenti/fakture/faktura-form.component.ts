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
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';

import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { NotificationService } from '../../../core/services/notification.service';
import { PrometService } from '../services/promet.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { DobavljaciService } from '../../sifarnici/dobavljaci/dobavljaci.service';
import { Dobavljac } from '../../sifarnici/dobavljaci/dobavljaci.models';
import { CreateDokumentDTO } from '../models/promet.models';

export interface FakturaFormData {
  dokumentId: number | null;
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
    MatProgressSpinnerModule,
    LoadingSpinnerComponent,
  ],
  templateUrl: './faktura-form.component.html',
  styleUrl: './faktura-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FakturaFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly prometService = inject(PrometService);
  private readonly dobavljaciService = inject(DobavljaciService);
  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<FakturaFormComponent>);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly dialogData = inject<FakturaFormData>(MAT_DIALOG_DATA);

  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly dobavljaci = signal<Dobavljac[]>([]);
  readonly dobavljacFilter = signal('');
  readonly tipId = signal<number | null>(null);

  readonly filtriranIDobavljaci = computed(() => {
    const q = this.dobavljacFilter().toLowerCase().trim();
    if (!q) return this.dobavljaci();
    return this.dobavljaci().filter(d => d.naziv.toLowerCase().includes(q));
  });

  readonly form = this.fb.group({
    idDobavljaca: this.fb.control<number | null>(null, Validators.required),
    brojFakture: [''],
    datum: [new Date(), Validators.required],
    napomena: [''],
  });

  get dialogTitle(): string {
    return 'Nova ulazna faktura';
  }

  ngOnInit(): void {
    this.ucitajDobavljace();
    this.ucitajTipUF();
  }

  private ucitajTipUF(): void {
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
        const tip = tipovi.find(t => t.kod === 'UF');
        this.tipId.set(tip?.id ?? null);
        this.cdr.markForCheck();
      });
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

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const tipId = this.tipId();
    if (!tipId) {
      this.notification.error('Tip dokumenta UF nije pronađen.');
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
      idDobavljaca: formValue.idDobavljaca as number,
      datum: this.formatDateToIso(formValue.datum),
      brojFakture: formValue.brojFakture || undefined,
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
          this.notification.error('Greška pri snimanju fakture.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(result => {
        this.notification.success('Faktura je uspješno kreirana.');
        this.dialogRef.close(true);
        void this.router.navigate(['/dokumenti/fakture', result.id]);
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
