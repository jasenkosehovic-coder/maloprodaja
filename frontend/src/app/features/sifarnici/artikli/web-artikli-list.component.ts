import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EMPTY, forkJoin } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';

import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CrudTableComponent } from '../../../shared/components/crud-table/crud-table.component';
import { CrudActionsConfig, CrudFieldConfig } from '../../../shared/components/crud-table/crud-field-config';
import { ArtikliService } from './artikli.service';
import { WebArtikliService } from './web-artikli.service';
import {
  ArtikalKompanija,
  WebArtikal,
  SlikaArtikla,
  CreateWebArtikal,
  UpdateWebArtikal,
} from './artikli.models';

@Component({
  selector: 'app-web-artikli-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DragDropModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
    MatTooltipModule,
    CrudTableComponent,
  ],
  templateUrl: './web-artikli-list.component.html',
  styleUrl: './web-artikli-list.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class WebArtikliListComponent implements OnInit {
  private readonly artikliService = inject(ArtikliService);
  private readonly webArtikliService = inject(WebArtikliService);
  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly fb = inject(NonNullableFormBuilder);

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  readonly baseFileUrl = environment.apiUrl.replace('/api', '') + '/slike/';

  // State
  isLoading = false;
  isSaving = signal(false);
  isLoadingSlike = signal(false);
  isUploadingSlika = signal(false);

  artikli: ArtikalKompanija[] = [];
  webArtikli: WebArtikal[] = [];

  selectedArtikal = signal<ArtikalKompanija | null>(null);
  slike = signal<SlikaArtikla[]>([]);
  editId = signal<number | null>(null);

  readonly webForm = this.fb.group({
    webNaziv: ['', Validators.maxLength(255)],
    webOpis: [''],
    mpc: [null as number | null],
    popust: [null as number | null, [Validators.min(0), Validators.max(100)]],
    novaMpc: [null as number | null],
    aktivan: [true],
    metaTitle: ['', Validators.maxLength(160)],
    metaOpis: ['', Validators.maxLength(320)],
  });

  readonly tableHeaders: string[] = ['naziv', 'sifra', 'aktivan'];

  readonly tableActions: CrudActionsConfig = {
    add: false,
    edit: false,
    delete: false,
    export: false,
  };

  readonly fields: CrudFieldConfig[] = [
    { key: 'id',     label: 'ID',     type: 'number',  visible: false },
    { key: 'naziv',  label: 'Naziv',  type: 'text' },
    { key: 'sifra',  label: 'Šifra',  type: 'text' },
    { key: 'aktivan', label: 'Aktivan', type: 'boolean' },
  ];

  readonly selectedHasWebArtikal = computed(() => {
    const a = this.selectedArtikal();
    if (!a) return false;
    return this.webArtikli.some(w => w.idArtikla === a.id);
  });

  ngOnInit(): void {
    this.loadAll();

    this.webForm.controls.popust.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.calcNovaMpc());

    this.webForm.controls.mpc.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.calcNovaMpc());
  }

  private calcNovaMpc(): void {
    const mpc = this.webForm.controls.mpc.value;
    const popust = this.webForm.controls.popust.value;
    if (mpc != null && popust != null) {
      const nova = mpc * (1 - popust / 100);
      const zaokruzena = Math.round(nova / 0.05) * 0.05;
      this.webForm.controls.novaMpc.setValue(
        Math.round(zaokruzena * 100) / 100,
        { emitEvent: false }
      );
    }
  }

  private loadAll(): void {
    this.isLoading = true;
    forkJoin([
      this.artikliService.getAll().pipe(catchError(() => EMPTY)),
      this.webArtikliService.getAll().pipe(catchError(() => EMPTY)),
    ])
      .pipe(
        finalize(() => { this.isLoading = false; this.cdr.markForCheck(); }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(([artikli, webArtikli]) => {
        this.artikli = artikli as ArtikalKompanija[];
        this.webArtikli = webArtikli as WebArtikal[];
        this.cdr.markForCheck();
      });
  }

  private loadWebArtikli(): void {
    this.webArtikliService.getAll()
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri učitavanju web artikala.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.webArtikli = data;
        const selected = this.selectedArtikal();
        if (selected) {
          const existing = data.find(w => w.idArtikla === selected.id) ?? null;
          if (existing) {
            this.editId.set(existing.id);
            this.patchForm(existing);
          }
        }
        this.cdr.markForCheck();
      });
  }

  private loadSlike(idArtikla: number): void {
    this.isLoadingSlike.set(true);
    this.webArtikliService.getSlike(idArtikla)
      .pipe(
        finalize(() => this.isLoadingSlike.set(false)),
        catchError(err => {
          this.notification.error('Greška pri učitavanju slika.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.slike.set([...data].sort((a, b) => a.redosljed - b.redosljed));
      });
  }

  onRowClick(row: ArtikalKompanija): void {
    this.selectedArtikal.set(row);
    const existing = this.webArtikli.find(w => w.idArtikla === row.id) ?? null;
    if (existing) {
      this.editId.set(existing.id);
      this.patchForm(existing);
    } else {
      this.editId.set(null);
      this.webForm.reset({ aktivan: true });
    }
    this.loadSlike(row.id);
  }

  private patchForm(w: WebArtikal): void {
    this.webForm.patchValue({
      webNaziv: w.webNaziv ?? '',
      webOpis: w.webOpis ?? '',
      mpc: w.mpc ?? null,
      popust: w.popust ?? null,
      novaMpc: w.novaMpc ?? null,
      aktivan: w.aktivan,
      metaTitle: w.metaTitle ?? '',
      metaOpis: w.metaOpis ?? '',
    });
  }

  onSubmitWebForm(): void {
    if (this.webForm.invalid || this.isSaving()) return;
    const artikal = this.selectedArtikal();
    if (!artikal) return;

    const v = this.webForm.getRawValue();
    const id = this.editId();

    this.isSaving.set(true);

    const request$ = id != null
      ? this.webArtikliService.update(id, {
          webNaziv: v.webNaziv || undefined,
          webOpis: v.webOpis || undefined,
          aktivan: v.aktivan,
          mpc: v.mpc ?? undefined,
          popust: v.popust ?? undefined,
          novaMpc: v.novaMpc ?? undefined,
          metaTitle: v.metaTitle || undefined,
          metaOpis: v.metaOpis || undefined,
        } as UpdateWebArtikal)
      : this.webArtikliService.create({
          idArtikla: artikal.id,
          webNaziv: v.webNaziv || undefined,
          webOpis: v.webOpis || undefined,
          aktivan: v.aktivan,
          mpc: v.mpc ?? undefined,
          popust: v.popust ?? undefined,
          novaMpc: v.novaMpc ?? undefined,
          metaTitle: v.metaTitle || undefined,
          metaOpis: v.metaOpis || undefined,
        } as CreateWebArtikal);

    request$
      .pipe(
        finalize(() => { this.isSaving.set(false); this.cdr.markForCheck(); }),
        catchError(err => {
          this.notification.error('Greška pri snimanju web artikla.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Web artikal uspješno sačuvan.');
        this.loadWebArtikli();
      });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const artikal = this.selectedArtikal();
    if (!artikal) return;

    this.isUploadingSlika.set(true);
    this.webArtikliService.uploadSlika(artikal.id, file)
      .pipe(
        finalize(() => {
          this.isUploadingSlika.set(false);
          input.value = '';
        }),
        catchError(err => {
          this.notification.error('Greška pri uploadu slike.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(novaSlika => {
        this.slike.update(s => [...s, novaSlika]);
        this.notification.success('Slika uspješno dodana.');
      });
  }

  onTriggerUpload(): void {
    this.fileInput.nativeElement.click();
  }

  onSetNaslovna(slika: SlikaArtikla): void {
    const dto = { redosljed: slika.redosljed, jeNaslovna: true, aktivan: true };
    this.webArtikliService.updateSlika(slika.id, dto)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri postavljanju naslovne slike.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.slike.update(slike =>
          slike.map(s => ({ ...s, jeNaslovna: s.id === slika.id }))
        );
      });
  }

  onDeleteSlika(slika: SlikaArtikla): void {
    this.webArtikliService.deleteSlika(slika.id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri brisanju slike.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.slike.update(s => s.filter(x => x.id !== slika.id));
        this.notification.success('Slika uspješno obrisana.');
      });
  }

  onDropSlika(event: CdkDragDrop<SlikaArtikla[]>): void {
    if (event.previousIndex === event.currentIndex) return;

    const current = [...this.slike()];
    moveItemInArray(current, event.previousIndex, event.currentIndex);
    const reordered = current.map((s, i) => ({ ...s, redosljed: i + 1 }));
    this.slike.set(reordered);

    const reorderPayload = reordered.map(s => ({ id: s.id, redosljed: s.redosljed }));
    this.webArtikliService.reorderSlike(reorderPayload)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri promjeni redosljeda slika.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe();
  }

  getSlikaUrl(slika: SlikaArtikla): string {
    return this.baseFileUrl + slika.putanja;
  }
}
