# Projekt — Arhitektura i Trenutno Stanje

> **Verificirano 2026-09-17** direktnim čitanjem koda, DDL-a i git historije.
> Prethodna verzija ovog fajla dokumentirala je **šest paketa koji nikada nisu
> postojali**. Sve niže navedeno je potvrđeno da postoji u repozitoriju.
> Za listu defekata i preostalog rada vidi `TASKS.md`.

## Tech Stack

**Backend:** Java 21, Spring Boot 3.3.5, Spring Security 6, Spring Data JPA,
Hibernate 6, PostgreSQL 16, Flyway 10, JWT (jjwt 0.12.6), Lombok 1.18.42.

**Backend — deklarirano ali NEISKORIŠTENO:** MapStruct (0 mappera), Apache POI,
OpenCSV, Spring WebSocket. JasperReports se koristi samo u `dokumenti/pdf/`.

**Frontend:** Angular 18.2.14, Angular Material 18, TypeScript 5.5.4, RxJS 7.8.2, SCSS.
**Frontend:** `@stomp/stompjs` + `sockjs-client` SE koriste — u
`features/chat/services/chat-websocket.service.ts`. Ali **backend `chat` paket ne postoji**,
pa se servis nema na što povezati (build ih prijavljuje kao non-ESM warning). Vidi `TASKS.md` P1.6.

**Dev URLs:** Backend `localhost:8080`, Frontend `localhost:4200`,
Swagger `localhost:8080/swagger-ui.html`

**Dev pokretanje:** `mvn spring-boot:run -Dspring-boot.run.profiles=dev` | `npm start`

> ✅ **Build radi na oba stack-a** (od 2026-09-18): `mvn verify` → 46 testova zeleno,
> `ng lint` → 0 errora, `ng build --configuration production` → OK. CI:
> `.github/workflows/ci.yml`. Vidi `TASKS.md` P0.1.

## Multi-tenant model

`Kompanija → Poslovnica → Korisnik` — svaki zapis nosi `id_kompanije` i `id_poslovnice`.

> ⚠️ **Tenancy nije enforced na framework nivou.** `common/tenant/TenantContext.java`
> je **mrtav kod** — nijedan drugi fajl ga ne referencira. Nema Hibernate
> `@Filter`/`@TenantId`/interceptora. Izolacija je konvencija koju svaka servisna
> metoda mora ručno primijeniti, i **~9 servisa je ne primjenjuje** (`TASKS.md` P0.4).

## Baza podataka

- **Jedna migracija:** `backend/src/main/resources/db/migration/V1__init_schema.sql` (589 linija)
- **Redoslijed tablica:** `kompanije → poslovnice → korisnici → tipovi_velicina → velicine
  → artikli_kompanije → varijante_artikla → artikli_poslovnice → barkodovi
  → varijante_artikla_poslovnica → definicije_atributa → vrijednosti_atributa
  → artikal_atributi → slike_artikala → web_artikli → tipovi_dokumenata → dokumenti
  → stavke_dokumenata → brojaci_dokumenata → nivelacije → nivelacije_stavke`

> ⚠️ **Dev profil ne koristi Flyway.** `application-dev.yml`: `ddl-auto: create`,
> `flyway.enabled: false`. Prod: `ddl-auto: validate`, Flyway `true`.
> **Posljedica:** `V1__init_schema.sql` se nikada ne izvršava u razvoju, pa entity/DDL
> drift ostaje nevidljiv do produkcije. Trenutno postoji poznati drift
> (`popust_procenat`) koji **onemogućava boot u produkciji** — `TASKS.md` P0.2.

## Backend paketi (`ba.maloprodaja.*`) — stvarno stanje

```
auth/                    — JWT autentifikacija + Korisnik CRUD/RBAC
                           (korisnici su OVDJE, ne u zasebnom korisnik/ paketu)
common/                  — audit, config, dto, entity, exception, security, tenant
                           (tenant/ je mrtav kod; NEMA export/ ni websocket/)
dokumenti/nivelacija/    — Nivelacije (mijenjaju MPC/VPC, ne zalihu — OSTAJU odvojene)
dokumenti/pdf/           — PDF generisanje (JasperReports)
kompanija/               — entity, repository
poslovnica/              — controller, dto, entity, repository, service
promet/                  — GLAVNI DOKUMENT MODUL (Sprint 4)
  ├── brojac/            — BrojacDokumenta, auto-number KOD-YYYY-NNNN
  ├── dokument/          — Dokument entity, DokumentService (696 LOC, 9 kolaboratora)
  ├── stavka/            — StavkaDokumenta (idVarijante, kolicina, cijena)
  └── tipdokumenta/      — TipDokumenta (kod, naziv, smjerKolicine ±1)
sifarnici/
  ├── artikalkomp/       — ArtikalKompanija
  ├── artikalposl/       — ArtikalPoslovnica (cijena; zaliha je uklonjena u Sprintu 3)
  ├── atribut/           — DefinicijaAtributa, VrijednostAtributa, ArtikalAtribut
  ├── barkodovi/         — Barkod, vezan za varijantu (ne artikal direktno)
  ├── boja/              — Boja (naziv, hexKod, aktivan)
  ├── dobavljac/ kupac/ proizvodjac/ popust/
  ├── grupaartikala/     — GrupaArtikala
  ├── slika/             — SlikaArtikla (putanja na disku)
  ├── varijanta/         — VarijantaArtikla (idArtikla × idVelicine × idBoje),
  │                        VarijantaArtiklaPoslovnica (zaliha)
  └── velicina/          — TipVelicina, Velicina
webshop/                 — WebArtikal entity (web opisi, SEO) — nedovršeno, Sprint 5
```

### Paketi koje je stara dokumentacija navodila a NE POSTOJE

Potvrđeno sa `git log --all --diff-filter=A` — **nikada nisu postojali u ni jednom commitu:**

`blagajna/` · `fiskalizacija/` · `izvjestaji/` · `chat/` · `postavke/` · `korisnik/`
(korisnici žive u `auth/`) · `common/export/` · `common/websocket/` ·
`sifarnici/artikal/` · `sifarnici/grupa/`

## Frontend feature moduli (`src/app/features/`) — stvarno stanje

```
auth/login/                     ✅ radi
blagajna/                       ❌ STUB — 25 LOC, prazan div, dugme bez (click)
chat/                           ❌ STUB
dokumenti/
  ├── fakture/                  ✅ tipKod=UF
  ├── medjuskladisnica/         ✅ tipKod=MSI+MSU atomično
  ├── nivelacije/               🟡 kreirajRucnu zove nepostojeći endpoint
  ├── otpremnice/               ☠️ MRTAV KOD — 1170 LOC, zamijenjeno povrat-dobavljacu
  ├── povrat-dobavljacu/        ✅ tipKod=PD
  ├── models/promet.models.ts   ⚠️ status 'POTVRĐEN' ≠ backend POTVRDEN
  └── services/                 🟡 promet.service.ts ✅; fakture.service.ts ☠️ mrtav
fiskalni/                       ❌ STUB — 24 LOC
izvjestaji/                     ❌ STUB — 24 LOC
korisnici/                      ✅ radi
postavke/                       ❌ STUB — 24 LOC
sifarnici/                      ✅ radi
  ├── artikli/                  — shell sa tabovima (Artikli, U poslovnici, Barkodovi,
  │                               Tipovi veličina, Definicije atributa, Boje)
  ├── boje/ definicije-atributa/ tipovi-velicina/
  └── dobavljaci/ kupci/ grupe/ popusti/ proizvodjaci/
```

**`izlazne-fakture/` NE POSTOJI** — ni direktorij, ni ruta, ni komponenta, iako je
stari plan tvrdio da je završeno i backend podržava `tipKod=IF`.

### Shared komponente

```
shared/components/
  crud-table/          ✅ aktivno korištena (ali SCSS lomi build budget)
  confirm-dialog/      ✅
  page-header/         ✅
  loading-spinner/     ✅
  data-table/          ☠️ MRTAV — zamijenjen crud-table
  search-filter/       ☠️ MRTAV
  export-buttons/      ☠️ MRTAV
  form-field-error/    ☠️ MRTAV
layout/
  topbar/              ✅ ali ~15 linkova vodi na nepostojeće rute
  sidebar/             ☠️ MRTAV — 132 LOC, ne renderuje se
```

## Promet arhitektura — ključna pravila

| Tip dokumenta | kod | smjer |
|---|---|---|
| Ulazna faktura | UF | +1 |
| Povrat dobavljaču | PD | -1 |
| Izlazna faktura (veleprodaja) | IF | -1 |
| Međuskladišnica izlaz | MSI | -1 |
| Međuskladišnica ulaz | MSU | +1 |
| Prodaja (blagajna) | PROD | -1 |

- **Status enum (backend, autoritativno):** `NACRT`, `POTVRDEN`, `STORNIRAN` — **ASCII,
  bez `Đ`**. Frontend koristi `'POTVRĐEN'` i zato filtriranje po statusu nikad ne radi.
  Vidi `TASKS.md` P0.7.
- **`stavke_dokumenata.kolicina` je POTPISANA (+/−)** — `DokumentService:411-412`
  množi unesenu količinu sa `tipDokumenta.smjerKolicine`. (Stari `TEXTILE_ARCHITECTURE_PLAN.md`
  je na jednom mjestu tvrdio "uvijek pozitivna" — to je bilo netočno.)
- **Storno:** kod kreira novi dokument sa `.negate()` stavkama **I postavlja original
  na `STORNIRAN`** (`DokumentService.storniraj:192-234`). Stara dokumentacija je tvrdila
  suprotno ("ne mijenja status originala"). Koje je ispravno poslovno pravilo je
  **otvorena odluka** — `TASKS.md`.
- **Međuskladišnica = 2 dokumenta** (MSI + MSU) — atomično, jedna transakcija.
- **Broj dokumenta** auto-generisan pri potvrdi: `KOD-YYYY-NNNN`.
  ⚠️ Nema unique constraint na `dokumenti.broj_dokumenta` i nema locka na brojaču.
- **Nivelacije ostaju odvojene** — mijenjaju MPC/VPC, ne zalihu.
- **Zaliha** je snapshot u `varijante_artikla_poslovnica.kolicina`, ažurira se pri
  potvrdi/stornu. ⚠️ Nema `@Version`, nema locka, može ići u minus.

## Varijanta model — ključna pravila

- **Svaka varijanta = artikal × veličina × boja** (`idVelicine` nullable + `idBoje` nullable)
- Artikal bez veličina → jedna default varijanta (`idVelicine = NULL`)
- **Barkod vezan za varijantu** (ne artikal direktno)
- **Zaliha** u `varijante_artikla_poslovnica` po varijanti + poslovnici
- **Cijena** u `artikli_poslovnice` za cijeli artikal (ista za sve veličine)
- Boja se ne može deaktivirati ako postoji aktivna varijanta

> ⚠️ **DDL ne podržava 2D model.** `varijante_artikla` ima samo
> `UNIQUE(id_artikla, id_velicine)` — bez `id_boje`. Znači "ista veličina, dvije boje"
> je nemoguća. Vidi `TASKS.md` P0.5.

## Cijene

Tri divergentne implementacije MPC formule postoje u kodu; jedna je pogrešna
(`ArtikalPoslovnicaService.kalkulisajMpc:156-170` primjenjuje PDV na goli VPC umjesto
na cijenu s maržom → podcjenjuje ~4.8%). Treba unificirati u jedan `CijenaCalculator`.
Vidi `TASKS.md` P0.6.

**Pravilo popusta** iz `CLAUDE.md` je **0% implementirano** — nema resolvera, `popusti`
tablica nema `id_artikla`, `PopustRepository` ima jednu metodu. Popust se trenutno
uzima nevalidirano iz klijentskog DTO-a. Vidi `TASKS.md` P1.3.

## Dev-only kod koji se pokreće u produkciji

Nijedan `@Profile` guard ne postoji u backendu (grep: 0 rezultata):

- **`DataInitializer`** — seeda `admin`/`superadmin1`/`superadmin2` sa passwordom
  `Admin123!` i fiktivne kompanije. Također daje ADMIN-u 11+ izbornika za nepostojeće
  module — **glavni razlog zašto je projekt izgledao ~90% završen.**
- **`SchemaFixer`** — raw JDBC `ALTER TABLE korisnici DROP CONSTRAINT`.

Vidi `TASKS.md` P0.3.

## Trenutni branch

`master` — Textile Sprint 4 (Promet) backend završen, frontend djelomično.

## Povezani dokumenti

| Fajl | Sadržaj |
|---|---|
| `TASKS.md` | Prioritizirana lista svega što treba raditi (P0/P1/P2) |
| `.claude/docs/HANDOFF.md` | Kontekst za nastavak rada u novoj sesiji |
| `.claude/docs/api-standard.md` | REST API konvencije |
| `.claude/docs/testing.md` | Test strategija |
| `.claude/docs/deploy.md` | Deployment i konfiguracija |
| `TEXTILE_ARCHITECTURE_PLAN.md` | Historijski dizajn dokument za Sprint 1-4 |
