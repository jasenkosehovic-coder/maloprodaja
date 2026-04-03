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
import { Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { CrudTableComponent } from '../../../shared/components/crud-table/crud-table.component';
import { CrudFieldConfig, CrudActionsConfig } from '../../../shared/components/crud-table/crud-field-config';
import { NotificationService } from '../../../core/services/notification.service';
import { OtpremnicaService } from '../services/otpremnice.service';
import { OtpremnicaListItem } from '../models/dokumenti.models';
import { OtpremnicaFormComponent } from './otpremnica-form.component';

@Component({
  selector: 'app-otpremnice-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    PageHeaderComponent,
    CrudTableComponent,
  ],
  templateUrl: './otpremnice-list.component.html',
  styleUrl: './otpremnice-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OtpremnicaListComponent implements OnInit {
  private readonly otpremnicaService = inject(OtpremnicaService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly otpremnice = signal<OtpremnicaListItem[]>([]);
  readonly isLoading = signal(false);

  readonly odabranaGodina = signal<number | null>(new Date().getFullYear());

  readonly opcijePodine = computed(() => {
    const tekucaGodina = new Date().getFullYear();
    return [
      { value: null, label: 'Sve godine' },
      { value: tekucaGodina - 2, label: String(tekucaGodina - 2) },
      { value: tekucaGodina - 1, label: String(tekucaGodina - 1) },
      { value: tekucaGodina, label: String(tekucaGodina) },
      { value: tekucaGodina + 1, label: String(tekucaGodina + 1) },
      { value: tekucaGodina + 2, label: String(tekucaGodina + 2) },
    ];
  });

  readonly fields: CrudFieldConfig[] = [
    { key: 'broj', label: 'Broj', type: 'text', readOnly: true },
    { key: 'datum', label: 'Datum', type: 'date', readOnly: true },
    {
      key: 'status', label: 'Status', type: 'select', readOnly: true,
      options: [
        { value: 'KREIRANA', label: 'Kreirana' },
        { value: 'POSLANA', label: 'Poslana' },
        { value: 'PRIMLJENA', label: 'Primljena' },
        { value: 'STORNIRANA', label: 'Stornirana' },
      ],
    },
    { key: 'nazivPoslovnicePosiljaoca', label: 'Pošiljalac', type: 'text', readOnly: true },
    { key: 'nazivPoslovnicePrimaoca', label: 'Primalac', type: 'text', readOnly: true },
    { key: 'brojStavki', label: 'Br. stavki', type: 'number', readOnly: true },
  ];

  readonly actions: CrudActionsConfig = { add: true, edit: true, delete: false, export: true };

  readonly filterableColumns = ['status', 'datum', 'nazivPoslovnicePosiljaoca', 'nazivPoslovnicePrimaoca'];

  readonly rowStyleClass = (row: OtpremnicaListItem) => ({
    'row-primljena': row.status === 'PRIMLJENA',
    'row-stornirana': row.status === 'STORNIRANA',
    'row-poslana': row.status === 'POSLANA',
  });

  ngOnInit(): void {
    this.ucitajPodatke();
  }

  onGodinaChange(godina: number | null): void {
    this.odabranaGodina.set(godina);
    this.ucitajPodatke();
  }

  ucitajPodatke(): void {
    this.isLoading.set(true);
    this.otpremnicaService.list(this.odabranaGodina())
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju otpremnica.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.otpremnice.set(data);
        this.cdr.markForCheck();
      });
  }

  onAddClick(): void {
    const dialogRef = this.dialog.open(OtpremnicaFormComponent, {
      width: '900px',
      maxWidth: '95vw',
      disableClose: true,
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(saved => {
        if (saved) {
          this.ucitajPodatke();
        }
      });
  }

  onEditClick(row: OtpremnicaListItem): void {
    void this.router.navigate(['/dokumenti/otpremnice', row.id]);
  }
}
