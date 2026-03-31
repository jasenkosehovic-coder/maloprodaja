# Maloprodaja — Task Lista

## FAZA 1 — Infrastruktura ✅ ZAVRŠENA

- [x] Inicijalizuj Spring Boot projekat (Java 21, Spring Boot 3.3.5, pom.xml)
- [x] Konfiguriši PostgreSQL i application.yml (dev/prod profili, env varijable)
- [x] Kreiraj BaseEntity sa sistemskim poljima i JPA Auditing
- [x] Implementiraj TenantContext, ApiResponse, GlobalExceptionHandler
- [x] Konfiguriši Swagger/OpenAPI i CORS
- [x] Inicijalizuj Angular projekat sa Material i strukturom foldera
- [x] Kreiraj shared Angular komponente (DataTable, ConfirmDialog, ExportButtons, LoadingSpinner, PageHeader, FormFieldError)

---

## FAZA 2 — Autentifikacija i RBAC ✅ ZAVRŠENA

- [x] Implementiraj JWT autentifikaciju — backend
  - Entiteti: Korisnik, KorisnikIzbornik
  - JwtUtil, JwtAuthFilter, KorisnikDetailsService
  - AuthService, AuthController (POST /api/auth/login, /refresh, /logout, GET /me)
  - uloga + poslovnicaId u JWT claims
- [x] Implementiraj Spring Security konfiguraciju i RBAC
  - SecurityConfig (whitelist, CSRF off, stateless, @EnableMethodSecurity)
  - KorisnikUloga: SUPER_ADMIN, ADMIN, MENADZER, BLAGAJNIK, KNJIGOVODŽA
  - KorisnikIzbornik entity + KorisnikService + KorisnikController
  - Flyway V2 migracija za korisnik_izbornici
- [x] Implementiraj Angular autentifikaciju i layout
  - LoginComponent (ReactiveForm, signal-based)
  - AuthService signal-based, authGuard, roleGuard
  - JWT interceptor sa auto-refresh (401 → refresh → retry)
  - InactivityService — auto-logout nakon 1h neaktivnosti
  - Dinamički meni po ulozi (top meni)

---

## FAZA 3 — Šifrarnici 🔲 TODO

- [x] Implementiraj šifarnike — backend
  [x]  Entiteti: GrupaArtikala, ArtikalKompanija, ArtikalPoslovnica, Barkod, Proizvodjac, Dobavljac, Kupac, Popust
  [x]  Kalkulacija MPC: VPC + marža + PDV - popust
  [x]  Tipovi marže: FIKSNA_MARZA, FIKSNA_CIJENA, SLOBODNA, DIFERENCIRANA (po kupcu)
  - Popusti: po grupi, proizvođaču, dobavljaču, periodu, kupcu
  [x]  CRUD + filter + paginacija + deaktivacija za sve entitete
  - Historija kupovine za kupca
- [ ] Implementiraj Export/Import (Excel, CSV, PDF) i naljepnice
  [x] ExcelExportService, CsvExportService, PdfExportService (JasperReports)
  [x] ExcelImportService sa definisanim templateima
  - PDF naljepnice za artikle (barkod + cijena + popust + kalkulacija)
- [x]  Implementiraj šifarnike — Angular frontend
  [x]  Feature komponente za sve šifarnike (list + form)
  - Kalkulacija cijene komponenta
  - Ispis naljepnica komponenta
  - Historija kupovine za kupca

---

## FAZA 4 — Blagajna 🔲 TODO

- [ ] Implementiraj blagajnu — backend
  - Entiteti: Racun, RacunStavka, StornoRacun, StornoStavka, Posudba, PosudbaStavka, Ponuda, PonudaStavka, VeleprodajniRacun, VeleprodajnaStavka, ZakljucenDan
  - Prodaja (gotovina, kartica, virman)
  - Storno: parcijalni/puni, povrat zalihe + novca (blagajna/kartica)
  - Posudba: rezervacija zalihe, konverzija u račun, ističe kraj godine
  - Ponuda: konverzija u račun
  - Veleprodaja: virman, fiskalizacija, pregled zalihe po poslovnicama
  - Zaključivanje dana: blokira prodaju, okida dnevni fiskalni izvještaj
  - PDF štampa A4 za račun/posudbu/ponudu (JasperReports)
- [ ] Implementiraj blagajnu — Angular frontend
  - Glavni POS ekran (dodavanje artikala barkodom/pretragom)
  - Plaćanje komponenta (gotovina/kartica/virman, kalkulator kusura)
  - Storno komponenta (odabir stavki)
  - Posudba, Ponuda, Veleprodaja feature komponente
  - Zaključivanje dana
  - Pregled računa i storniranih računa

---

## FAZA 5 — Fiskalizacija 🔲 TODO

- [ ] Implementiraj fiskalizaciju — backend
  - Interface FiskalniPrinterService
  - EpsonFiskalniPrinterService (HTTP komunikacija sa E-link serverom)
  - TringFiskalniPrinterService (Tring fiscal server)
  - Factory pattern za odabir implementacije po tipu printera
  - Entitet FiskalniPrinter (tip, host, port)
  - Integracija u RacunService, StornoService, ZakljucivanjeService
- [ ] Implementiraj fiskalni panel — Angular frontend
  - FiskalniPanelComponent (sve fiskalne operacije)
  - Forma za postavke printera
  - Prikaz statusa printera

---

## FAZA 6 — Dokumenti ✅ ZAVRŠENA

### 6.1 — Data Layer (Backend)
- [x] **V15 Flyway migracija** — tabele za sve dokumente:
  - `ulazne_fakture` (id, id_kompanije, id_poslovnice, id_dobavljaca, broj, datum, datum_valute, status, ukupno_bez_pdv, ukupno_pdv, ukupno, napomena + audit)
  - `ulazne_fakture_stavke` (id, id_fakture, id_artikla, kolicina, vpc, pdv_stopa, iznos_pdv, ukupno + audit)
  - `nivelacije` (id, id_kompanije, id_poslovnice, id_fakture nullable, broj, datum, vrsta, napomena + audit)
  - `nivelacije_stavke` (id, id_nivelacije, id_artikla, kolicina, vpc, mpc_stara, mpc_nova, iznos_nivelacije + audit)
  - `otpremnice` (id, id_kompanije, id_poslovnice_posiljaoca, id_poslovnice_primaoca, broj, datum, status, napomena + audit)
  - `otpremnice_stavke` (id, id_otpremnice, id_artikla, kolicina, vpc_posiljaoac, mpc_posiljalac + audit)
- [x] **Enum-i**: `StatusFakture` (NACRT, POTVRDJENO, STORNIRANO), `StatusOtpremnice` (KREIRANA, POSLANA, PRIMLJENA, STORNIRANA), `VrstaNivelacije` (AUTOMATSKA_FAKTURA, AUTOMATSKA_OTPREMNICA, RUCNA)

### 6.2 — Entiteti & Repozitoriji (Backend)
- [x] `UlaznaFaktura` entity (extends PoslovnicaBaseEntity) + `UlaznaFakturaRepository`
- [x] `UlaznaFakturaStavka` entity (extends BaseEntity, ManyToOne → UlaznaFaktura) + repozitorij
- [x] `Nivelacija` entity (extends PoslovnicaBaseEntity) + `NivelacijaRepository`
- [x] `NivelacijaStavka` entity (extends BaseEntity, ManyToOne → Nivelacija) + repozitorij
- [x] `Otpremnica` entity (extends KompanijaBaseEntity, ima i id_poslovnice_posiljaoca i id_poslovnice_primaoca) + `OtpremnicaRepository`
- [x] `OtpremnicaStavka` entity (extends BaseEntity, ManyToOne → Otpremnica) + repozitorij

### 6.3 — Ulazna Faktura (Backend)
- [x] **DTOs**: `UlaznaFakturaDTO` — `ListItemDTO`, `DetailDTO`, `CreateDTO`, `UpdateDTO`, `StavkaDTO`
- [x] **`IUlaznaFakturaService`** + **`UlaznaFakturaService`**:
  - `listAll(idKompanije, idPoslovnice)` → lista zaglavlja
  - `findById(id)` → detalj sa stavkama
  - `create(dto, idKompanije, idPoslovnice)` → kreira fakturu u statusu NACRT
  - `update(id, dto)` → izmjena dok je NACRT
  - `potvrdi(id)` → status POTVRDJENO + ažurira zalihu (`kolicina +=`) + okida automatsku nivelaciju ako se VPC promijenio
  - `storno(id)` → status STORNIRANO + vraća zalihu (`kolicina -=`) + kreira storno nivelaciju
- [x] **`UlaznaFakturaController`** (`/api/dokumenti/fakture`): GET list, GET /:id, POST, PUT /:id, POST /:id/potvrdi, POST /:id/storno

### 6.4 — Nivelacija (Backend)
- [x] **DTOs**: `NivelacijaDTO` — `ListItemDTO`, `DetailDTO`, `CreateRucnaDTO`, `StavkaDTO`
- [x] **`INivelacijaService`** + **`NivelacijaService`**:
  - `listAll(idKompanije, idPoslovnice)` → lista nivelacija
  - `findById(id)` → detalj sa stavkama
  - `kreirajAutomatsku(idFakture/idOtpremnice, stavke)` → interno, poziva UlaznaFakturaService / OtpremnicaService
  - `kreirajRucnu(dto, idKompanije, idPoslovnice)` → ručna nivelacija za korekciju cijena
- [x] **`NivelacijaController`** (`/api/dokumenti/nivelacije`): GET list, GET /:id, POST (ručna)

### 6.5 — Otpremnica (Backend)
- [x] **DTOs**: `OtpremnicaDTO` — `ListItemDTO`, `DetailDTO`, `CreateDTO`, `PotvrdiPrijemDTO`, `StavkaDTO`
- [x] **`IOtpremnicaService`** + **`OtpremnicaService`**:
  - `listAll(idKompanije, idPoslovnice)` → lista (i poslane i primljene)
  - `findById(id)` → detalj sa stavkama
  - `create(dto, idKompanije, idPoslovnicePosaljioca)` → status KREIRANA, smanjuje zalihu pošiljaoca (`kolicina -=`)
  - `posalji(id)` → status POSLANA
  - `potvrdiPrijem(id, dto)` → status PRIMLJENA + povećava zalihu primaoca (`kolicina +=`) + nivelacija ako je MPC različit
  - `storno(id)` → status STORNIRANA + vraća zalihu pošiljaoca (ako nije primljena) ili oduzima od primaoca (ako je primljena)
- [x] **`OtpremnicaController`** (`/api/dokumenti/otpremnice`): GET list, GET /:id, POST, POST /:id/posalji, POST /:id/potvrdi-prijem, POST /:id/storno

### 6.6 — Import početnog stanja & PDF (Backend)
- [x] **ExcelImportService** — `importPocetnoStanje(file, idKompanije, idPoslovnice)`:
  - Template: sifra, naziv, kolicina, vpc, mpc
  - Za svaki red: pronađi/kreiraj ArtikalKompanija → kreiraj/ažuriraj ArtikalPoslovnica (kolicina, vpc, mpc)
  - Endpoint: POST `/api/dokumenti/import/pocetno-stanje`
- [x] **PDF izvještaji** (JasperReports):
  - Faktura PDF (zaglavlje + tabela stavki + zbrojevi) → GET `/api/dokumenti/fakture/:id/pdf`
  - Otpremnica PDF → GET `/api/dokumenti/otpremnice/:id/pdf`
  - Nivelacija PDF → GET `/api/dokumenti/nivelacije/:id/pdf`

### 6.7 — Frontend: Modeli & Servisi
- [x] `dokumenti.models.ts` — interfejsi za sve dokumente (UlaznaFaktura, Nivelacija, Otpremnica + stavke + DTO-i)
- [x] `fakture.service.ts` — CRUD + potvrdi + storno + PDF download
- [x] `nivelacije.service.ts` — list + findById + kreirajRucnu + PDF download
- [x] `otpremnice.service.ts` — CRUD + posalji + potvrdiPrijem + storno + PDF download

### 6.8 — Frontend: Ulazna Faktura komponente
- [x] **`fakture-list`** — DataTable (broj, datum, dobavljač, status, ukupno) + filter po statusu
- [x] **`faktura-form`** — zaglavlje (dobavljač, datum, broj, valuta) + dinamička tabela stavki + kalkulacija zbrojeva + submit kao NACRT
- [x] **`faktura-detail`** — read-only prikaz + gumbi: Potvrdi / Storno / Štampaj PDF

### 6.9 — Frontend: Nivelacija komponente
- [x] **`nivelacije-list`** — DataTable (broj, datum, vrsta chip) + filter
- [x] **`nivelacija-detail`** — read-only tabela stavki (artikal, kolicina, vpc, mpcStara → mpcNova, iznos) + Štampaj PDF
- [x] **`nivelacija-rucna-form`** — forma za ručnu nivelaciju (odabir artikala, unos novih cijena)

### 6.10 — Frontend: Otpremnica komponente
- [ ] **`otpremnice-list`** — DataTable + filter po statusu/poslovnici/datumu
- [x] **`otpremnica-form`** — odabir primajuće poslovnice + dinamička tabela stavki (artikal, kolicina)
- [x] **`otpremnica-detail`** — status-aware prikaz + gumbi ovisno o statusu (Pošalji / Potvrdi Prijem / Storno / Štampaj)

### 6.11 — Routing & Integracija
- [x] Ažuriraj `dokumenti.routes.ts` sa lazy-loaded rutama za sve komponente
- [x] Dodaj navigacione stavke u top meni (role guard: ADMIN, MENADZER, KNJIGOVODJA)

---

## FAZA 7 — Izvještaji 🔲 TODO

- [ ] Implementiraj izvještaje — backend
  - KnjigaBlagajni, TrgovackaKnjiga, ArtikliBezIzlaza
  - StanjeZaliha (po poslovnicama, min/optimal, vrijednost po tarifama)
  - LagerLista, PregledDokumenata
  - RekapitulacijaBlagajne, StornoStavke, Realizacija, PrometArtikala
  - Export: JasperReports PDF + Excel + CSV za sve izvještaje
- [ ] Implementiraj izvještaje — Angular frontend
  - Filter forma + DataTable + ExportButtons za svaki izvještaj

---

## FAZA 8 — Chat 🔲 TODO

- [ ] Implementiraj realtime chat između poslovnica
  - Backend: Spring WebSocket + STOMP, entitet ChatPoruka, trajno čuvanje
  - JWT autentifikacija u WebSocket handshakeu
  - Frontend: @stomp/stompjs, ChatComponent, indikator novih poruka, historija

---

## FAZA 9 — Postavke i korisnici 🔲 TODO

- [ ] Implementiraj postavke i upravljanje korisnicima
  - Backend: ProgramskiParametri, PostavkePoslovnice, KorisnikController
  - Frontend: forme za parametre, postavke poslovnice, upravljanje korisnicima + izbornicima, "O programu"

---

## FAZA 10 — Testiranje i deployment 🔲 TODO

- [ ] Unit testovi: KalkulacijaService, NivelacijaService, StornoService
- [ ] Integracijski testovi za kritične endpointe (auth, racun, faktura)
- [ ] Strukturirani logging (SLF4J + Logback, MDC za tenant)
- [ ] Dockerizacija backend i frontend
- [ ] Docker Compose (backend + frontend + PostgreSQL)
- [ ] init.sql za demo kompaniju/poslovnicu/admin korisnika
- [ ] Excel import templati za sve module
- [ ] Swagger UI dokumentacija kompletna

---

## Napredak

| Faza | Status | Taskovi |
|---|---|---|
| 1 — Infrastruktura | ✅ Završena | 7/7 |
| 2 — Auth & RBAC | ✅ Završena | 3/3 |
| 3 — Šifrarnici | 🔲 TODO | 2/3 |
| 4 — Blagajna | 🔲 TODO | 0/2 |
| 5 — Fiskalizacija | 🔲 TODO | 0/2 |
| 6 — Dokumenti | ✅ Završena | 11/11 |
| 7 — Izvještaji | 🔲 TODO | 0/2 |
| 8 — Chat | 🔲 TODO | 0/1 |
| 9 — Postavke | 🔲 TODO | 0/1 |
| 10 — Deployment | 🔲 TODO | 0/1 |
| **Ukupno** | **23/33** | **70%** |
