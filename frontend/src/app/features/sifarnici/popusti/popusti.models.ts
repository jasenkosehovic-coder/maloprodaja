export interface Popust {
  id: number;
  naziv: string;
  procenat: number;
  datumOd?: string;
  datumDo?: string;
  aktivan: boolean;
  idPoslovnice?: number;
  poslovnicaNaziv?: string;
}

export interface CreatePopust {
  naziv: string;
  procenat: number;
  datumOd?: string;
  datumDo?: string;
  aktivan: boolean;
  idPoslovnice?: number;
}

export interface UpdatePopust {
  naziv?: string;
  procenat?: number;
  datumOd?: string;
  datumDo?: string;
  aktivan?: boolean;
  idPoslovnice?: number | null;
}
