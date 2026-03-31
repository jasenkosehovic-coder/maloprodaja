export interface GrupaArtikala {
  id: number;
  naziv: string;
  opis?: string;
  aktivan: boolean;
}

export interface CreateGrupaArtikala {
  naziv: string;
  opis?: string;
  aktivan: boolean;
}

export interface UpdateGrupaArtikala {
  naziv?: string;
  opis?: string;
  aktivan?: boolean;
}

export interface GrupaOption {
  id: number;
  naziv: string;
}
