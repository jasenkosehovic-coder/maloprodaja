export interface Boja {
  id: number;
  naziv: string;
  hexKod: string | null;
  aktivan: boolean;
}

export interface CreateBoja {
  naziv: string;
  hexKod?: string | null;
}

export interface UpdateBoja {
  naziv?: string;
  hexKod?: string | null;
  aktivan?: boolean;
}
