export type JedinicaMjere = 'KOM' | 'KG' | 'L' | 'M' | 'M2' | 'PAK' | 'SET';
export type TipMarze = 'FIKSNA_MARZA' | 'FIKSNA_CIJENA' | 'SLOBODNA' | 'DIFERENCIRANA';

export interface ArtikalKompanija {
  id: number;
  sifra: string;
  naziv: string;
  jedin: JedinicaMjere;
  pdv: number;
  opis?: string;
  aktivan: boolean;
  idGrupe?: number;
  nazivGrupe?: string;
  idProizvodjaca?: number;
  nazivProizvodjaca?: string;
  idDobavljaca?: number;
  nazivDobavljaca?: string;
}

export interface CreateArtikalKompanija {
  sifra: string;
  naziv: string;
  jedin: JedinicaMjere;
  pdv: number;
  opis?: string;
  aktivan: boolean;
  idGrupe?: number;
  idProizvodjaca?: number;
  idDobavljaca?: number;
}

export interface UpdateArtikalKompanija {
  sifra?: string;
  naziv?: string;
  jedin?: JedinicaMjere;
  pdv?: number;
  opis?: string;
  aktivan?: boolean;
  idGrupe?: number;
  idProizvodjaca?: number;
  idDobavljaca?: number;
}

export interface ArtikalPoslovnica {
  id: number;
  idArtikla: number;
  artikalNaziv: string;
  artikalSifra: string;
  idPoslovnice: number;
  vpc?: number;
  marza?: number;
  tipMarze: TipMarze;
  mpc?: number;
  kolicina: number;
  minZaliha?: number;
  optimalnaZaliha?: number;
  aktivan: boolean;
}

export interface CreateArtikalPoslovnica {
  idArtikla: number;
  idPoslovnice: number;
  vpc?: number;
  marza?: number;
  tipMarze?: TipMarze;
  kolicina?: number;
  minZaliha?: number;
  optimalnaZaliha?: number;
}

export interface UpdateArtikalPoslovnica {
  vpc?: number;
  marza?: number;
  tipMarze?: TipMarze;
  mpc?: number;
  kolicina?: number;
  minZaliha?: number;
  optimalnaZaliha?: number;
  aktivan?: boolean;
}

export interface Barkod {
  id: number;
  barkod: string;
  idArtikla: number;
  artikalNaziv: string;
  artikalSifra: string;
  idPoslovnice?: number;
  poslovnicaNaziv?: string;
  aktivan: boolean;
}

export interface CreateBarkod {
  barkod: string;
  idArtikla: number;
  idPoslovnice?: number | null;
}

export interface UpdateBarkod {
  barkod: string;
  idPoslovnice?: number | null;
  aktivan?: boolean;
}
