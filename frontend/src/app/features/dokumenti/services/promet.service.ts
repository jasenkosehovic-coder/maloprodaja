import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import {
  TipDokumenta,
  DokumentListItem,
  DokumentPage,
  DokumentDetail,
  CreateDokumentDTO,
  UpdateDokumentDTO,
  CreateStavkaDTO,
  UpdateStavkaDTO,
  CreateMedjuskladisnicaDTO,
  DokumentFilterParams,
} from '../models/promet.models';

@Injectable({ providedIn: 'root' })
export class PrometService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}`;

  // ===== Tipovi dokumenata =====

  getTipoviDokumenata(): Observable<TipDokumenta[]> {
    return this.http
      .get<ApiResponse<TipDokumenta[]>>(`${this.baseUrl}/tipovi-dokumenata`)
      .pipe(map(r => r.data));
  }

  // ===== Dokumenti — lista =====

  list(params: DokumentFilterParams): Observable<DokumentListItem[]> {
    let httpParams = new HttpParams().set('tipKod', params.tipKod);

    if (params.poslovnicaId != null) {
      httpParams = httpParams.set('poslovnicaId', String(params.poslovnicaId));
    }
    if (params.status) {
      httpParams = httpParams.set('status', params.status);
    }
    if (params.datumOd) {
      httpParams = httpParams.set('datumOd', params.datumOd);
    }
    if (params.datumDo) {
      httpParams = httpParams.set('datumDo', params.datumDo);
    }
    if (params.page != null) {
      httpParams = httpParams.set('page', String(params.page));
    }
    if (params.size != null) {
      httpParams = httpParams.set('size', String(params.size));
    }

    return this.http
      .get<ApiResponse<DokumentPage>>(`${this.baseUrl}/dokumenti`, { params: httpParams })
      .pipe(map(r => r.data.content));
  }

  // ===== Dokument — detalj =====

  findById(id: number): Observable<DokumentDetail> {
    return this.http
      .get<ApiResponse<DokumentDetail>>(`${this.baseUrl}/dokumenti/${id}`)
      .pipe(map(r => r.data));
  }

  // ===== Kreiranje dokumenta =====

  create(dto: CreateDokumentDTO): Observable<DokumentDetail> {
    return this.http
      .post<ApiResponse<DokumentDetail>>(`${this.baseUrl}/dokumenti`, dto)
      .pipe(map(r => r.data));
  }

  // ===== Ažuriranje headera =====

  update(id: number, dto: UpdateDokumentDTO): Observable<DokumentDetail> {
    return this.http
      .put<ApiResponse<DokumentDetail>>(`${this.baseUrl}/dokumenti/${id}`, dto)
      .pipe(map(r => r.data));
  }

  // ===== Potvrdi =====

  potvrdi(id: number): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/dokumenti/${id}/potvrdi`, {})
      .pipe(map(() => void 0));
  }

  // ===== Storniraj =====

  storniraj(id: number): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/dokumenti/${id}/storniraj`, {})
      .pipe(map(() => void 0));
  }

  // ===== Stavke — dodaj =====

  addStavka(dokumentId: number, dto: CreateStavkaDTO): Observable<DokumentDetail> {
    return this.http
      .post<ApiResponse<DokumentDetail>>(`${this.baseUrl}/dokumenti/${dokumentId}/stavke`, dto)
      .pipe(map(r => r.data));
  }

  // ===== Stavke — ažuriraj =====

  updateStavka(stavkaId: number, dto: UpdateStavkaDTO): Observable<DokumentDetail> {
    return this.http
      .put<ApiResponse<DokumentDetail>>(`${this.baseUrl}/stavke-dokumenata/${stavkaId}`, dto)
      .pipe(map(r => r.data));
  }

  // ===== Stavke — ukloni =====

  deleteStavka(stavkaId: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/stavke-dokumenata/${stavkaId}`)
      .pipe(map(() => void 0));
  }

  // ===== Međuskladišnica (atomički kreiranje i potvrda) =====

  createMedjuskladisnica(dto: CreateMedjuskladisnicaDTO): Observable<DokumentDetail> {
    return this.http
      .post<ApiResponse<DokumentDetail>>(`${this.baseUrl}/dokumenti/medjuskladisnica`, dto)
      .pipe(map(r => r.data));
  }

  // ===== PDF =====

  downloadPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/dokumenti/${id}/pdf`, { responseType: 'blob' });
  }

  downloadFakturaPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/dokumenti/fakture/${id}/pdf`, { responseType: 'blob' });
  }

  downloadPovratPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/dokumenti/povrat-dobavljacu/${id}/pdf`, { responseType: 'blob' });
  }
}
