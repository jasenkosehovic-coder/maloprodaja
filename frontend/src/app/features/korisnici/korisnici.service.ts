import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/models/api-response.model';
import { CreateKorisnik, KorisnikListItem, UpdateKorisnik } from './korisnici.models';

@Injectable({ providedIn: 'root' })
export class KorisniciService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/korisnici`;

  getAll(): Observable<KorisnikListItem[]> {
    return this.http
      .get<ApiResponse<KorisnikListItem[]>>(this.baseUrl)
      .pipe(map(r => r.data));
  }

  create(dto: CreateKorisnik): Observable<KorisnikListItem> {
    return this.http
      .post<ApiResponse<KorisnikListItem>>(this.baseUrl, dto)
      .pipe(map(r => r.data));
  }

  update(id: number, dto: UpdateKorisnik): Observable<KorisnikListItem> {
    return this.http
      .put<ApiResponse<KorisnikListItem>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map(r => r.data));
  }

  deactivate(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map(() => void 0));
  }

  getAvailableIzbornici(): Observable<string[]> {
    return this.http
      .get<ApiResponse<string[]>>(`${this.baseUrl}/izbornici/dostupni`)
      .pipe(map(r => r.data));
  }

  updateIzbornici(id: number, izbornici: string[]): Observable<void> {
    const payload = izbornici.map(key => ({ izbornikKljuc: key, aktivan: true }));
    return this.http
      .put<ApiResponse<any>>(`${this.baseUrl}/${id}/izbornici`, payload)
      .pipe(map(() => void 0));
  }
}
