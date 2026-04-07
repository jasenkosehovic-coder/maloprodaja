export interface TipVelicine {
  id: number;
  naziv: string;
  opis?: string;
  aktivan: boolean;
}

export interface CreateTipVelicine {
  naziv: string;
  opis?: string;
  aktivan: boolean;
}

export interface UpdateTipVelicine {
  naziv?: string;
  opis?: string;
  aktivan?: boolean;
}

export interface Velicina {
  id: number;
  tipVelicineId: number;
  oznaka: string;
  redosljed: number;
  aktivan: boolean;
}

export interface CreateVelicina {
  oznaka: string;
  redosljed: number;
}

export interface UpdateVelicina {
  oznaka?: string;
  redosljed?: number;
  aktivan?: boolean;
}
