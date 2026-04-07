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
import { MatChipsModule } from '@angular/material/chips';

import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CrudTableComponent } from '../../../shared/components/crud-table/crud-table.component';
import { CrudActionsConfig, CrudFieldConfig, CrudPdfHeader } from '../../../shared/components/crud-table/crud-field-config';
import { TipoviVelicinaService } from './tipovi-velicina.service';
import { TipVelicine, CreateTipVelicine, UpdateTipVelicine, Velicina, UpdateVelicina } from './tipovi-velicina.models';

@Component({
  selector: 'app-tipovi-velicina-list',
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
    MatChipsModule,
    CrudTableComponent,
  ],
  templateUrl: './tipovi-velicina-list.component.html',
  styleUrl: './tipovi-velicina-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class TipoviVelicinaListComponent implements OnInit {
  private readonly service = inject(TipoviVelicinaService);
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

  items: TipVelicine[] = [];
  isLoading = false;
  isSaving = false;

  selectedTip = signal<TipVelicine | null>(null);
  velicine = signal<Velicina[]>([]);
  isLoadingVelicine = signal(false);
  isSavingVelicina = signal(false);

  readonly velicinaForm = this.fb.group({
    oznaka: ['', [Validators.required, Validators.maxLength(20)]],
  });

  readonly velicineColumns = ['dragHandle', 'oznaka', 'redosljed', 'aktivan', 'akcije'];

  readonly tableHeaders: string[] = ['naziv', 'opis', 'aktivan'];

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
      key: 'opis',
      label: 'Opis',
      type: 'text',
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
          this.notification.error('Greška pri učitavanju tipova veličina.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.items = data;
        const selected = this.selectedTip();
        if (selected) {
          const updated = data.find(t => t.id === selected.id) ?? null;
          this.selectedTip.set(updated);
        }
        this.cdr.markForCheck();
      });
  }

  onSelectionChange(rows: any[]): void {
    const tip = rows.length > 0 ? (rows[0] as TipVelicine) : null;
    this.selectedTip.set(tip);
    if (tip) {
      this.loadVelicine(tip.id);
    } else {
      this.velicine.set([]);
    }
  }

  loadVelicine(tipId: number): void {
    this.isLoadingVelicine.set(true);
    this.service
      .getVelicine(tipId)
      .pipe(
        finalize(() => this.isLoadingVelicine.set(false)),
        catchError(err => {
          this.notification.error('Greška pri učitavanju veličina.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.velicine.set([...data].sort((a, b) => a.redosljed - b.redosljed));
      });
  }

  onCreate(row: any): void {
    const dto: CreateTipVelicine = {
      naziv: row['naziv'],
      opis: row['opis'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : true,
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
          this.notification.error('Greška pri kreiranju tipa veličine.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Tip veličine uspješno kreiran.');
        this.loadData();
      });
  }

  onUpdate(row: any): void {
    const id: number = row['id'];
    const dto: UpdateTipVelicine = {
      naziv: row['naziv'],
      opis: row['opis'] || undefined,
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
          this.notification.error('Greška pri ažuriranju tipa veličine.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Tip veličine uspješno ažuriran.');
        this.loadData();
      });
  }

  onDeactivate(row: any): void {
    const id: number = row['id'];

    this.service
      .deactivate(id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji tipa veličine.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Tip veličine uspješno deaktiviran.');
        if (this.selectedTip()?.id === id) {
          this.selectedTip.set(null);
          this.velicine.set([]);
        }
        this.loadData();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (rows.length === 0) return;

    const requests = rows.map(row =>
      this.service.deactivate(row['id']).pipe(
        catchError(err => {
          console.error(`Greška pri deaktivaciji tipa ${row['id']}:`, err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} tip(a) veličine uspješno deaktivirano.`);
        this.loadData();
      });
  }

  onDodajVelicinu(): void {
    if (this.velicinaForm.invalid) return;
    const tip = this.selectedTip();
    if (!tip) return;

    const current = this.velicine();
    const nextRedosljed = current.length > 0
      ? Math.max(...current.map(v => v.redosljed)) + 1
      : 1;

    this.isSavingVelicina.set(true);
    this.service
      .createVelicina(tip.id, {
        oznaka: this.velicinaForm.controls.oznaka.value,
        redosljed: nextRedosljed,
      })
      .pipe(
        finalize(() => this.isSavingVelicina.set(false)),
        catchError(err => {
          this.notification.error('Greška pri dodavanju veličine.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Veličina uspješno dodana.');
        this.velicinaForm.reset();
        this.loadVelicine(tip.id);
      });
  }

  onToggleVelicinuAktivan(velicina: Velicina): void {
    const dto: UpdateVelicina = { aktivan: !velicina.aktivan };
    this.service
      .updateVelicina(velicina.id, dto)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri ažuriranju veličine.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        const tip = this.selectedTip();
        if (tip) this.loadVelicine(tip.id);
      });
  }

  onDeleteVelicina(velicina: Velicina): void {
    const dialogData: ConfirmDialogData = {
      title: 'Brisanje veličine',
      message: `Da li ste sigurni da želite obrisati veličinu "${velicina.oznaka}"?`,
      confirmLabel: 'Obriši',
      confirmColor: 'warn',
      icon: 'straighten',
    };

    this.dialog.open(ConfirmDialogComponent, { data: dialogData, width: '400px' })
      .afterClosed()
      .pipe(
        filter(result => result === true),
        switchMap(() => this.service.deleteVelicina(velicina.id).pipe(
          catchError(err => {
            this.notification.error('Greška pri brisanju veličine.');
            console.error(err);
            return EMPTY;
          })
        )),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Veličina uspješno obrisana.');
        const tip = this.selectedTip();
        if (tip) this.loadVelicine(tip.id);
      });
  }

  onDropVelicina(event: CdkDragDrop<Velicina[]>): void {
    if (event.previousIndex === event.currentIndex) return;
    const tip = this.selectedTip();
    if (!tip) return;

    const current = [...this.velicine()];
    moveItemInArray(current, event.previousIndex, event.currentIndex);

    const updated = current.map((v, i) => ({ ...v, redosljed: i + 1 }));
    this.velicine.set(updated);

    const requests = updated.map(v =>
      this.service.updateVelicina(v.id, { redosljed: v.redosljed }).pipe(
        catchError(err => {
          console.error('Greška pri ažuriranju redosljeda:', err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.loadVelicine(tip.id);
      });
  }
}
