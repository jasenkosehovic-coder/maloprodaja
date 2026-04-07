export interface DefinicijaAtributa {
  id: number;
  naziv: string;
  redosljed: number;
  obavezno: boolean;
  zaWeb: boolean;
  aktivan: boolean;
}

export interface CreateDefinicijaAtributa {
  naziv: string;
  redosljed: number;
  obavezno: boolean;
  zaWeb: boolean;
}

export interface UpdateDefinicijaAtributa {
  naziv?: string;
  redosljed?: number;
  obavezno?: boolean;
  zaWeb?: boolean;
  aktivan?: boolean;
}

export interface VrijednostAtributa {
  id: number;
  definicijaId: number;
  vrijednost: string;
  redosljed: number;
  aktivan: boolean;
}

export interface CreateVrijednostAtributa {
  vrijednost: string;
  redosljed: number;
}

export interface UpdateVrijednostAtributa {
  vrijednost?: string;
  redosljed?: number;
  aktivan?: boolean;
}
