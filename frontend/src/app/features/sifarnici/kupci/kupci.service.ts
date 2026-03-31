import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import { Kupac, CreateKupac, UpdateKupac } from './kupci.models';

@Injectable({ providedIn: 'root' })
export class KupciService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/kupci`;

  getAll(): Observable<Kupac[]> {
    return this.http
      .get<ApiResponse<Kupac[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateKupac): Observable<Kupac> {
    return this.http
      .post<ApiResponse<Kupac>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateKupac): Observable<Kupac> {
    return this.http
      .put<ApiResponse<Kupac>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }
}
