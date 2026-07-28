// ===== Tip dokumenta =====
export interface TipDokumenta {
  id: number;
  kod: string;
  naziv: string;
  smjerKolicine: number;
}

// ===== Statusi =====
export type DokumentStatus = 'NACRT' | 'POTVRĐEN' | 'STORNIRAN';

// ===== Lista dokumenata (paginirana) =====
export interface DokumentListItem {
  id: number;
  tipKod: string;
  tipNaziv: string;
  idPoslovnice: number;
  poslovnicaNaziv: string;
  dobavljacNaziv: string | null;
  kupacNaziv: string | null;
  status: DokumentStatus;
  brojDokumenta: string | null;
  brojFakture: string | null;
  datum: string;
  iznosMpc: number;
  iznosVpc: number;
  iznosPdv: number;
  sysCreatedDate: string;
}

export interface DokumentPage {
  content: DokumentListItem[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ===== Detalj dokumenta =====
export interface StavkaDokumenta {
  id: number;
  idVarijante: number;
  artikalNaziv: string;
  velicinaOznaka: string | null;
  bojaNaziv: string | null;
  kolicina: number;
  vpc: number;
  mpc: number;
  popustProcenat: number;
  pdvProcenat: number;
  marzaProcenat: number;
  iznosVpc: number;
  iznosMpc: number;
  iznosMarze: number;
  iznosPopusta: number;
  iznosPdv: number;
  sysCreatedDate: string;
}

export interface DokumentDetail {
  id: number;
  tipDokumenta: TipDokumenta;
  idPoslovnice: number;
  poslovnicaNaziv: string;
  idDobavljaca: number | null;
  dobavljacNaziv: string | null;
  idKupca: number | null;
  kupacNaziv: string | null;
  status: DokumentStatus;
  brojDokumenta: string | null;
  brojFakture: string | null;
  datum: string;
  napomena: string | null;
  iznosMpc: number;
  iznosVpc: number;
  iznosPdv: number;
  iznosPopusta: number;
  iznosMarze: number;
  stavke: StavkaDokumenta[];
  sysCreatedDate: string;
  sysModifiedDate: string;
}

// ===== DTO-ovi za kreiranje/ažuriranje =====
export interface CreateDokumentDTO {
  idTipa: number;
  idPoslovnice: number;
  idDobavljaca?: number;
  idKupca?: number;
  brojFakture?: string;
  datum: string;
  napomena?: string;
  stavke?: CreateStavkaDTO[];
}

export interface UpdateDokumentDTO {
  datum?: string;
  napomena?: string;
  idDobavljaca?: number | null;
  idKupca?: number | null;
}

export interface CreateStavkaDTO {
  idVarijante: number;
  kolicina: number;
  vpc: number;
  popustProcenat?: number;
  pdvProcenat?: number;
  marzaProcenat?: number;
}

export interface UpdateStavkaDTO {
  kolicina?: number;
  vpc?: number;
  popustProcenat?: number;
  pdvProcenat?: number;
  marzaProcenat?: number;
}

// ===== Međuskladišnica =====
export interface CreateMedjuskladisnicaDTO {
  izvorPoslovnicaId: number;
  odredistePoslovnicaId: number;
  datum: string;
  napomena?: string;
  stavke: CreateStavkaDTO[];
}

// ===== Parametri filtriranja =====
export interface DokumentFilterParams {
  tipKod: string;
  poslovnicaId?: number | null;
  status?: DokumentStatus | null;
  datumOd?: string | null;
  datumDo?: string | null;
  page?: number;
  size?: number;
}
