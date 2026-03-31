export interface Proizvodjac {
  id: number;
  naziv: string;
  drzava?: string;
  kontaktOsoba?: string;
  telefon?: string;
  email?: string;
  aktivan: boolean;
}

export interface CreateProizvodjac {
  naziv: string;
  drzava?: string;
  kontaktOsoba?: string;
  telefon?: string;
  email?: string;
  aktivan: boolean;
}

export interface UpdateProizvodjac {
  naziv?: string;
  drzava?: string;
  kontaktOsoba?: string;
  telefon?: string;
  email?: string;
  aktivan?: boolean;
}

export interface ProizvodjacOption {
  id: number;
  naziv: string;
}
