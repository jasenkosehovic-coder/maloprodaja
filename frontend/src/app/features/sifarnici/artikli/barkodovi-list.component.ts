import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { NotificationService } from '../../../core/services/notification.service';
import { ArtikliService } from './artikli.service';
import { ArtikalKompanija, ArtikalVarijanta, CreateVarijanta, CreateVarijantaBarkod } from './artikli.models';
import { BojeService } from '../boje/boje.service';
import { Boja } from '../boje/boje.models';
import { TipoviVelicinaService } from '../tipovi-velicina/tipovi-velicina.service';
import { Velicina } from '../tipovi-velicina/tipovi-velicina.models';

export interface VarijantaUiState {
  varijanta: ArtikalVarijanta;
  noviBarkodinput: string;
  isAddingBarkod: boolean;
  isSavingBarkod: boolean;
  barkodError: string | null;
}

@Component({
  selector: 'app-barkodovi-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatExpansionModule,
    MatChipsModule,
    MatTooltipModule,
  ],
  templateUrl: './barkodovi-list.component.html',
  styleUrl: './barkodovi-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class BarkodoviListComponent implements OnInit {
  private readonly artikliService = inject(ArtikliService);
  private readonly bojeService = inject(BojeService);
  private readonly tipoviVelicinaService = inject(TipoviVelicinaService);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly fb = inject(NonNullableFormBuilder);

  // State
  readonly isLoadingArtikli = signal(false);
  readonly isLoadingVarijante = signal(false);
  readonly isLoadingBoje = signal(false);
  readonly isSavingVarijanta = signal(false);
  readonly loadError = signal<string | null>(null);

  readonly artikli = signal<ArtikalKompanija[]>([]);
  readonly selectedArtikalId = signal<number | null>(null);
  readonly varijante = signal<VarijantaUiState[]>([]);
  readonly boje = signal<Boja[]>([]);
  readonly velicine = signal<Velicina[]>([]);
  readonly isAddingVarijanta = signal(false);

  readonly novaVarijantaForm = this.fb.group({
    idVelicine: this.fb.control<number | null>(null),
    idBoje: this.fb.control<number | null>(null),
  });

  readonly selectedArtikal = computed(() => {
    const id = this.selectedArtikalId();
    return id ? this.artikli().find(a => a.id === id) ?? null : null;
  });

  readonly hasVarijante = computed(() => this.varijante().length > 0);

  constructor() {
    effect(() => {
      const artikal = this.selectedArtikal();
      const idTipa = artikal?.idTipaVelicina ?? null;
      if (!idTipa) {
        this.velicine.set([]);
        return;
      }
      this.tipoviVelicinaService.getVelicine(idTipa)
        .pipe(
          catchError(err => {
            console.error('Greška pri učitavanju veličina:', err);
            this.velicine.set([]);
            return EMPTY;
          }),
          takeUntilDestroyed(this.destroyRef),
        )
        .subscribe(data => {
          this.velicine.set(data);
          this.cdr.markForCheck();
        });
    });
  }

  ngOnInit(): void {
    this.loadArtikli();
    this.loadBoje();
  }

  private loadArtikli(): void {
    this.isLoadingArtikli.set(true);
    this.artikliService.getAll()
      .pipe(
        finalize(() => {
          this.isLoadingArtikli.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.loadError.set('Greška pri učitavanju artikala.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(data => {
        this.artikli.set(data.filter(a => a.aktivan));
        this.cdr.markForCheck();
      });
  }

  private loadBoje(): void {
    this.isLoadingBoje.set(true);
    this.bojeService.getAktivne()
      .pipe(
        finalize(() => {
          this.isLoadingBoje.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          console.error('Greška pri učitavanju boja:', err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(data => {
        this.boje.set(data);
        this.cdr.markForCheck();
      });
  }

  onArtikalChange(idArtikla: number | null): void {
    this.selectedArtikalId.set(idArtikla);
    this.varijante.set([]);
    this.loadError.set(null);
    this.isAddingVarijanta.set(false);
    this.novaVarijantaForm.reset();
    if (!idArtikla) return;
    this.loadVarijante(idArtikla);
  }

  private loadVarijante(idArtikla: number): void {
    this.isLoadingVarijante.set(true);
    this.loadError.set(null);
    this.artikliService.getVarijante(idArtikla)
      .pipe(
        finalize(() => {
          this.isLoadingVarijante.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.loadError.set('Greška pri učitavanju varijanti artikla.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(varijante => {
        this.varijante.set(
          varijante.map(v => ({
            varijanta: v,
            noviBarkodinput: '',
            isAddingBarkod: false,
            isSavingBarkod: false,
            barkodError: null,
          }))
        );
        this.cdr.markForCheck();
      });
  }

  buildVarijantaNaziv(varijanta: ArtikalVarijanta): string {
    const dijelovi: string[] = [];
    if (varijanta.oznakaVelicine) {
      dijelovi.push(varijanta.oznakaVelicine);
    }
    if (varijanta.nazivBoje) {
      dijelovi.push(varijanta.nazivBoje);
    }
    return dijelovi.length > 0 ? dijelovi.join(' / ') : 'Default';
  }

  onToggleAddVarijanta(): void {
    this.isAddingVarijanta.update(v => !v);
    if (!this.isAddingVarijanta()) {
      this.novaVarijantaForm.reset();
    }
  }

  onSpremiVarijantu(): void {
    const idArtikla = this.selectedArtikalId();
    if (!idArtikla) return;

    const dto: CreateVarijanta = {
      idVelicine: this.novaVarijantaForm.controls.idVelicine.value,
      idBoje: this.novaVarijantaForm.controls.idBoje.value,
    };

    this.isSavingVarijanta.set(true);
    this.artikliService.createVarijanta(idArtikla, dto)
      .pipe(
        finalize(() => {
          this.isSavingVarijanta.set(false);
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri kreiranju varijante.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.notification.success('Varijanta uspješno kreirana.');
        this.isAddingVarijanta.set(false);
        this.novaVarijantaForm.reset();
        this.loadVarijante(idArtikla);
      });
  }

  toggleAddBarkod(varijanataId: number): void {
    this.varijante.update(list =>
      list.map(item =>
        item.varijanta.id === varijanataId
          ? { ...item, isAddingBarkod: !item.isAddingBarkod, noviBarkodinput: '', barkodError: null }
          : item
      )
    );
  }

  dodajBarkod(varijantaId: number): void {
    const uiItem = this.varijante().find(v => v.varijanta.id === varijantaId);
    if (!uiItem || !uiItem.noviBarkodinput.trim()) return;

    const dto: CreateVarijantaBarkod = {
      barkod: uiItem.noviBarkodinput.trim(),
      idVarijante: varijantaId,
    };

    this.updateVarijanataState(varijantaId, { isSavingBarkod: true, barkodError: null });

    this.artikliService.createVarijantaBarkod(dto)
      .pipe(
        finalize(() => {
          this.updateVarijanataState(varijantaId, { isSavingBarkod: false });
          this.cdr.markForCheck();
        }),
        catchError(err => {
          const message = err?.error?.message ?? 'Greška pri dodavanju barkoda. Provjeri jedinstvenost.';
          this.updateVarijanataState(varijantaId, { barkodError: message });
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.updateVarijanataState(varijantaId, {
          noviBarkodinput: '',
          isAddingBarkod: false,
          barkodError: null,
        });
        this.notification.success('Barkod uspješno dodan.');
        const id = this.selectedArtikalId();
        if (id) this.loadVarijante(id);
      });
  }

  obrisiBarkod(varijantaId: number, barkodId: number): void {
    this.artikliService.deleteVarijantaBarkod(barkodId)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri brisanju barkoda.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.notification.success('Barkod uspješno obrisan.');
        this.varijante.update(list =>
          list.map(item =>
            item.varijanta.id === varijantaId
              ? {
                  ...item,
                  varijanta: {
                    ...item.varijanta,
                    barkodovi: item.varijanta.barkodovi.filter(b => b.id !== barkodId),
                  },
                }
              : item
          )
        );
        this.cdr.markForCheck();
      });
  }

  updateBarkodinput(varijantaId: number, value: string): void {
    this.updateVarijanataState(varijantaId, { noviBarkodinput: value, barkodError: null });
  }

  private updateVarijanataState(varijantaId: number, patch: Partial<VarijantaUiState>): void {
    this.varijante.update(list =>
      list.map(item =>
        item.varijanta.id === varijantaId ? { ...item, ...patch } : item
      )
    );
  }
}
