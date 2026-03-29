import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/models/api-response.model';

export interface PoslovnicaOption {
  id: number;
  naziv: string;
}

@Injectable({ providedIn: 'root' })
export class PoslovnicaService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<PoslovnicaOption[]> {
    return this.http
      .get<ApiResponse<PoslovnicaOption[]>>(`${environment.apiUrl}/poslovnice`)
      .pipe(map(r => r.data));
  }
}
