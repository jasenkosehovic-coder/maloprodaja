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
- [x] Implementiraj Spring Security konfiguraciju i RBAC
- [x] Implementiraj Angular autentifikaciju i layout

---

## FAZA 3 — Šifrarnici ✅ ZAVRŠENA

- [x] Implementiraj šifarnike — backend
  - GrupaArtikala, ArtikalKompanija, ArtikalPoslovnica, Barkod, Proizvodjac, Dobavljac, Kupac, Popust
  - CRUD + filter + paginacija + deaktivacija za sve entitete
- [x] ExcelExportService, CsvExportService, PdfExportService
- [x] ExcelImportService
- [x] Implementiraj šifarnike — Angular frontend (list + form za sve entitete)

---

## FAZA 4 — Blagajna ✅ ZAVRŠENA

- [x] Implementiraj blagajnu — backend
  - Entiteti: Racun, RacunStavka, StornoRacun, StornoStavka, Posudba, PosudbaStavka, Ponuda, PonudaStavka, VeleprodajniRacun, VeleprodajnaStavka, ZakljucenDan
  - Prodaja, Storno, Posudba, Ponuda, Veleprodaja, Zaključivanje dana
- [x] Implementiraj blagajnu — Angular frontend (svi ekrani)

---

## FAZA 5 — Fiskalizacija ✅ ZAVRŠENA

- [x] Implementiraj fiskalizaciju — backend
  - EpsonFiskalniPrinterService, TringFiskalniPrinterService, Factory pattern
- [x] Implementiraj fiskalni panel — Angular frontend

---

## FAZA 6 — Dokumenti (stara arhitektura) ✅ ZAVRŠENA / ZAMIJENJENA

> Stara arhitektura (ulazne_fakture, otpremnice) zamijenjena Promet arhitekturom u Sprint 4.
> Nivelacije ostaju — mijenjaju MPC/VPC, ne zalihu.

---

## FAZA 7 — Izvještaji ✅ ZAVRŠENA

- [x] Implementiraj izvještaje — backend (KnjigaBlagajni, StanjeZaliha, LagerLista...)
- [x] Implementiraj izvještaje — Angular frontend

---

## FAZA 8 — Chat ✅ ZAVRŠENA

- [x] Implementiraj realtime chat — backend (Spring WebSocket + STOMP, ChatPoruka entity)
- [x] Implementiraj chat — Angular frontend (@stomp/stompjs, ChatComponent)

---

## FAZA 9 — Postavke i korisnici ✅ ZAVRŠENA

- [x] Implementiraj postavke — backend (ProgramskiParametri, PostavkePoslovnice)
- [x] Implementiraj postavke — Angular frontend

---

## TEXTILE REFAKTORING — Sprint 1-4 ✅ ZAVRŠEN

### Sprint 1 — Master data (veličine + atributi + slike)
- [x] Backend — TipVelicina modul (CRUD `/api/tipovi-velicina`)
- [x] Backend — Atribut modul (CRUD `/api/atributi/definicije` + `/vrijednosti`)
- [x] Backend — Slike modul (upload + putanja)
- [x] Frontend — Šifarnici → Tipovi veličina
- [x] Frontend — Šifarnici → Definicije atributa

### Sprint 2 — Varijante artikala + zaliha
- [x] Backend — Varijanta modul (VarijantaArtikla, VarijantaArtiklaPoslovnica)
- [x] Backend — ArtikalKompanija + idTipaVelicina
- [x] Backend — Stanje zalihe po varijantama
- [x] Frontend — Tab "Artikli": Tip veličine + dinamičke kolone atributa
- [x] Frontend — Tab "Barkodovi": prilagodba novom modelu

### Sprint 3 — Barkodovi + boja dimenzija
- [x] Backend — Boja modul (`/api/boje`)
- [x] Backend — VarijantaArtikla + idBoje (2D: veličina × boja)
- [x] Backend — BarkodService prilagođen varijantama
- [x] Backend — ArtikalPoslovnica: uklonjena zaliha, ostala samo cijena
- [x] Frontend — Tab "Boje" sa hex color pickerom
- [x] Frontend — Barkodovi tab: accordion po varijantama + boja swatch

### Sprint 4 — Unified "Promet" arhitektura ✅ ZAVRŠENA
- [x] DDL — V1__init_schema.sql ažuriran (stare dokument-tablice uklonjene)
- [x] TipDokumenta entity + repository
- [x] Dokument entity + repository (Status: NACRT, POTVRĐEN, STORNIRAN)
- [x] StavkaDokumenta entity + repository
- [x] BrojacDokumenta entity + service (auto-number KOD-YYYY-NNNN)
- [x] PrometService (kreiranje, potvrda, storno, međuskladišnica)
- [x] PrometController (CRUD + potvrdi/storniraj + stavke)
- [x] PDF po tipu — shared Jasper template, GET `/api/dokumenti/{id}/pdf`
- [x] 10 JUnit testova za DokumentService
- [x] Frontend — Ulazne fakture (tipKod=UF)
- [x] Frontend — Povrat dobavljaču (tipKod=PD)
- [x] Frontend — Izlazne fakture (tipKod=IF, veleprodaja)
- [x] Frontend — Međuskladišnica (tipKod=MSI+MSU atomično)
- [x] Frontend — Topbar/navigacija ažurirana
- [ ] Frontend — Shared `PrometStavkeComponent` *(refaktoring u Sprint 5)*

---

## SPRINT 5 — Web shop modul 🔲 TODO

> Pitanja otvorena: autentifikacija web kupaca, košarica i narudžbe, javni API endpointi, platni sistemi.

- [ ] Backend webshop modul (WebArtikal entity postoji, treba dopuniti)
- [ ] Frontend — Tab "Artikli web" + Tab "Slike artikala"
- [ ] Javni API endpointi `/api/public/...` sa rate limitingom
- [ ] Web korisnici (autentifikacija kupaca)

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

| Faza / Sprint | Status |
|---|---|
| 1 — Infrastruktura | ✅ Završena |
| 2 — Auth & RBAC | ✅ Završena |
| 3 — Šifrarnici | ✅ Završena |
| 4 — Blagajna | ✅ Završena |
| 5 — Fiskalizacija | ✅ Završena |
| 6 — Dokumenti (stara) | ✅ Zamijenjena Prometom |
| 7 — Izvještaji | ✅ Završena |
| 8 — Chat | ✅ Završena |
| 9 — Postavke | ✅ Završena |
| Textile Sprint 1 | ✅ Završen |
| Textile Sprint 2 | ✅ Završen |
| Textile Sprint 3 | ✅ Završen |
| Textile Sprint 4 (Promet) | ✅ Završen |
| Sprint 5 — Web shop | 🔲 TODO |
| Faza 10 — Deployment | 🔲 TODO |
