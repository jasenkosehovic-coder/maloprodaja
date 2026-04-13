import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import {
  WebArtikal,
  CreateWebArtikal,
  UpdateWebArtikal,
  SlikaArtikla,
} from './artikli.models';

@Injectable({ providedIn: 'root' })
export class WebArtikliService {
  private readonly http = inject(HttpClient);
  private readonly webArtikliUrl = `${environment.apiUrl}/web-artikli`;
  private readonly artikliUrl = `${environment.apiUrl}/artikli`;
  private readonly slikeUrl = `${environment.apiUrl}/slike`;

  getAll(): Observable<WebArtikal[]> {
    return this.http
      .get<ApiResponse<WebArtikal[]>>(this.webArtikliUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateWebArtikal): Observable<WebArtikal> {
    return this.http
      .post<ApiResponse<WebArtikal>>(this.webArtikliUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateWebArtikal): Observable<WebArtikal> {
    return this.http
      .put<ApiResponse<WebArtikal>>(`${this.webArtikliUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  delete(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.webArtikliUrl}/${id}`)
      .pipe(map(() => void 0));
  }

  // ---- Slike ----

  getSlike(idArtikla: number): Observable<SlikaArtikla[]> {
    return this.http
      .get<ApiResponse<SlikaArtikla[]>>(`${this.artikliUrl}/${idArtikla}/slike`)
      .pipe(map(r => r.data));
  }

  uploadSlika(idArtikla: number, file: File): Observable<SlikaArtikla> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<ApiResponse<SlikaArtikla>>(`${this.artikliUrl}/${idArtikla}/slike`, formData)
      .pipe(map(r => r.data));
  }

  updateSlika(id: number, dto: { redosljed: number; jeNaslovna: boolean; aktivan: boolean }): Observable<SlikaArtikla> {
    return this.http
      .put<ApiResponse<SlikaArtikla>>(`${this.slikeUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deleteSlika(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.slikeUrl}/${id}`)
      .pipe(map(() => void 0));
  }

  reorderSlike(items: { id: number; redosljed: number }[]): Observable<void> {
    return this.http
      .put<ApiResponse<void>>(`${this.slikeUrl}/reorder`, items)
      .pipe(map(() => void 0));
  }
}
