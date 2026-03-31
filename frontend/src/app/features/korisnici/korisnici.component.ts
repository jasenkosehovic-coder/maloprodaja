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
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EMPTY, forkJoin, of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';

import { CrudTableComponent } from '../../shared/components/crud-table/crud-table.component';
import { CrudActionsConfig, CrudFieldConfig, CrudPdfHeader } from '../../shared/components/crud-table/crud-field-config';
import { AuthService } from '../../core/auth/services/auth.service';
import { KorisniciService } from './korisnici.service';
import { NotificationService } from '../../core/services/notification.service';
import { OptionsService, AppOptions } from '../../core/services/options.service';
import { KorisnikListItem, CreateKorisnik, UpdateKorisnik } from './korisnici.models';
import { PoslovnicaService, PoslovnicaOption } from './poslovnica.service';

@Component({
  selector: 'app-korisnici',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, CrudTableComponent],
  templateUrl: './korisnici.component.html',
  styleUrl: './korisnici.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class KorisniciComponent implements OnInit {
  private readonly korisniciService = inject(KorisniciService);
  private readonly poslovnicaService = inject(PoslovnicaService);
  private readonly optionsService = inject(OptionsService);
  private readonly notification = inject(NotificationService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

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

  korisnici: KorisnikListItem[] = [];
  poslovnice: PoslovnicaOption[] = [];
  isLoading = false;

  readonly tableHeaders: string[] = [
    'username', 'ime', 'prezime', 'email', 'uloga', 'poslovnicaId', 'aktivan', 'izbornici',
  ];

  readonly tableActions: CrudActionsConfig = {
    add: true,
    edit: true,
    delete: true,
    export: true,
  };

  fields: CrudFieldConfig[] = [];

  private buildFields(poslovnice: PoslovnicaOption[], availableIzbornici: string[], options: AppOptions): CrudFieldConfig[] {
    return [
      {
        key: 'id',
        label: 'ID',
        type: 'number',
        visible: false,
      },
      {
        key: 'username',
        label: 'Korisničko ime',
        type: 'text',
        required: true,
        readOnlyOnEdit: true,
        requiredMessage: 'Korisničko ime je obavezno.',
      },
      {
        key: 'password',
        label: 'Lozinka',
        type: 'password',
        requiredOnAdd: true,
        requiredMessage: 'Lozinka je obavezna.',
        pattern: '^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{6,}$',
        patternMessage: 'Lozinka mora imati min. 6 znakova, malo i veliko slovo, broj i simbol.',
      },
      {
        key: 'ime',
        label: 'Ime',
        type: 'text',
        required: true,
        requiredMessage: 'Ime je obavezno.',
      },
      {
        key: 'prezime',
        label: 'Prezime',
        type: 'text',
        required: true,
        requiredMessage: 'Prezime je obavezno.',
      },
      {
        key: 'email',
        label: 'E-mail',
        type: 'email',
        required: true,
        requiredMessage: 'E-mail je obavezan.',
        pattern: '^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$',
        patternMessage: 'Unesite ispravnu email adresu.',
      },
      {
        key: 'uloga',
        label: 'Uloga',
        type: 'select',
        required: true,
        requiredMessage: 'Uloga je obavezna.',
        options: options.uloge,
      },
      {
        key: 'poslovnicaId',
        label: 'Poslovnica',
        type: 'select',
        options: poslovnice.map(p => ({ label: p.naziv, value: p.id })),
        required: true,
        requiredMessage: 'Poslovnica je obavezna.',
      },
      {
        key: 'aktivan',
        label: 'Aktivan',
        type: 'boolean',
        defaultValue: true,
      },
      {
        key: 'izbornici',
        label: 'Prava pristupa',
        type: 'multiselect',
        required: true,
        requiredMessage: 'Prava pristupa su obavezna.',
        options: availableIzbornici.map(k => ({ label: k, value: k })),
      },
    ];
  }

  ngOnInit(): void {
    forkJoin([
      this.poslovnicaService.getAll().pipe(catchError(() => of([] as PoslovnicaOption[]))),
      this.korisniciService.getAvailableIzbornici().pipe(catchError(() => of([] as string[]))),
      this.optionsService.getOptions(),
    ]).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(([poslovnice, izbornici, options]) => {
        this.poslovnice = poslovnice;
        this.fields = this.buildFields(poslovnice, izbornici, options);
        this.loadKorisnici();
      });
  }

  private loadKorisnici(): void {
    this.isLoading = true;
    this.korisniciService.getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.markForCheck();
        }),
        catchError(err => {
          this.notification.error('Greška pri učitavanju korisnika.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => {
        this.korisnici = data;
        this.cdr.markForCheck();
      });
  }

  onCreate(row: any): void {
    const dto: CreateKorisnik = {
      username: row['username'],
      password: row['password'],
      ime: row['ime'],
      prezime: row['prezime'],
      email: row['email'],
      uloga: row['uloga'],
      poslovnicaId: row['poslovnicaId'] || undefined,
    };
    const izbornici: string[] = row['izbornici'] ?? [];

    this.korisniciService.create(dto)
      .pipe(
        switchMap(created =>
          izbornici.length > 0
            ? this.korisniciService.updateIzbornici(created.id, izbornici)
            : of(void 0)
        ),
        catchError(err => {
          this.notification.error('Greška pri kreiranju korisnika.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Korisnik uspješno kreiran.');
        this.loadKorisnici();
      });
  }

  onUpdate(row: any): void {
    const id: number = row['id'];
    const dto: UpdateKorisnik = {
      password: row['password'] || undefined,
      ime: row['ime'],
      prezime: row['prezime'],
      email: row['email'],
      uloga: row['uloga'],
      poslovnicaId: row['poslovnicaId'] || undefined,
      aktivan: row['aktivan'] !== undefined ? row['aktivan'] : false,
    };
    const izbornici: string[] = row['izbornici'] ?? [];

    forkJoin([
      this.korisniciService.update(id, dto),
      this.korisniciService.updateIzbornici(id, izbornici),
    ]).pipe(
      catchError(err => {
        this.notification.error('Greška pri ažuriranju korisnika.');
        console.error(err);
        return EMPTY;
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => {
      this.notification.success('Korisnik uspješno ažuriran.');
      this.loadKorisnici();
    });
  }

  onDeactivate(row: any): void {
    const id: number = row['id'];

    this.korisniciService.deactivate(id)
      .pipe(
        catchError(err => {
          this.notification.error('Greška pri deaktivaciji korisnika.');
          console.error(err);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.notification.success('Korisnik uspješno deaktiviran.');
        this.loadKorisnici();
      });
  }

  onDeactivateMany(rows: any[]): void {
    if (rows.length === 0) return;

    const requests = rows.map(row =>
      this.korisniciService.deactivate(row['id']).pipe(
        catchError(err => {
          console.error(`Greška pri deaktivaciji korisnika ${row['id']}:`, err);
          return EMPTY;
        })
      )
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.notification.success(`${rows.length} korisnik(a) uspješno deaktivirano.`);
        this.loadKorisnici();
      });
  }
}
