import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import {
  DefinicijaAtributa,
  CreateDefinicijaAtributa,
  UpdateDefinicijaAtributa,
  VrijednostAtributa,
  CreateVrijednostAtributa,
  UpdateVrijednostAtributa,
} from './definicije-atributa.models';

@Injectable({ providedIn: 'root' })
export class DefinicijeAtributaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/atributi/definicije`;

  getAll(): Observable<DefinicijaAtributa[]> {
    return this.http
      .get<ApiResponse<DefinicijaAtributa[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateDefinicijaAtributa): Observable<DefinicijaAtributa> {
    return this.http
      .post<ApiResponse<DefinicijaAtributa>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateDefinicijaAtributa): Observable<DefinicijaAtributa> {
    return this.http
      .put<ApiResponse<DefinicijaAtributa>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }

  getVrijednosti(definicijaId: number): Observable<VrijednostAtributa[]> {
    return this.http
      .get<ApiResponse<VrijednostAtributa[]>>(`${this.baseUrl}/${definicijaId}/vrijednosti`)
      .pipe(map(r => r.data));
  }

  createVrijednost(definicijaId: number, dto: CreateVrijednostAtributa): Observable<VrijednostAtributa> {
    return this.http
      .post<ApiResponse<VrijednostAtributa>>(`${this.baseUrl}/${definicijaId}/vrijednosti`, dto)
      .pipe(map(r => r.data));
  }

  updateVrijednost(id: number, dto: UpdateVrijednostAtributa): Observable<VrijednostAtributa> {
    return this.http
      .put<ApiResponse<VrijednostAtributa>>(`${environment.apiUrl}/atributi/vrijednosti/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deleteVrijednost(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${environment.apiUrl}/atributi/vrijednosti/${id}`)
      .pipe(map(() => void 0));
  }
}
