import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import { Proizvodjac, CreateProizvodjac, UpdateProizvodjac } from './proizvodjaci.models';

@Injectable({ providedIn: 'root' })
export class ProizvodjaciService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/proizvodjaci`;

  getAll(): Observable<Proizvodjac[]> {
    return this.http
      .get<ApiResponse<Proizvodjac[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateProizvodjac): Observable<Proizvodjac> {
    return this.http
      .post<ApiResponse<Proizvodjac>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateProizvodjac): Observable<Proizvodjac> {
    return this.http
      .put<ApiResponse<Proizvodjac>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }
}
