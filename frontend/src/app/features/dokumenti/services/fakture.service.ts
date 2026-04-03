import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import {
  UlaznaFakturaListItem,
  UlaznaFakturaDetail,
  CreateFakturaDTO,
  AddFakturaStavkaDTO,
} from '../models/dokumenti.models';

@Injectable({ providedIn: 'root' })
export class FaktureService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dokumenti/fakture`;

  list(godina?: number | null): Observable<UlaznaFakturaListItem[]> {
    const params: Record<string, string> = {};
    if (godina != null) {
      params['godina'] = String(godina);
    }
    return this.http
      .get<ApiResponse<UlaznaFakturaListItem[]>>(this.baseUrl, { params })
      .pipe(map(r => r.data));
  }

  findById(id: number): Observable<UlaznaFakturaDetail> {
    return this.http
      .get<ApiResponse<UlaznaFakturaDetail>>(`${this.baseUrl}/${id}`)
      .pipe(map(r => r.data));
  }

  create(dto: CreateFakturaDTO): Observable<UlaznaFakturaDetail> {
    return this.http
      .post<ApiResponse<UlaznaFakturaDetail>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: Partial<CreateFakturaDTO>): Observable<UlaznaFakturaDetail> {
    return this.http
      .put<ApiResponse<UlaznaFakturaDetail>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  potvrdi(id: number): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/${id}/potvrdi`, {})
      .pipe(map(() => void 0));
  }

  storno(id: number): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/${id}/storno`, {})
      .pipe(map(() => void 0));
  }

  addStavka(fakturaId: number, dto: AddFakturaStavkaDTO): Observable<UlaznaFakturaDetail> {
    return this.http
      .post<ApiResponse<UlaznaFakturaDetail>>(`${this.baseUrl}/${fakturaId}/stavke`, dto)
      .pipe(map(r => r.data));
  }

  removeStavka(fakturaId: number, stavkaId: number): Observable<UlaznaFakturaDetail> {
    return this.http
      .delete<ApiResponse<UlaznaFakturaDetail>>(`${this.baseUrl}/${fakturaId}/stavke/${stavkaId}`)
      .pipe(map(r => r.data));
  }

  delete(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }

  downloadPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/pdf`, { responseType: 'blob' });
  }
}
