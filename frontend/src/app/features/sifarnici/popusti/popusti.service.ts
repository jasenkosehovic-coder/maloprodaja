import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import { Popust, CreatePopust, UpdatePopust } from './popusti.models';

@Injectable({ providedIn: 'root' })
export class PopustiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/popusti`;

  getAll(): Observable<Popust[]> {
    return this.http
      .get<ApiResponse<Popust[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreatePopust): Observable<Popust> {
    return this.http
      .post<ApiResponse<Popust>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdatePopust): Observable<Popust> {
    return this.http
      .put<ApiResponse<Popust>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }
}
