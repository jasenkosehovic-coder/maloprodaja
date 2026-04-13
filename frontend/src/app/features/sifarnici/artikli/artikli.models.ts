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
  idTipaVelicina?: number;
  nazivTipaVelicina?: string;
  atributi?: Record<number, number>;
  popustProcenat?: number;
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
  idTipaVelicina?: number | null;
  popustProcenat?: number;
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
  idTipaVelicina?: number | null;
  popustProcenat?: number;
}

export interface ArtikalAtributVrijednost {
  idDefinicije: number;
  nazivDefinicije: string;
  idVrijednosti?: number;
  vrijednost?: string;
}

export interface SaveArtikalAtributiRequest {
  atributi: SaveArtikalAtributItem[];
}

export interface SaveArtikalAtributItem {
  idDefinicije: number;
  idVrijednosti: number;
}

export interface VarijantaBarkod {
  id: number;
  barkod: string;
  aktivan: boolean;
}

export interface ArtikalVarijanta {
  id: number;
  idArtikla: number;
  idVelicine: number | null;
  oznakaVelicine: string | null;
  idBoje: number | null;
  nazivBoje: string | null;
  hexKodBoje: string | null;
  nazivVarijante: string;
  barkodovi: VarijantaBarkod[];
  aktivan: boolean;
}

export interface CreateVarijanta {
  idVelicine: number | null;
  idBoje: number | null;
}

export interface CreateVarijantaBarkod {
  barkod: string;
  idVarijante: number;
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
  ukupnaKolicina: number;
  aktivan: boolean;
  popustProcenat?: number;
}

export interface CreateArtikalPoslovnica {
  idArtikla: number;
  idPoslovnice: number;
  vpc?: number;
  marza?: number;
  tipMarze?: TipMarze;
  popustProcenat?: number;
}

export interface UpdateArtikalPoslovnica {
  vpc?: number;
  marza?: number;
  tipMarze?: TipMarze;
  mpc?: number;
  aktivan?: boolean;
  popustProcenat?: number;
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

export interface UpdateBarkodAktivan {
  aktivan: boolean;
}

export interface BatchPopustUpdate {
  id: number;
  popustProcenat: number;
}

// ---- Web artikli ----

export interface WebArtikal {
  id: number;
  idArtikla: number;
  artikalNaziv: string;
  artikalSifra: string;
  webNaziv?: string;
  webOpis?: string;
  aktivan: boolean;
  mpc?: number;
  popust?: number;
  novaMpc?: number;
  metaTitle?: string;
  metaOpis?: string;
}

export interface CreateWebArtikal {
  idArtikla: number;
  webNaziv?: string;
  webOpis?: string;
  aktivan: boolean;
  mpc?: number;
  popust?: number;
  novaMpc?: number;
  metaTitle?: string;
  metaOpis?: string;
}

export interface UpdateWebArtikal {
  webNaziv?: string;
  webOpis?: string;
  aktivan?: boolean;
  mpc?: number;
  popust?: number;
  novaMpc?: number;
  metaTitle?: string;
  metaOpis?: string;
}

export interface SlikaArtikla {
  id: number;
  idArtikla: number;
  putanja: string;
  redosljed: number;
  jeNaslovna: boolean;
  aktivan: boolean;
}

// ---- Stanje zaliha po varijantama ----

export interface StanjePoslovnice {
  id: number;
  idPoslovnice: number;
  nazivPoslovnice: string;
  kolicina: number;
  minZaliha: number | null;
  optimalnaZaliha: number | null;
}

export interface StanjeVarijante {
  varijantaId: number;
  oznakaVelicine: string;
  poslovnice: StanjePoslovnice[];
}

export interface UpdateZalihe {
  minZaliha: number | null;
  optimalnaZaliha: number | null;
}

export interface ZalihaListItem {
  id: number;
  idArtikla: number;
  artikalNaziv: string;
  artikalSifra: string;
  varijantaId: number;
  varijantaNaziv: string;
  kolicina: number;
  minZaliha: number | null;
  optimalnaZaliha: number | null;
}
