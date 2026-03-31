import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import {
  ArtikalKompanija, CreateArtikalKompanija, UpdateArtikalKompanija,
  ArtikalPoslovnica, CreateArtikalPoslovnica, UpdateArtikalPoslovnica,
  Barkod, CreateBarkod, UpdateBarkod,
} from './artikli.models';

@Injectable({ providedIn: 'root' })
export class ArtikliService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/artikli`;

  getAll(): Observable<ArtikalKompanija[]> {
    return this.http
      .get<ApiResponse<ArtikalKompanija[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateArtikalKompanija): Observable<ArtikalKompanija> {
    return this.http
      .post<ApiResponse<ArtikalKompanija>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateArtikalKompanija): Observable<ArtikalKompanija> {
    return this.http
      .put<ApiResponse<ArtikalKompanija>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }

  // ---- Artikli u poslovnici ----

  getAllByPoslovnica(idPoslovnice: number): Observable<ArtikalPoslovnica[]> {
    return this.http
      .get<ApiResponse<ArtikalPoslovnica[]>>(`${this.baseUrl}-poslovnice`, {
        params: { idPoslovnice: idPoslovnice.toString() },
      })
      .pipe(map(r => r.data));
  }

  createPoslovnica(dto: CreateArtikalPoslovnica): Observable<ArtikalPoslovnica> {
    return this.http
      .post<ApiResponse<ArtikalPoslovnica>>(`${this.baseUrl}-poslovnice`, dto)
      .pipe(map(r => r.data));
  }

  updatePoslovnica(id: number, dto: UpdateArtikalPoslovnica): Observable<ArtikalPoslovnica> {
    return this.http
      .put<ApiResponse<ArtikalPoslovnica>>(`${this.baseUrl}-poslovnice/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivatePoslovnica(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}-poslovnice/${id}`)
      .pipe(map(() => void 0));
  }

  // ---- Barkodovi ----

  getAllBarkodovi(): Observable<Barkod[]> {
    return this.http
      .get<ApiResponse<Barkod[]>>(`${environment.apiUrl}/barkodovi`)
      .pipe(map(r => r.data));
  }

  createBarkod(dto: CreateBarkod): Observable<Barkod> {
    return this.http
      .post<ApiResponse<Barkod>>(`${environment.apiUrl}/barkodovi`, dto)
      .pipe(map(r => r.data));
  }

  updateBarkod(id: number, dto: UpdateBarkod): Observable<Barkod> {
    return this.http
      .put<ApiResponse<Barkod>>(`${environment.apiUrl}/barkodovi/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivateBarkod(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${environment.apiUrl}/barkodovi/${id}`)
      .pipe(map(() => void 0));
  }
}
