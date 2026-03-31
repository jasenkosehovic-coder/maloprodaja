import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import {
  NivelacijaListItem,
  NivelacijaDetail,
  CreateNivelacijaRucnaDTO,
} from '../models/dokumenti.models';

@Injectable({ providedIn: 'root' })
export class NivelacijeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dokumenti/nivelacije`;

  list(): Observable<NivelacijaListItem[]> {
    return this.http
      .get<ApiResponse<NivelacijaListItem[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  findById(id: number): Observable<NivelacijaDetail> {
    return this.http
      .get<ApiResponse<NivelacijaDetail>>(`${this.baseUrl}/${id}`)
      .pipe(map(r => r.data));
  }

  kreirajRucnu(dto: CreateNivelacijaRucnaDTO): Observable<NivelacijaDetail> {
    return this.http
      .post<ApiResponse<NivelacijaDetail>>(`${this.baseUrl}/rucna`, dto)
      .pipe(map(r => r.data));
  }

  downloadPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/pdf`, { responseType: 'blob' });
  }
}
