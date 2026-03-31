export interface Dobavljac {
  id: number;
  naziv: string;
  adresa?: string;
  grad?: string;
  telefon?: string;
  email?: string;
  pib?: string;
  aktivan: boolean;
}

export interface CreateDobavljac {
  naziv: string;
  adresa?: string;
  grad?: string;
  telefon?: string;
  email?: string;
  pib?: string;
  aktivan: boolean;
}

export interface UpdateDobavljac {
  naziv?: string;
  adresa?: string;
  grad?: string;
  telefon?: string;
  email?: string;
  pib?: string;
  aktivan?: boolean;
}

export interface DobavljacOption {
  id: number;
  naziv: string;
}
