import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { ArtikliService } from './artikli.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UpdateZalihe, ZalihaListItem } from './artikli.models';
import { AuthService } from '../../../core/auth/services/auth.service';
import { PoslovnicaOption, PoslovnicaService } from '../../korisnici/poslovnica.service';
import { CrudTableComponent } from '../../../shared/components/crud-table/crud-table.component';
import { CrudActionsConfig, CrudFieldConfig, CrudPdfHeader } from '../../../shared/components/crud-table/crud-field-config';

@Component({
  selector: 'app-min-opt-zalihe',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressBarModule,
    CrudTableComponent,
  ],
  templateUrl: './min-opt-zalihe.component.html',
  styleUrl: './min-opt-zalihe.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class MinOptZaliheComponent implements OnInit {
  private readonly artikliService = inject(ArtikliService);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly authService = inject(AuthService);
  private readonly poslovnicaService = inject(PoslovnicaService);

  items: ZalihaListItem[] = [];
  poslovnice: PoslovnicaOption[] = [];
  selectedPoslovnicaId: number | null = null;
  isLoading = false;
  isSaving = false;

  readonly tableHeaders = [
    'artikalNaziv',
    'artikalSifra',
    'varijantaNaziv',
    'kolicina',
    'minZaliha',
    'optimalnaZaliha',
  ];

  readonly fields: CrudFieldConfig[] = [
    { key: 'id', label: 'ID', type: 'number', visible: false },
    { key: 'artikalNaziv', label: 'Artikal', type: 'text', readOnly: true },
    { key: 'artikalSifra', label: 'Šifra', type: 'text', readOnly: true, visible: false },
    { key: 'varijantaNaziv', label: 'Varijanta', type: 'text', readOnly: true },
    { key: 'kolicina', label: 'Količina', type: 'number', readOnly: true, decimals: 2 },
    {
      key: 'minZaliha',
      label: 'Min zaliha',
      type: 'number',
      decimals: 2,
      min: 0,
      minMessage: 'Min zaliha ne može biti negativna.',
    },
    {
      key: 'optimalnaZaliha',
      label: 'Opt zaliha',
      type: 'number',
      decimals: 2,
      min: 0,
      minMessage: 'Opt zaliha ne može biti negativna.',
    },
  ];

  readonly tableActions: CrudActionsConfig = { edit: true };

  readonly pdfHeader = computed<CrudPdfHeader | null>(() => {
    const k = this.authService.korisnik();
    if (!k) return null;
    return {
      kompanijaNaziv: k.kompanijaNaziv,
      kompanijaAdresa: k.kompanijaAdresa,
      kompanijaGrad: k.kompanijaGrad,
      poslovnicaNaziv: k.poslovnicaNaziv,
      poslovnicaAdresa: k.poslovnicaAdresa,
      poslovnicaGrad: k.poslovnicaGrad,
    };
  });

  get isSuperAdminBezPoslovnice(): boolean {
    return this.authService.hasRole('SUPER_ADMIN') && !this.authService.currentPoslovnicaId();
  }

  ngOnInit(): void {
    if (this.isSuperAdminBezPoslovnice) {
      this.poslovnicaService.getAll()
        .pipe(
          catchError(err => {
            this.notification.error('Greška pri učitavanju poslovnica.');
            console.error(err);
            return EMPTY;
          }),
          takeUntilDestroyed(this.destroyRef),
        )
        .subscribe(data => {
          this.poslovnice = data;
          this.cdr.markForCheck();
        });
    } else {
      const id = this.authService.currentPoslovnicaId();
      if (id) {
        this.selectedPoslovnicaId = id;
        this.loadData();
      }
    }
  }

  onPoslovnicaChange(id: number): void {
    this.selectedPoslovnicaId = id;
    this.loadData();
  }

  onUpdate(row: any): void {
    const dto: UpdateZalihe = {
      minZaliha: row['minZaliha'] != null ? Number(row['minZaliha']) : null,
      optimalnaZaliha: row['optimalnaZaliha'] != null ? Number(row['optimalnaZaliha']) : null,
    };
    this.isSaving = true;
    this.artikliService.updateZalihe(row['id'], dto)
      .pipe(
        finalize(() => { this.isSaving = false; this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri snimanju zaliha.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.notification.success('Zalihe uspješno snimljene.');
        this.loadData();
      });
  }

  private loadData(): void {
    if (!this.selectedPoslovnicaId) return;
    this.isLoading = true;
    this.artikliService.getZaliheByPoslovnica(this.selectedPoslovnicaId)
      .pipe(
        finalize(() => { this.isLoading = false; this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju zaliha.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(data => {
        this.items = data.slice().sort((a, b) => a.artikalNaziv.localeCompare(b.artikalNaziv, 'bs'));
        this.cdr.markForCheck();
      });
  }
}
