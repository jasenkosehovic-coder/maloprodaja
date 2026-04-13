import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import { Boja, CreateBoja, UpdateBoja } from './boje.models';

@Injectable({ providedIn: 'root' })
export class BojeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/boje`;

  getAll(): Observable<Boja[]> {
    return this.http
      .get<ApiResponse<Boja[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  getAktivne(): Observable<Boja[]> {
    return this.http
      .get<ApiResponse<Boja[]>>(`${this.baseUrl}/aktivne`)
      .pipe(map(r => r.data));
  }

  create(dto: CreateBoja): Observable<Boja> {
    return this.http
      .post<ApiResponse<Boja>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateBoja): Observable<Boja> {
    return this.http
      .put<ApiResponse<Boja>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }
}
