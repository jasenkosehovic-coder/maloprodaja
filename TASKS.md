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

## FAZA 2 — Autentifikacija i RBAC 🔲 TODO

- [ ] Implementiraj JWT autentifikaciju — backend
  - Entiteti: Korisnik, RefreshToken
  - JwtTokenProvider, JwtAuthenticationFilter, RefreshTokenService
  - AuthService, AuthController (POST /api/auth/login, /refresh, /logout, GET /me)
  - Logout nakon 1h neaktivnosti
- [ ] Implementiraj Spring Security konfiguraciju i RBAC
  - SecurityConfig (whitelist, CSRF off, stateless)
  - Entitet KorisnikIzbornik (override izbornika po korisniku)
  - KorisnikService sa logikom uloga i izbornika
- [ ] Implementiraj Angular autentifikaciju i layout
  - LoginComponent (ReactiveForm)
  - AuthService signal-based, authGuard, roleGuard
  - JWT interceptor sa auto-refresh
  - Automatski logout na 1h neaktivnosti
  - Dinamički meni po ulozi

---

## FAZA 3 — Šifrarnici 🔲 TODO

- [ ] Implementiraj šifarnike — backend
  - Entiteti: GrupaArtikala, ArtikalKompanija, ArtikalPoslovnica, Barkod, Proizvodjac, Dobavljac, Kupac, Popust
  - Kalkulacija MPC: VPC + marža + PDV - popust
  - Tipovi marže: FIKSNA_MARZA, FIKSNA_CIJENA, SLOBODNA, DIFERENCIRANA (po kupcu)
  - Popusti: po grupi, proizvođaču, dobavljaču, periodu, kupcu
  - CRUD + filter + paginacija + deaktivacija za sve entitete
  - Historija kupovine za kupca
- [ ] Implementiraj Export/Import (Excel, CSV, PDF) i naljepnice
  - ExcelExportService, CsvExportService, PdfExportService (JasperReports)
  - ExcelImportService sa definisanim templateima
  - PDF naljepnice za artikle (barkod + cijena + popust)
- [ ] Implementiraj šifarnike — Angular frontend
  - Feature komponente za sve šifarnike (list + form)
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

## FAZA 6 — Dokumenti 🔲 TODO

- [ ] Implementiraj dokumente — backend
  - Entiteti: UlaznaFaktura, UlaznaFakturaStavka, Nivelacija, NivelacijaStavka, Otpremnica, OtpremnicaStavka
  - Automatska nivelacija pri unosu fakture (mijenja MPC za SVU zalihu)
  - Nivelacija čuva: količinu, mpcStaru, mpcNovu, vpc, iznosNivelacije
  - Otpremnica: potvrda prijema od primajuće poslovnice → promjena zalihe + nivelacija ako je cijena različita
  - Import početnog stanja artikala iz Excela
  - Storno fakture i otpremnice
- [ ] Implementiraj dokumente — Angular frontend
  - UlaznaFaktura (list, forma s kalkulacijom, storno, ispis, naljepnice)
  - Nivelacija (list, pregled, ispis)
  - Otpremnica (list, forma, potvrda prijema, storno, ispis)

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
| 2 — Auth & RBAC | 🔲 TODO | 0/3 |
| 3 — Šifrarnici | 🔲 TODO | 0/3 |
| 4 — Blagajna | 🔲 TODO | 0/2 |
| 5 — Fiskalizacija | 🔲 TODO | 0/2 |
| 6 — Dokumenti | 🔲 TODO | 0/2 |
| 7 — Izvještaji | 🔲 TODO | 0/2 |
| 8 — Chat | 🔲 TODO | 0/1 |
| 9 — Postavke | 🔲 TODO | 0/1 |
| 10 — Deployment | 🔲 TODO | 0/1 |
| **Ukupno** | **7/24** | **29%** |
