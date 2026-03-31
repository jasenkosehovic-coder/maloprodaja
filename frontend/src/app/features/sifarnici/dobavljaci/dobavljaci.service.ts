import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import { Dobavljac, CreateDobavljac, UpdateDobavljac } from './dobavljaci.models';

@Injectable({ providedIn: 'root' })
export class DobavljaciService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dobavljaci`;

  getAll(): Observable<Dobavljac[]> {
    return this.http
      .get<ApiResponse<Dobavljac[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateDobavljac): Observable<Dobavljac> {
    return this.http
      .post<ApiResponse<Dobavljac>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateDobavljac): Observable<Dobavljac> {
    return this.http
      .put<ApiResponse<Dobavljac>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }
}
