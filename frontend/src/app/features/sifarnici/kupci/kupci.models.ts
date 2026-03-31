export interface Kupac {
  id: number;
  naziv: string;
  adresa?: string;
  grad?: string;
  telefon?: string;
  email?: string;
  pib?: string;
  aktivan: boolean;
}

export interface CreateKupac {
  naziv: string;
  adresa?: string;
  grad?: string;
  telefon?: string;
  email?: string;
  pib?: string;
  aktivan: boolean;
}

export interface UpdateKupac {
  naziv?: string;
  adresa?: string;
  grad?: string;
  telefon?: string;
  email?: string;
  pib?: string;
  aktivan?: boolean;
}
