// ===== Faktura =====
export interface UlaznaFakturaListItem {
  id: number;
  broj: string;
  datum: string;
  datumValute: string | null;
  statusFakture: string;
  nazivDobavljaca: string;
  ukupnoBezPdv: number;
  ukupnoPdv: number;
  ukupno: number;
}

export interface UlaznaFakturaDetail extends UlaznaFakturaListItem {
  idDobavljaca: number;
  napomena: string | null;
  unesenoUkupnoBezPdv: number | null;
  unesenoUkupno: number | null;
  stavke: FakturaStavka[];
}

export interface FakturaStavka {
  id: number;
  idArtikla: number;
  nazivArtikla: string;
  sifraArtikla: string;
  kolicina: number;
  vpc: number;
  popust: number | null;
  pdvStopa: number;
  iznosPdv: number;
  ukupno: number;
}

export interface CreateFakturaDTO {
  idDobavljaca: number;
  broj: string;
  datum: string;
  datumValute: string | null;
  napomena: string | null;
  ukupnoBezPdv?: number | null;
  ukupno?: number | null;
}

export interface AddFakturaStavkaDTO {
  idArtikla: number;
  kolicina: number;
  vpc: number;
  popust?: number | null;
}

// ===== Nivelacija =====
export interface NivelacijaListItem {
  id: number;
  broj: string;
  datum: string;
  vrsta: string;
  idFakture: number | null;
  idOtpremnice: number | null;
  brojStavki: number;
}

export interface NivelacijaDetail extends NivelacijaListItem {
  napomena: string | null;
  ukupnoNivelacije: number;
  stavke: NivelacijaStavka[];
}

export interface NivelacijaStavka {
  id: number;
  idArtikla: number;
  nazivArtikla: string;
  sifraArtikla: string;
  kolicina: number;
  vpc: number;
  mpcStara: number;
  mpcNova: number;
  iznosNivelacije: number;
}

export interface CreateNivelacijaRucnaDTO {
  broj: string;
  datum: string;
  napomena: string | null;
  stavke: { idArtikla: number; mpcNova: number }[];
}

// ===== Otpremnica =====
export interface OtpremnicaListItem {
  id: number;
  broj: string;
  datum: string;
  status: string;
  idPoslovnicePosiljaoca: number;
  nazivPoslovnicePosiljaoca: string;
  idPoslovnicePrimaoca: number;
  nazivPoslovnicePrimaoca: string;
  brojStavki: number;
}

export interface OtpremnicaDetail extends OtpremnicaListItem {
  napomena: string | null;
  stavke: OtpremnicaStavka[];
}

export interface OtpremnicaStavka {
  id: number;
  idArtikla: number;
  nazivArtikla: string;
  sifraArtikla: string;
  kolicina: number;
  vpcPosiljalac: number;
  mpcPosiljalac: number;
}

export interface CreateOtpremnicaDTO {
  idPoslovnicePrimaoca: number;
  broj: string;
  datum: string;
  napomena: string | null;
  stavke: { idArtikla: number; kolicina: number }[];
}

// ===== Import =====
export interface ImportResultDTO {
  ukupnoRedova: number;
  uspjesnoUvezeno: number;
  preskoceno: number;
  greske: string[];
}
