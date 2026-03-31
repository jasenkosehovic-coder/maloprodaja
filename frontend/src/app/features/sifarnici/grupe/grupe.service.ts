import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import { GrupaArtikala, CreateGrupaArtikala, UpdateGrupaArtikala } from './grupe.models';

@Injectable({ providedIn: 'root' })
export class GrupeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/grupe-artikala`;

  getAll(): Observable<GrupaArtikala[]> {
    return this.http
      .get<ApiResponse<GrupaArtikala[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateGrupaArtikala): Observable<GrupaArtikala> {
    return this.http
      .post<ApiResponse<GrupaArtikala>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateGrupaArtikala): Observable<GrupaArtikala> {
    return this.http
      .put<ApiResponse<GrupaArtikala>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }
}
