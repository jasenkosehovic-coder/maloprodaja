import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import {
  OtpremnicaListItem,
  OtpremnicaDetail,
  CreateOtpremnicaDTO,
} from '../models/dokumenti.models';

@Injectable({ providedIn: 'root' })
export class OtpremnicaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dokumenti/otpremnice`;

  list(): Observable<OtpremnicaListItem[]> {
    return this.http
      .get<ApiResponse<OtpremnicaListItem[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  findById(id: number): Observable<OtpremnicaDetail> {
    return this.http
      .get<ApiResponse<OtpremnicaDetail>>(`${this.baseUrl}/${id}`)
      .pipe(map(r => r.data));
  }

  create(dto: CreateOtpremnicaDTO): Observable<OtpremnicaDetail> {
    return this.http
      .post<ApiResponse<OtpremnicaDetail>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  posalji(id: number): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/${id}/posalji`, {})
      .pipe(map(() => void 0));
  }

  potvrdiPrijem(id: number, napomena?: string): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/${id}/potvrdi-prijem`, { napomena: napomena ?? null })
      .pipe(map(() => void 0));
  }

  storno(id: number): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/${id}/storno`, {})
      .pipe(map(() => void 0));
  }

  downloadPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/pdf`, { responseType: 'blob' });
  }
}
