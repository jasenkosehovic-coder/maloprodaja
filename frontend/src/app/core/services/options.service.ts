import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';
import { CrudFieldOption } from '../../shared/components/crud-table/crud-field-config';

export interface AppOptions {
  uloge: CrudFieldOption[];
  jediniceMjere: CrudFieldOption[];
  tipoviMarze: CrudFieldOption[];
}

@Injectable({ providedIn: 'root' })
export class OptionsService {
  private readonly http = inject(HttpClient);
  private readonly options$ = this.http
    .get<AppOptions>('/assets/options.json')
    .pipe(shareReplay(1));

  getOptions(): Observable<AppOptions> {
    return this.options$;
  }
}
