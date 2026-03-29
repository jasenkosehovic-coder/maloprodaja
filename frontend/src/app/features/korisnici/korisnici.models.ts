import { Uloga } from '../../core/auth/models/auth.models';

export interface KorisnikListItem {
  id: number;
  username: string;
  ime: string;
  prezime: string;
  email: string;
  uloga: Uloga;
  poslovnicaId: number;
  aktivan: boolean;
  izbornici: string[];
}

export interface CreateKorisnik {
  username: string;
  password: string;
  ime?: string;
  prezime?: string;
  email?: string;
  uloga: Uloga;
  poslovnicaId?: number;
}

export interface UpdateKorisnik {
  password?: string;
  ime?: string;
  prezime?: string;
  email?: string;
  uloga?: Uloga;
  poslovnicaId?: number;
  aktivan?: boolean;
}
