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
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EMPTY, forkJoin } from 'rxjs';
import { catchError, filter, finalize, switchMap } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

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
import { CrudTableComponent } from '../../../shared/components/crud-table/crud-table.component';
import { CrudActionsConfig, CrudFieldConfig, CrudPdfHeader } from '../../../shared/components/crud-table/crud-field-config';
import { DefinicijeAtributaService } from './definicije-atributa.service';
import {
  DefinicijaAtributa,
  CreateDefinicijaAtributa,
  UpdateDefinicijaAtributa,
  VrijednostAtributa,
  UpdateVrijednostAtributa,
} from './definicije-atributa.models';

@Component({
  selector: 'app-definicije-atributa-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DragDropModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    CrudTableComponent,
  ],
  templateUrl: './definicije-atributa-list.component.html',
  styleUrl: './definicije-atributa-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class DefinicijeAtributaListComponent implements OnInit {
  private readonly service = inject(DefinicijeAtributaService);
  private readonly notification = inject(NotificationService);
  private readonly dialog = inject(MatDialog);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly pdfHeader = computed<CrudPdfHeader | null>(() => {
    const k = this.authService.korisnik();
    if (!k) return null;
    return {
      kompanijaNaziv: k.kompanijaNaziv,
      kompanijaAdresa: k.kompanijaAdresa,
      kompanijaGrad: k.kompanijaGrad,
      poslovnicaNaziv: k.poslovnicaId ? k.poslovnicaNaziv : undefined,
      poslovnicaAdresa: k.poslovnicaId ? k.poslovnicaAdresa : undefined,
      poslovnicaGrad: k.poslovnicaId ? k.poslovnicaGrad : undefined,
    };
  });

  items: DefinicijaAtributa[] = [];
  isLoading = false;
  isSaving = false;

  selectedDefinicija = signal<DefinicijaAtributa | null>(null);
  vrijednosti = signal<VrijednostAtributa[]>([]);
  isLoadingVrijednosti = signal(false);
  isSavingVrijednost = signal(false);

  readonly vrijednostForm = this.fb.group({
    vrijednost: ['', [Validators.required, Validators.maxLength(200)]],
  });

  readonly vrijednostiColumns = ['dragHandle', 'vrijednost', 'redosljed', 'aktivan', 'akcije'];

  readonly tableHeaders: string[] = ['naziv', 'redosljed', 'obavezno', 'zaWeb', 'aktivan'];

  readonly tableActions: CrudActionsConfig = {
    add: true,
    edit: true,
    delete: true,
    export: true,
  };

  readonly fields: CrudFieldConfig[] = [
    {
      key: 'id',
      label: 'ID',
      type: 'number',
      visible: false,
    },
    {
      key: 'naziv',
      label: 'Naziv',
      type: 'text',
      required: true,
      requiredMessage: 'Naziv je obavezan.',
    },
    {
      key: 'redosljed',
      label: 'Redosljed',
      type: 'number',
      defaultValue: 1,
    },
    {
      key: 'obavezno',
      label: 'Obavezno',
      type: 'boolean',
      defaultValue: false,
    },
    {
      key: 'zaWeb',
      label: 'Za web',
      type: 'boolean',
      defaultValue: false,
    },
    {
      key: 'aktivan',
      label: 'Aktivan',
      type: 'boolean',
      defaultValue: true,
    },
  ];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.service
      .getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju definicija atributa.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.items = data;
        const selected = this.selectedDefinicija();
        if (selected) {
          const updated = data.find(d => d.id === selected.id) ?? null;
          this.selectedDefinicija.set(updated);
        }
        this.cdr.markForCheck();
      });
  }

  onRowClick(row: DefinicijaAtributa): void {
    this.selectedDefinicija.set(row);
    this.loadVrijednosti(row.id);
  }

  onSelectionChange(rows: any[]): void {
    const definicija = rows.length > 0 ? (rows[0] as DefinicijaAtributa) : null;
    this.selectedDefinicija.set(definicija);
    if (definicija) {
      this.loadVrijednosti(definicija.id);
    } else {
      this.vrijednosti.set([]);
    }
  }

  loadVrijednosti(definicijaId: number): void {
    this.isLoadingVrijednosti.set(true);
    this.service
      .getVrijednosti(definicijaId)
      .pipe(
        finalize(() => this.isLoadingVrijednosti.set(false)),
        catchError(err => {
          this.notification.error('Greška pri učitavanju vrijednosti atributa.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.vrijednosti.set([...data].sort((a, b) => a.redosljed - b.redosljed));
      });
  }

  onCreate(row: any): void {
    const dto: CreateDefinicijaAtributa = {
      naziv: row['naziv'],
      redosljed: row['redosljed'] ?? 1,
      obavezno: row['obavezno'] ?? false,
      zaWeb: row['zaWeb'] ?? false,
    };

    this.isSaving = true;
    this.service
      .create(dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju definicije atributa.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Definicija atributa uspješno kreirana.');
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const id: number = row['id'];
    const dto: UpdateDefinicijaAtributa = {
      naziv: row['naziv'],
      redosljed: row['redosljed'],
      obavezno: row['obavezno'] ?? false,
      zaWeb: row['zaWeb'] ?? false,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : false,
    };

    this.isSaving = true;
    this.service
      .update(id, dto)
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri ažuriranju definicije atributa.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Definicija atributa uspješno ažurirana.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    const id: number = row['id'];

    this.service
      .deactivate(id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji definicije atributa.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Definicija atributa uspješno deaktivirana.');
        if (this.selectedDefinicija()?.id === id) {
          this.selectedDefinicija.set(null);
          this.vrijednosti.set([]);
        }
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (rows.length === 0) return;

    const requests = rows.map(row =>
      this.service.deactivate(row['id']).pipe(
        catchError(err => {
          console.error(`Greška pri deaktivaciji definicije ${row['id']}:`, err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} definicija atributa uspješno deaktivirano.`);
        this.loadData();
      });
  }

  onDodajVrijednost(): void {
    if (this.vrijednostForm.invalid) return;
    const definicija = this.selectedDefinicija();
    if (!definicija) return;

    const current = this.vrijednosti();
    const nextRedosljed = current.length > 0
      ? Math.max(...current.map(v => v.redosljed)) + 1
      : 1;

    this.isSavingVrijednost.set(true);
    this.service
      .createVrijednost(definicija.id, {
        vrijednost: this.vrijednostForm.controls.vrijednost.value,
        redosljed: nextRedosljed,
      })
      .pipe(
        finalize(() => this.isSavingVrijednost.set(false)),
        catchError(err => {
          this.notification.error('Greška pri dodavanju vrijednosti atributa.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Vrijednost atributa uspješno dodana.');
        this.vrijednostForm.reset();
        this.loadVrijednosti(definicija.id);
      });
  }

  onToggleVrijednostAktivan(vrijednost: VrijednostAtributa): void {
    const dto: UpdateVrijednostAtributa = { aktivan: !vrijednost.aktivan };
    this.service
      .updateVrijednost(vrijednost.id, dto)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri ažuriranju vrijednosti atributa.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        const definicija = this.selectedDefinicija();
        if (definicija) this.loadVrijednosti(definicija.id);
      });
  }

  onDeleteVrijednost(vrijednost: VrijednostAtributa): void {
    const dialogData: ConfirmDialogData = {
      title: 'Brisanje vrijednosti atributa',
      message: `Da li ste sigurni da želite obrisati vrijednost "${vrijednost.vrijednost}"?`,
      confirmLabel: 'Obriši',
      confirmColor: 'warn',
      icon: 'label_off',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData, width: '400px' })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.service.deleteVrijednost(vrijednost.id).pipe(
          catchError(err => {
            this.notification.error('Greška pri brisanju vrijednosti atributa.');
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Vrijednost atributa uspješno obrisana.');
        const definicija = this.selectedDefinicija();
        if (definicija) this.loadVrijednosti(definicija.id);
      });
  }

  onDropVrijednost(event: CdkDragDrop<VrijednostAtributa[]>): void {
    if (event.previousIndex === event.currentIndex) return;
    const definicija = this.selectedDefinicija();
    if (!definicija) return;

    const current = [...this.vrijednosti()];
    moveItemInArray(current, event.previousIndex, event.currentIndex);

    const updated = current.map((v, i) => ({ ...v, redosljed: i + 1 }));
    this.vrijednosti.set(updated);

    const requests = updated.map(v =>
      this.service.updateVrijednost(v.id, { redosljed: v.redosljed }).pipe(
        catchError(err => {
          console.error('Greška pri ažuriranju redosljeda:', err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.loadVrijednosti(definicija.id);
      });
  }
}
