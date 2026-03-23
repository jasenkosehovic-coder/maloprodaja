export interface LoginRequest {
  username: string;
  password: string;
  poslovnicaId?: number;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  korisnik: KorisnikInfo;
}

export interface KorisnikInfo {
  id: number;
  username: string;
  ime: string;
  prezime: string;
  email: string;
  uloga: Uloga;
  poslovnicaId: number;
  poslovnicaNaziv: string;
  aktivan: boolean;
}

export type Uloga =
  | 'SUPER_ADMIN'
  | 'ADMIN'
  | 'MENADZER'
  | 'BLAGAJNIK'
  | 'SKLADISTAR';

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface TokenPayload {
  sub: string;
  iat: number;
  exp: number;
  uloga: Uloga;
  poslovnicaId: number;
}
