import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import {
  TipVelicine,
  CreateTipVelicine,
  UpdateTipVelicine,
  Velicina,
  CreateVelicina,
  UpdateVelicina,
} from './tipovi-velicina.models';

@Injectable({ providedIn: 'root' })
export class TipoviVelicinaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/tipovi-velicina`;

  getAll(): Observable<TipVelicine[]> {
    return this.http
      .get<ApiResponse<TipVelicine[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateTipVelicine): Observable<TipVelicine> {
    return this.http
      .post<ApiResponse<TipVelicine>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateTipVelicine): Observable<TipVelicine> {
    return this.http
      .put<ApiResponse<TipVelicine>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }

  getVelicine(tipId: number): Observable<Velicina[]> {
    return this.http
      .get<ApiResponse<Velicina[]>>(`${this.baseUrl}/${tipId}/velicine`)
      .pipe(map(r => r.data));
  }

  createVelicina(tipId: number, dto: CreateVelicina): Observable<Velicina> {
    return this.http
      .post<ApiResponse<Velicina>>(`${this.baseUrl}/${tipId}/velicine`, dto)
      .pipe(map(r => r.data));
  }

  updateVelicina(id: number, dto: UpdateVelicina): Observable<Velicina> {
    return this.http
      .put<ApiResponse<Velicina>>(`${environment.apiUrl}/velicine/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deleteVelicina(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${environment.apiUrl}/velicine/${id}`)
      .pipe(map(() => void 0));
  }
}
