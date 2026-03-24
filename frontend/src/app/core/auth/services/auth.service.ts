import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { map, Observable, tap } from 'rxjs';

import { environment } from '../../../../environments/environment';
import {
  KorisnikInfo,
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  Uloga,
} from '../models/auth.models';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

const ACCESS_TOKEN_KEY = 'mp_access_token';
const REFRESH_TOKEN_KEY = 'mp_refresh_token';
const KORISNIK_KEY = 'mp_korisnik';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _korisnik = signal<KorisnikInfo | null>(
    this.loadKorisnikFromStorage()
  );
  private readonly _accessToken = signal<string | null>(
    localStorage.getItem(ACCESS_TOKEN_KEY)
  );

  readonly korisnik = this._korisnik.asReadonly();
  readonly accessToken = this._accessToken.asReadonly();

  readonly isLoggedIn = computed(() => this._korisnik() !== null && this._accessToken() !== null);
  readonly currentUloga = computed(() => this._korisnik()?.uloga ?? null);
  readonly currentPoslovnicaId = computed(() => this._korisnik()?.poslovnicaId ?? null);
  readonly displayName = computed(() => {
    const k = this._korisnik();
    return k ? `${k.ime} ${k.prezime}` : '';
  });

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<ApiResponse<LoginResponse>>(`${environment.apiUrl}/auth/login`, request)
      .pipe(
        map((r) => r.data),
        tap((response) => this.persistSession(response))
      );
  }

  refreshToken(): Observable<LoginResponse> {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    const body: RefreshTokenRequest = { refreshToken: refreshToken ?? '' };
    return this.http
      .post<ApiResponse<LoginResponse>>(`${environment.apiUrl}/auth/refresh`, body)
      .pipe(
        map((r) => r.data),
        tap((response) => this.persistSession(response))
      );
  }

  logout(): void {
    this.clearSession();
    void this.router.navigate(['/login']);
  }

  hasRole(...roles: Uloga[]): boolean {
    const uloga = this.currentUloga();
    return uloga !== null && roles.includes(uloga);
  }

  private persistSession(response: LoginResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(KORISNIK_KEY, JSON.stringify(response.korisnik));

    this._accessToken.set(response.accessToken);
    this._korisnik.set(response.korisnik);
  }

  private clearSession(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(KORISNIK_KEY);

    this._accessToken.set(null);
    this._korisnik.set(null);
  }

  private loadKorisnikFromStorage(): KorisnikInfo | null {
    const raw = localStorage.getItem(KORISNIK_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as KorisnikInfo;
    } catch {
      return null;
    }
  }
}
