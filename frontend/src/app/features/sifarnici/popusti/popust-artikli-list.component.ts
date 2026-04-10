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
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EMPTY, forkJoin, Observable, of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';

import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ArtikliService } from '../artikli/artikli.service';
import { ArtikalKompanija, ArtikalPoslovnica, BatchPopustUpdate } from '../artikli/artikli.models';
import { DobavljaciService } from '../dobavljaci/dobavljaci.service';
import { Dobavljac } from '../dobavljaci/dobavljaci.models';
import { ProizvodjaciService } from '../proizvodjaci/proizvodjaci.service';
import { Proizvodjac } from '../proizvodjaci/proizvodjaci.models';
import { TipoviVelicinaService } from '../tipovi-velicina/tipovi-velicina.service';
import { TipVelicine } from '../tipovi-velicina/tipovi-velicina.models';
import { DefinicijeAtributaService } from '../definicije-atributa/definicije-atributa.service';
import { DefinicijaAtributa, VrijednostAtributa } from '../definicije-atributa/definicije-atributa.models';

export interface PopustArtikalRow {
  id: number;
  idArtikla: number;
  naziv: string;
  sifra: string;
  idPoslovnice: number;
  idDobavljaca?: number;
  idProizvodjaca?: number;
  idTipaVelicina?: number;
  atributi?: Record<number, number>;
  mpc?: number;
  popustProcenat: number;
  selected: boolean;
}

export interface DefinicijaVrijednosti {
  definicija: DefinicijaAtributa;
  vrijednosti: VrijednostAtributa[];
}

@Component({
  selector: 'app-popust-artikli-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatCheckboxModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
    MatTooltipModule,
  ],
  templateUrl: './popust-artikli-list.component.html',
  styleUrl: './popust-artikli-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class PopustArtikliListComponent implements OnInit {
  private readonly artikliService = inject(ArtikliService);
  private readonly dobavljaciService = inject(DobavljaciService);
  private readonly proizvodjaciService = inject(ProizvodjaciService);
  private readonly tipoviVelicinaService = inject(TipoviVelicinaService);
  private readonly definicijeAtributaService = inject(DefinicijeAtributaService);
  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly displayedColumns = ['select', 'naziv', 'sifra', 'mpc', 'popustProcenat', 'novaMpc'];

  isLoading = false;
  isSaving = false;

  rows = signal<PopustArtikalRow[]>([]);
  dobavljaci = signal<Dobavljac[]>([]);
  proizvodjaci = signal<Proizvodjac[]>([]);
  tipoviVelicina = signal<TipVelicine[]>([]);
  definicijeVrijednosti = signal<DefinicijaVrijednosti[]>([]);

  filterDobavljacId = signal<number | null>(null);
  filterProizvodjacId = signal<number | null>(null);
  filterTipVelicinaId = signal<number | null>(null);
  filterAtributi = signal<Record<number, number | null>>({});

  bulkPopustProcenat = 0;

  readonly filteredRows = computed(() => {
    const all = this.rows();
    const dobavljacId = this.filterDobavljacId();
    const proizvodjacId = this.filterProizvodjacId();
    const tipVelicinaId = this.filterTipVelicinaId();
    const atributiFilter = this.filterAtributi();

    return all.filter(row => {
      if (dobavljacId !== null && row.idDobavljaca !== dobavljacId) return false;
      if (proizvodjacId !== null && row.idProizvodjaca !== proizvodjacId) return false;
      if (tipVelicinaId !== null && row.idTipaVelicina !== tipVelicinaId) return false;

      for (const [defIdStr, vrijednostId] of Object.entries(atributiFilter)) {
        if (vrijednostId === null) continue;
        const defId = Number(defIdStr);
        const rowAtributi = row.atributi ?? {};
        const rowVal = Number(rowAtributi[defId]);
        if (rowVal !== vrijednostId) return false;
      }

      return true;
    });
  });

  readonly selectedCount = computed(() =>
    this.filteredRows().filter(r => r.selected).length
  );

  readonly allSelected = computed(() => {
    const filtered = this.filteredRows();
    return filtered.length > 0 && filtered.every(r => r.selected);
  });

  readonly someSelected = computed(() => {
    const filtered = this.filteredRows();
    return filtered.some(r => r.selected) && !this.allSelected();
  });

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;

    const poslovnicaId = this.authService.currentPoslovnicaId();
    const artikalPoslovnice$: Observable<ArtikalPoslovnica[]> = poslovnicaId
      ? this.artikliService.getAllByPoslovnica(poslovnicaId)
      : this.artikliService.getAllByKompanija();

    forkJoin({
      artikalPoslovnice: artikalPoslovnice$,
      artikalKompanija: this.artikliService.getAll(),
      dobavljaci: this.dobavljaciService.getAll(),
      proizvodjaci: this.proizvodjaciService.getAll(),
      tipoviVelicina: this.tipoviVelicinaService.getAll(),
      definicije: this.definicijeAtributaService.getAll(),
    }).pipe(
      switchMap(data => {
        const defRequests = data.definicije.map(def =>
          this.definicijeAtributaService.getVrijednosti(def.id).pipe(
            catchError(() => of([] as VrijednostAtributa[]))
          )
        );

        if (defRequests.length === 0) {
          return of({ ...data, vrijednostiPerDef: [] as VrijednostAtributa[][] });
        }

        return forkJoin(defRequests).pipe(
          catchError(() => of(data.definicije.map(() => [] as VrijednostAtributa[]))),
          switchMap((vrijednostiPerDef: VrijednostAtributa[][]) => of({ ...data, vrijednostiPerDef }))
        );
      }),
      finalize(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }),
      catchError(err => {
        this.notification.error('Greška pri učitavanju artikala.');
        console.error(err);
        return EMPTY;
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(data => {
      const kompanijaMap = new Map<number, ArtikalKompanija>(
        data.artikalKompanija.map(a => [a.id, a])
      );

      const rows: PopustArtikalRow[] = data.artikalPoslovnice.map(ap => {
        const ak = kompanijaMap.get(ap.idArtikla);
        const atributiRaw = ak?.atributi ?? {};
        const atributi: Record<number, number> = {};
        for (const [k, v] of Object.entries(atributiRaw)) {
          atributi[Number(k)] = Number(v);
        }

        return {
          id: ap.id,
          idArtikla: ap.idArtikla,
          naziv: ap.artikalNaziv,
          sifra: ap.artikalSifra,
          idPoslovnice: ap.idPoslovnice,
          idDobavljaca: ak?.idDobavljaca,
          idProizvodjaca: ak?.idProizvodjaca,
          idTipaVelicina: ak?.idTipaVelicina,
          atributi,
          mpc: ap.mpc,
          popustProcenat: ap.popustProcenat ?? 0,
          selected: false,
        };
      });

      const definicijeVrijednosti: DefinicijaVrijednosti[] = data.definicije
        .map((def, i) => ({
          definicija: def,
          vrijednosti: data.vrijednostiPerDef[i] ?? [],
        }))
        .filter(dv => dv.vrijednosti.length > 0);

      this.rows.set(rows);
      this.dobavljaci.set(data.dobavljaci);
      this.proizvodjaci.set(data.proizvodjaci);
      this.tipoviVelicina.set(data.tipoviVelicina);
      this.definicijeVrijednosti.set(definicijeVrijednosti);

      const initialAtributiFilter: Record<number, number | null> = {};
      definicijeVrijednosti.forEach(dv => {
        initialAtributiFilter[dv.definicija.id] = null;
      });
      this.filterAtributi.set(initialAtributiFilter);

      this.cdr.markForCheck();
    });
  }

  onPopustProcenatChange(row: PopustArtikalRow): void {
    row.popustProcenat = Math.min(100, Math.max(0, row.popustProcenat || 0));
    this.rows.update(rows => [...rows]);
  }

  onToggleAll(checked: boolean): void {
    const filteredIds = new Set(this.filteredRows().map(r => r.id));
    this.rows.update(rows =>
      rows.map(r => filteredIds.has(r.id) ? { ...r, selected: checked } : r)
    );
  }

  onToggleRow(row: PopustArtikalRow): void {
    this.rows.update(rows =>
      rows.map(r => r.id === row.id ? { ...r, selected: !r.selected } : r)
    );
  }

  onAtributFilterChange(defId: number, vrijednostId: number | null): void {
    this.filterAtributi.update(current => ({ ...current, [defId]: vrijednostId }));
  }

  applyBulkPopust(): void {
    const procenat = Math.min(100, Math.max(0, this.bulkPopustProcenat || 0));

    const selectedIds = new Set(
      this.filteredRows().filter(r => r.selected).map(r => r.id)
    );
    this.rows.update(rows =>
      rows.map(r => selectedIds.has(r.id) ? { ...r, popustProcenat: procenat } : r)
    );
  }

  spremiPromjene(): void {
    const toSave: BatchPopustUpdate[] = this.filteredRows()
      .map(r => ({ id: r.id, popustProcenat: r.popustProcenat }));

    if (toSave.length === 0) {
      this.notification.info('Nema promjena za čuvanje.');
      return;
    }

    this.isSaving = true;
    this.artikliService.batchUpdatePopust(toSave).pipe(
      finalize(() => { this.isSaving = false; this.cdr.markForCheck(); }),
      catchError(err => {
        this.notification.error('Greška pri čuvanju popusta.');
        console.error(err);
        return EMPTY;
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => {
      this.notification.success('Popusti uspješno sačuvani.');
      this.loadAll();
    });
  }
}
