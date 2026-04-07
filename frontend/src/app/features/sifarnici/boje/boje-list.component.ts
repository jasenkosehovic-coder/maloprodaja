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
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EMPTY } from 'rxjs';
import { catchError, finalize, filter, switchMap } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { BojeService } from './boje.service';
import { Boja, CreateBoja, UpdateBoja } from './boje.models';

const HEX_KOD_PATTERN = /^#[0-9A-Fa-f]{6}$/;

@Component({
  selector: 'app-boje-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './boje-list.component.html',
  styleUrl: './boje-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class BojeListComponent implements OnInit {
  private readonly service = inject(BojeService);
  private readonly notification = inject(NotificationService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly loadError = signal<string | null>(null);

  readonly items = signal<Boja[]>([]);
  readonly editingId = signal<number | null>(null);
  readonly isAddingNew = signal(false);

  readonly displayedColumns = ['naziv', 'hexKod', 'aktivan', 'akcije'];

  readonly bojaForm = this.fb.group({
    naziv: ['', [Validators.required, Validators.maxLength(100)]],
    hexKod: ['', [Validators.pattern(HEX_KOD_PATTERN)]],
  });

  readonly editForm = this.fb.group({
    naziv: ['', [Validators.required, Validators.maxLength(100)]],
    hexKod: ['', [Validators.pattern(HEX_KOD_PATTERN)]],
    aktivan: [true],
  });

  readonly addHexPreview = computed(() => {
    const val = this.bojaForm.controls.hexKod.value;
    return HEX_KOD_PATTERN.test(val) ? val : null;
  });

  readonly editHexValue = signal<string>('#000000');

  readonly editHexPreview = computed(() => {
    const val = this.editHexValue();
    return HEX_KOD_PATTERN.test(val) ? val : null;
  });

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.isLoading.set(true);
    this.loadError.set(null);
    this.service
      .getAll()
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.loadError.set('Greška pri učitavanju boja.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.items.set(data);
        this.cdr.markForCheck();
      });
  }

  onDodajBojaClick(): void {
    this.isAddingNew.set(true);
    this.editingId.set(null);
    this.bojaForm.reset({ naziv: '', hexKod: '' });
  }

  onCancelAdd(): void {
    this.isAddingNew.set(false);
    this.bojaForm.reset();
  }

  onSaveAdd(): void {
    if (this.bojaForm.invalid) {
      this.bojaForm.markAllAsTouched();
      return;
    }
    const dto: CreateBoja = {
      naziv: this.bojaForm.controls.naziv.value,
      hexKod: this.bojaForm.controls.hexKod.value || null,
    };

    this.isSaving.set(true);
    this.service
      .create(dto)
      .pipe(
        finalize(() => {
          this.isSaving.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju boje.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Boja uspješno kreirana.');
        this.isAddingNew.set(false);
        this.bojaForm.reset();
        this.loadData();
      });
  }

  onEditClick(boja: Boja): void {
    this.editingId.set(boja.id);
    this.isAddingNew.set(false);
    this.editHexValue.set(boja.hexKod ?? '#000000');
    this.editForm.setValue({
      naziv: boja.naziv,
      hexKod: boja.hexKod ?? '',
      aktivan: boja.aktivan,
    });
  }

  onEditColorPickerChange(value: string): void {
    this.editHexValue.set(value);
    this.editForm.controls.hexKod.setValue(value);
  }

  onEditHexTextChange(value: string): void {
    if (HEX_KOD_PATTERN.test(value)) {
      this.editHexValue.set(value);
    }
    this.editForm.controls.hexKod.setValue(value);
  }

  onCancelEdit(): void {
    this.editingId.set(null);
    this.editForm.reset();
  }

  onSaveEdit(id: number): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }
    const dto: UpdateBoja = {
      naziv: this.editForm.controls.naziv.value,
      hexKod: this.editForm.controls.hexKod.value || null,
      aktivan: this.editForm.controls.aktivan.value,
    };

    this.isSaving.set(true);
    this.service
      .update(id, dto)
      .pipe(
        finalize(() => {
          this.isSaving.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju boje.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Boja uspješno ažurirana.');
        this.editingId.set(null);
        this.editForm.reset();
        this.loadData();
      });
  }

  onDeleteClick(boja: Boja): void {
    const dialogData: ConfirmDialogData = {
      title: 'Deaktivacija boje',
      message: `Da li ste sigurni da želite deaktivirati boju "${boja.naziv}"?`,
      confirmLabel: 'Deaktiviraj',
      confirmColor: 'warn',
      icon: 'palette',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData, width: '400px' })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.service.deactivate(boja.id).pipe(
          catchError(err => {
            this.notification.error('Greška pri deaktivaciji boje.');
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Boja uspješno deaktivirana.');
        if (this.editingId() === boja.id) {
          this.editingId.set(null);
        }
        this.loadData();
      });
  }

  retry(): void {
    this.loadData();
  }
}
