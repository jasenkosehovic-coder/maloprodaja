# Plan refaktorisanja arhitekture artikala — Tekstilna industrija

> **Status: historijski dizajn dokument.** Sprint 1-4 su odrađeni prema ovom planu.
> Dokument je 2026-09-17 usklađen sa stvarnim kodom (predznak `kolicina`, status enum,
> nedovršeni frontend ekrani). **Za aktualnu listu rada koristi `TASKS.md`**, ne ovaj fajl.
>
> Poznata odstupanja implementacije od ovog dizajna:
> - `varijante_artikla` UNIQUE ne uključuje `id_boje` → 2D model (veličina × boja) ne radi
> - Frontend "Izlazne fakture" (`tipKod=IF`) nije napravljen
> - Frontend koristi `'POTVRĐEN'`, backend `POTVRDEN` → filtriranje po statusu ne radi
> - `PrometStavkeComponent` nije napravljen → ~1100 LOC duplikacije u dokument ekranima

## Pregled

Refaktorisanje sifarnika artikala kako bi sistem podržao prodaju u tekstilnoj industriji:
veličine po artiklu, praćenje zaliha po veličini po poslovnici, strukturirani SKU atributi
sa predefinisanim vrijednostima, slike artikala i web shop polja.

---

## Kontekst — postojeća arhitektura

```
artikli_kompanije        — matični podaci artikla (sifra, naziv, pdv...)
artikli_poslovnice       — cijena + ZALIHA po poslovnici (problem: zaliha je skalarna)
barkodovi                — barkodovi vezani za artikal (bez veze sa veličinom)
```

**Problem:** Zaliha (`kolicina`) je jedan broj po artiklu/poslovnici. Tekstilna industrija
zahtijeva praćenje zalihe po svakoj veličini (S, M, L, XL...) posebno.

---

## Nova arhitektura — šema tablica

### Dijagram odnosa

```
tipovi_velicina (1) ──── (N) velicine
                               oznaka (S/M/L, 38/39/40...)
                               redosljed

artikli_kompanije ──── (FK, nullable) tipovi_velicina
      │
      │ 1:N
      ▼
varijante_artikla           ← jedna varijanta = artikal + veličina
  id_artikla FK
  id_velicine FK (nullable) ← NULL = artikal bez veličina
  aktivan
  UNIQUE(id_artikla, id_velicine)
      │
      ├──── (1:N) barkodovi      ← barkod vezan za varijantu, može ih biti više
      │           id_varijante FK
      │           barkod UNIQUE per company
      │
      └──── (1:N) varijante_artikla_poslovnica   ← ZALIHA po varijanti
                  id_varijante FK
                  id_poslovnice
                  kolicina, min_zaliha, optimalna_zaliha
                  UNIQUE(id_varijante, id_poslovnice)

artikli_poslovnice          ← ostaje, samo CIJENA (kolicina se briše)
  id_artikla FK
  id_poslovnice
  vpc, marza, tipMarze, mpc
  UNIQUE(id_artikla, id_poslovnice)

definicije_atributa         ← šta kompanija želi pratiti (Boja, Sezona, Fit...)
  naziv, redosljed, obavezno, za_web, id_kompanije

vrijednosti_atributa        ← predefinisane opcije (Crvena, Plava, Slim Fit...)
  id_definicije FK
  vrijednost, redosljed, id_kompanije

artikal_atributi            ← koje vrijednosti ima konkretni artikal
  id_artikla FK
  id_definicije FK
  id_vrijednosti FK → vrijednosti_atributa
  UNIQUE(id_artikla, id_definicije)

slike_artikala              ← slike za prikaz i web shop
  id_artikla FK
  putanja VARCHAR(500)
  redosljed, je_naslovna, aktivan, id_kompanije

web_artikli                 ← artikli objavljeni na web shopu (posebna tabela)
  id_artikla FK → artikli_kompanije
  web_naziv, web_opis       ← web opisi neovisni od centralnog opisa
  aktivan                   ← da li je vidljiv na web shopu
  mpc                       ← regularna cijena na webu
  popust                    ← popust u procentima
  nova_mpc                  ← cijena sa popustom
  meta_title, meta_opis     ← SEO polja
  UNIQUE(id_artikla, id_kompanije)
```

---

## Pravila modela

| Pravilo | Gdje se provjerava |
|---|---|
| Artikal bez veličina → tačno jedna default varijanta (`id_velicine = NULL`) | `ArtikalKompService.create` |
| Artikal sa veličinama → jedna varijanta po veličini, ne može biti dvije iste | DB UNIQUE constraint |
| Barkod mora biti jedinstven u kompaniji | DB UNIQUE(barkod, id_kompanije) |
| Veličina se ne može brisati ako postoji aktivna varijanta | `VelicinaService.delete` |
| Tip veličine se ne može mijenjati na artiklu koji već ima varijante sa veličinama | `ArtikalKompService.update` |
| Cijena je uvijek ista za sve veličine artikla u poslovnici | Arhitekturni dizajn — cijena na `artikli_poslovnice` |
| Zaliha se prati po svakoj veličini posebno po poslovnici | `varijante_artikla_poslovnica` |

---

## Paket struktura — novi moduli

```
ba.maloprodaja.sifarnici
  ├── velicina/
  │   ├── entity/       TipVelicina.java, Velicina.java
  │   ├── dto/          TipVelicinaDTO.java, VelicinaDTO.java
  │   ├── repository/   TipVelicinaRepository.java, VelicinaRepository.java
  │   ├── service/      ITipVelicinaService.java, TipVelicinaService.java
  │   └── controller/   TipVelicinaController.java
  │
  ├── varijanta/
  │   ├── entity/       VarijantaArtikla.java, VarijantaArtiklaPoslovnica.java
  │   ├── dto/          VarijantaDTO.java, VarijantaPoslovnicaDTO.java
  │   ├── repository/   VarijantaArtiklaRepository.java, VarijantaArtiklaPoslovnicaRepository.java
  │   ├── service/      IVarijantaService.java, VarijantaService.java
  │   └── controller/   VarijantaController.java
  │
  └── atribut/
      ├── entity/       DefinicijaAtributa.java, VrijednostAtributa.java, ArtikalAtribut.java
      ├── dto/          DefinicijaAtributaDTO.java, ArtikalAtributDTO.java
      ├── repository/   DefinicijaAtributaRepository.java, VrijednostAtributaRepository.java, ArtikalAtributRepository.java
      ├── service/      IAtributService.java, AtributService.java
      └── controller/   AtributController.java
```

### Novi modul — web shop

```
webshop/
  ├── entity/       WebArtikal.java
  ├── dto/          WebArtikalDTO.java
  ├── repository/   WebArtikalRepository.java
  ├── service/      IWebArtikalService.java, WebArtikalService.java
  └── controller/   WebArtikalController.java
```

### Izmjene postojećih modula

```
sifarnici/artikalkomp/
  entity/ArtikalKompanija.java    + idTipaVelicina (web polja su u WebArtikal entitetu)
  entity/ArtikalAtribut.java      → premjestiti u atribut/ modul
  entity/Barkod.java              id_artikla → id_varijante, brisanje id_poslovnice

sifarnici/artikalposl/
  entity/ArtikalPoslovnica.java   brisanje: kolicina, minZaliha, optimalnaZaliha
  dto/ArtikalPoslovnicaDTO.java   brisanje: kolicina, minZaliha, optimalnaZaliha
```

---

## API endpointi — novi

```
# Tipovi veličina
GET    /api/tipovi-velicina
POST   /api/tipovi-velicina
PUT    /api/tipovi-velicina/{id}
DELETE /api/tipovi-velicina/{id}

# Veličine unutar tipa
GET    /api/tipovi-velicina/{id}/velicine
POST   /api/tipovi-velicina/{id}/velicine
PUT    /api/velicine/{id}
DELETE /api/velicine/{id}

# Varijante artikla (veličine + barkodovi)
GET    /api/artikli/{id}/varijante
POST   /api/artikli/{id}/varijante
PUT    /api/varijante/{id}
DELETE /api/varijante/{id}

# Zaliha po varijanti
GET    /api/artikli/{id}/stanje              — pregled po svim poslovnicama i veličinama
GET    /api/varijante/{id}/stanje            — za jednu varijantu
PUT    /api/varijante-poslovnica/{id}        — update zalihe

# Definicije atributa
GET    /api/atributi/definicije
POST   /api/atributi/definicije
PUT    /api/atributi/definicije/{id}
DELETE /api/atributi/definicije/{id}

# Vrijednosti atributa (dropdown opcije)
GET    /api/atributi/definicije/{id}/vrijednosti
POST   /api/atributi/definicije/{id}/vrijednosti
PUT    /api/atributi/vrijednosti/{id}
DELETE /api/atributi/vrijednosti/{id}

# Atributi artikla
GET    /api/artikli/{id}/atributi
PUT    /api/artikli/{id}/atributi            — upsert (šalje se cijela mapa)

# Slike artikla
GET    /api/artikli/{id}/slike
POST   /api/artikli/{id}/slike
PUT    /api/slike/{id}
DELETE /api/slike/{id}

# Web shop — upravljanje objavljenim artiklima
GET    /api/web-artikli                      — lista svih objavljenih artikala
GET    /api/web-artikli/{id}                 — detalji web artikla
POST   /api/web-artikli                      — objaviti artikal na webu
PUT    /api/web-artikli/{id}                 — ažurirati web opis, cijenu, popust
DELETE /api/web-artikli/{id}                 — ukloniti sa weba
```

---

## UI struktura — Sifarnici → Artikli (izmjene postojećih i novi tabovi)

Stranica **Šifarnici → Artikli** već postoji sa sljedećim tabovima:

```
[postojeći tabovi]  Artikli | Artikli u poslovnici | Barkodovi | Grupe artikala | Popusti | Tipovi veličina | Definicije atributa
```

### Izmjene postojećih tabova

#### Tab: Artikli (artikli_kompanije) — izmjene

Dodati dvije vrste novih kolona u tabelu/formu artikla:

1. **Kolona "Tip veličine"** — selectbox koji se popunjava iz šifarnika `tipovi_velicina`
   - Opcije dolaze iz `GET /api/tipovi-velicina` za trenutnu kompaniju
   - Ako nema tipova kreiranog → prazna lista (nije obavezno polje)
   - Odabirom tipa određuje se koji set veličina (S/M/L/XL ili 38-42...) važi za artikal

2. **Dinamičke kolone atributa** — za svaku aktivnu definiciju atributa kompanije (Boja, Sezona, Fit...)
   kreira se jedna kolona sa selectboxom:
   - Kolone se učitavaju dinamički iz `GET /api/atributi/definicije` pri otvaranju taba
   - Svaki selectbox se popunjava iz `GET /api/atributi/definicije/{id}/vrijednosti`
   - Ako kompanija nema definisanih atributa → nema dodatnih kolona
   - Vrijednost se sprema u tabelu `artikal_atributi`

#### Tab: Barkodovi — prilagodba novom modelu

Trenutni tab prikazuje barkod vezan za artikal. Novi model: **barkod je vezan za varijantu**
(artikal + veličina). Izmjene taba:

- Grupisanje po artiklu: svaki artikal prikazuje listu svojih varijanti sa barkodovima
- Za artikal **bez veličina** → jedna default varijanta, može imati više barkodova
- Za artikal **sa veličinama** → po jedna varijanta po veličini, svaka može imati više barkodova
- Dodavanje barkoda: odabir artikla → odabir varijante/veličine → unos barkoda
- Barkod ostaje jedinstven u kompaniji (validacija pri unosu)

### Novi tabovi

```
[novi tabovi]  ... | Artikli web | Slike artikala
```

#### Novi tab: Artikli web (web_artikli)

Funkcionira analogno tabu **"Artikli u poslovnici"** — iz centralnog šifarnika se biraju artikli
koji se žele objaviti na web shopu i popunjavaju web-specifični podaci:

| Polje | Opis |
|---|---|
| Artikal | Selectbox — bira se artikal iz `artikli_kompanije` koji još nije dodan na web |
| Web naziv | Naziv za prikaz na webu (može se razlikovati od centralnog naziva) |
| Web opis | Duži opis za web stranicu artikla |
| MPC | Cijena na web shopu |
| Popust (%) | Popust u procentima |
| Nova MPC | Auto-izračun: `mpc - (mpc * popust / 100)`, korisnik može i ručno upisati |
| Aktivan | Da li je artikal vidljiv na web shopu |
| Meta title | SEO naslov (max 160 znakova) |
| Meta opis | SEO opis (max 320 znakova) |

#### Novi tab: Slike artikala (slike_artikala)

Dostupno **samo za artikle koji su u artikli_kompanije** (postoji zapis u `artikli_kompanije`):

- Lista artikala koji su u centrali sa prikazom thumbnailova
- Po artiklu: upload jedne ili više slika (drag-drop ili file picker)
- Postavljanje naslovne/cover slike (prikazuje se prva u listingu)
- Redosljed slika drag-drop sortiranjem
- Brisanje pojedinačnih slika
- Slike se čuvaju na disku, u bazi samo putanja (`slike_artikala.putanja`)

### Tab: Tipovi veličina (unutar Artikli stranice)

Tipovi veličina su master data za veličine artikala — nalaze se kao tab direktno u
Šifarnici → Artikli jer se konceptualno odnose na artikle:

```
Šifarnici → Artikli → Tab: Tipovi veličina
  ├── [Lista tipova]         — "Konfekcija S-XXL", "Cipele 36-46", "One size"...
  └── [Odabrani tip]
        └── Inline tabela    — oznaka (S/M/L...), redosljed, aktivan
```

### Tab: Definicije atributa (unutar Artikli stranice)

Definicije atributa su master data za SKU atribute — nalaze se kao tab u Artikli jer
se vrijednosti atributa direktno primjenjuju na artikle:

```
Šifarnici → Artikli → Tab: Definicije atributa
  ├── [Lista definicija]     — Boja, Sezona, Fit, Sastav... (redosljed, za_web, aktivan)
  └── [Odabrana definicija]
        └── Inline tabela    — vrijednosti (Crvena, Slim fit...), redosljed, aktivan
```

---

## Šema baze

Cijela šema se nalazi u jednoj DDL skripti koja se izvršava pri svakom pokretanju aplikacije u razvoju:

```
backend/src/main/resources/db/migration/V1__init_schema.sql
```

Skripta kreira sve tablice ispočetka u ispravnom redoslijedu zavisnosti:
`kompanije → poslovnice → korisnici → tipovi_velicina → velicine → artikli_kompanije → varijante_artikla → artikli_poslovnice → barkodovi → varijante_artikla_poslovnica → definicije_atributa → vrijednosti_atributa → artikal_atributi → slike_artikala → web_artikli → dokumenti`

---

## Plan rada — sprintovi

---

### Sprint 1 — Master data moduli (veličine + atributi + slike)
**Cilj:** Novi master data moduli potpuno funkcionalni, šema već kreirana u V1.

- [x] **Backend — TipVelicina modul** (entity, repository, service, controller)
  - CRUD `/api/tipovi-velicina`
  - CRUD `/api/tipovi-velicina/{id}/velicine`
- [x] **Backend — Atribut modul** (entity, repository, service, controller)
  - CRUD `/api/atributi/definicije` + `/vrijednosti`
  - CRUD `/api/artikli/{id}/atributi`
- [x] **Backend — Slike modul** (entity, repository, service, controller)
  - CRUD `/api/artikli/{id}/slike` (upload fajla + čuvanje putanje)
- [x] **Frontend — Sifarnici → Tipovi veličina** (nova posebna stranica: lista tipova + inline tabela veličina)
- [x] **Frontend — Sifarnici → Definicije atributa** (nova posebna stranica: lista definicija + inline tabela vrijednosti)

---

### Sprint 2 — Varijante artikala + zaliha
**Cilj:** Novi model zalihe aktivan, stari još uvijek radi.

- [x] **Backend — Varijanta modul**
  - Entity `VarijantaArtikla`, `VarijantaArtiklaPoslovnica`
  - Service + repository + controller
  - `ArtikalKompService.create` → auto-kreira default varijantu
- [x] **Backend — ArtikalKompanija**
  - Dodati `idTipaVelicina` na entity i DTO
  - Update endpoint za `idTipaVelicina`
- [x] **Backend — Stanje zalihe**
  - `/api/artikli/{id}/stanje` — pregled po poslovnicama + veličinama
  - `/api/varijante-poslovnica/{id}` — update zalihe direktno
- [x] **Frontend — Tab "Artikli": dodati kolonu Tip veličine** (selectbox → tipovi_velicina)
- [x] **Frontend — Tab "Artikli": dinamičke kolone atributa** (učitava se iz definicije_atributa, svaka je selectbox → vrijednosti_atributa)
- [x] **Frontend — Tab "Barkodovi"** — prilagodba novom modelu (accordion po varijantama + barkodovi)

---

### Sprint 3 — Refaktoring barkodova + uklanjanje zalihe sa artikli_poslovnice + boja dimenzija
**Cilj:** Barkodovi vezani za varijante, stanje zalihe samo u `varijante_artikla_poslovnica`, boja kao druga dimenzija varijante.

- [x] **Backend — Barkod entity + service**
  - Entity: `id_varijante` FK (šema već ispravna u V1)
  - `BarkodService` prilagođen novom modelu, stare metode zamijenjene
- [x] **Backend — ArtikalPoslovnicaService**
  - Uklonjene sve reference na `kolicina`, `minZaliha`, `optimalnaZaliha`
  - Uklonjena ta polja iz entiteta i DTO-a
  - `ukupnaKolicina` kolona dodana (JPQL aggregate, bez N+1)
  - TODO komentari dodati u: `UlaznaFakturaService`, `OtpremnicaService`, `NivelacijaService`, `ImportPocetnogStanjaService` — riješava se u Sprintu 4
- [x] **Frontend — Tab "Artikli u poslovnici"**: uklonjena polja zalihe, dodana `ukupnaKolicina` kolona (sum po varijantama, narandžasta kad = 0)
- [x] **Backend — Boja modul** (`ba.maloprodaja.sifarnici.boja`)
  - `Boja` entity + `boje` tabela (naziv, hexKod nullable, aktivan, idKompanije)
  - `BojaService` sa guard-om: ne može se deaktivirati ako se koristi u aktivnim varijantama
  - `BojaController` — `/api/boje`, `/api/boje/aktivne`, POST, PUT, DELETE
- [x] **Backend — VarijantaArtikla proširena sa `id_boje`**
  - `VarijantaArtikla.java`: `idBoje` polje + `@ManyToOne Boja`
  - `VarijantaDTO`: `idBoje`, `nazivBoje`, `hexKodBoje`, `nazivVarijante`
  - `VarijantaService`: `buildBojeMap`, duplikat check za obje dimenzije, naziv "S / Crvena" logika
  - `VarijantaArtiklaRepository`: `existsByIdBojeAndAktivanTrue`, `existsByIdArtiklaAndIdVelicinaAndIdBoje`
- [x] **Frontend — Tab "Boje"** (`sifarnici/boje/`)
  - `BojeListComponent` sa tabelom, inline add/edit, hex preview swatch, native color picker
  - Ruta `boje` dodan kao artikli child u `sifarnici.routes.ts`
  - Tab link dodan u `artikli-shell.component.html`
- [x] **Frontend — Barkodovi tab proširen za boju**
  - Accordion paneli prikazuju color swatch + kombinirani naziv (S / Crvena)
  - Forma za dodavanje varijante sa mat-select za boju (color swatchevi u opcijama)

---

### Sprint 4 — Unified "Promet" arhitektura
**Cilj:** Zamijeniti sve odvojene dokument-tabele (ulazne_fakture, otpremnice, nivelacije, import_pocetnog_stanja)
jednim generičkim sistemom prometa koji radi sa varijantama artikala.

---

#### Arhitekturne odluke (sve potvrđene)

| # | Odluka | Detalji |
|---|---|---|
| 1 | Storno = novi dokument | Ne mijenja status, kreira novi dokument sa minus količinama |
| 2 | Međuskladišnica = 2 dokumenta | MEDJUSKLADISNICA_IZLAZ + MEDJUSKLADISNICA_ULAZ, sve-ili-ništa |
| 3 | Snapshot zalihe | `varijante_artikla_poslovnica.kolicina` ostaje, ažurira se pri potvrdi/stornu |
| 4 | Auto broj | Format `KOD-YYYY-NNNN`, resetuje se po godini+tipu, korisnik ne može editovati |
| 5 | PDF po tipu | Svaki tip dokumenta ima vlastiti Jasper template |
| 6 | Blagajna u dokumenti | `PRODAJA` tip sa smjer_kolicine = -1 |
| 7 | Veleprodaja cijena | VPC + marža bez PDV |
| 8 | Povrat samo dobavljaču | Nema povrata kupca u v1 |
| 9 | Nivelacije ostaju odvojene | Nivelacije mijenjaju MPC/VPC, ne količinu — ne uklapaju se u promet |

---

#### Nova šema tablica

```
tipovi_dokumenata
  id, kod VARCHAR(20) UNIQUE, naziv VARCHAR(100)
  smjer_kolicine SMALLINT  -- +1 (ulaz) ili -1 (izlaz)
  id_kompanije FK, sys_*

dokumenti
  id, id_tipa FK → tipovi_dokumenata
  id_poslovnice FK
  id_dobavljaca FK (nullable)   -- za ULAZNA_FAKTURA, POVRAT_DOBAVLJACU
  id_kupca      FK (nullable)   -- za IZLAZNA_FAKTURA (veleprodaja)
  status        VARCHAR(20)     -- NACRT | POTVRDEN | STORNIRAN  (ASCII, bez Đ)
  broj_dokumenta VARCHAR(30)    -- auto: KOD-YYYY-NNNN, kreira se pri potvrdi
  datum DATE, napomena TEXT
  id_kompanije FK, sys_*

stavke_dokumenata
  id, id_dokumenta FK → dokumenti
  id_varijante FK → varijante_artikla
  kolicina NUMERIC(12,3)        -- POTPISANA (+/−); servis množi sa tip.smjer_kolicine
  cijena   NUMERIC(12,4)        -- VPC pri ulasku, MPC pri izlasku
  popust   NUMERIC(5,2) DEFAULT 0
  ukupno   NUMERIC(12,4)        -- (cijena - popust%) * kolicina
  id_kompanije FK, sys_*

brojaci_dokumenata
  id, id_tipa FK, id_kompanije FK
  godina SMALLINT, brojac INT DEFAULT 0
  UNIQUE(id_tipa, id_kompanije, godina)
```

**Tipovi dokumenata (seed data):**

| kod | naziv | smjer_kolicine |
|---|---|---|
| UF | Ulazna faktura | +1 |
| PD | Povrat dobavljaču | -1 |
| IF | Izlazna faktura (veleprodaja) | -1 |
| MSI | Međuskladišnica izlaz | -1 |
| MSU | Međuskladišnica ulaz | +1 |
| PROD | Prodaja (blagajna) | -1 |

**Tablice koje se uklanjaju iz V1:**
- `ulazne_fakture`, `ulazne_fakture_stavke`
- `otpremnice`, `otpremnice_stavke`
- `import_pocetno_stanje`, `import_pocetno_stanje_stavke`
- Nivelacije **ostaju** (`nivelacije`, `nivelacije_stavke`) — mijenjaju cijene, ne zalihu

**Kalkulacija zalihe (single JPQL):**
```sql
SELECT SUM(s.kolicina * t.smjerKolicine)
FROM StavkaDokumenta s
JOIN s.dokument d
JOIN d.tipDokumenta t
WHERE s.idVarijante = :idVarijante
  AND d.idPoslovnice = :idPoslovnice
  AND d.status = 'POTVRDEN'
  AND d.idKompanije = :idKompanije
```

---

#### Backend — task lista

- [x] **DDL** — ažurirati `V1__init_schema.sql`: ukloniti stare tablice, dodati 4 nove
- [x] **TipDokumenta entity + repository** (`ba.maloprodaja.promet.tipdokumenta`)
- [x] **Dokument entity + repository** (`ba.maloprodaja.promet.dokument`)
  - Status enum: `NACRT`, `POTVRDEN`, `STORNIRAN` (ASCII — frontend koristi `'POTVRĐEN'`, mismatch)
- [x] **StavkaDokumenta entity + repository**
  - Napomena: `kolicina` je **potpisana** (+/−) — servis primjenjuje `smjerKolicine` pri unosu stavke
- [x] **BrojacDokumenta entity + service** — auto-number `KOD-YYYY-NNNN`
- [x] **PrometService** — kreiranje, potvrda, storno
  - `potvrdi(id)`: validacija + update snapshot zalihe + generisanje broja
  - `storniraj(id)`: kreira novi dokument sa stavkama `.negate()`, odmah potvrđuje
  - `kreirajMedjuskladisnicu`: atomično kreira MSI + MSU
- [x] **PrometController** — CRUD + akcije potvrdi/storniraj + stavke
- [x] **PDF po tipu** — jedan shared `buildPrometDokumentReport()` template za UF, IF, PD, MSI/MSU; logo iz `kompanija.logo`; endpoint `GET /api/dokumenti/{id}/pdf`
- [x] **Testovi** — 10 JUnit testova za DokumentService (kreiranje, potvrda, storno, međuskladišnica, nivelacija trigger, nivelacija failure isolation)

---

#### Frontend — task lista

- [x] **Adaptiraj "Ulazne fakture"** — sada gada `/api/dokumenti?tipKod=UF`
- [x] **Adaptiraj "Otpremnice" → "Povrat dobavljaču"** (`tipKod=PD`)
- [ ] **Novi screen "Izlazne fakture"** (veleprodaja, `tipKod=IF`) — ❌ **NE POSTOJI**:
      ni direktorij, ni ruta, ni komponenta. Backend podržava `IF`. Vidi `TASKS.md` P1.8.
- [x] **Novi screen "Međuskladišnica"** (`tipKod=MSI`+`MSU` atomično)
- [x] **Ažurirati topbar/navigaciju** — nove rute dodane; ⚠️ stare NISU uklonjene
      (~15 mrtvih linkova koje catch-all ruta tiho guta)
- [ ] **Shared komponenta za stavke** `PrometStavkeComponent` *(svaki screen ima vlastitu formu; refaktoring u Sprint 5 ako bude potrebe)*

---

## Pitanja otvorena za web shop modul (Sprint 5+)

- Autentifikacija web kupaca (posebna tabela `web_korisnici`?)
- Košarica i narudžbe (novi modul `narudžbe`)
- Javni API endpointi (`/api/public/...`) sa rate limitingom
- Integracija sa platnim sistemima

---

## Tehnički stack

| Sloj | Tehnologija |
|---|---|
| Backend | Java 21, Spring Boot 3.x LTS |
| ORM | Hibernate / JPA (code-first) |
| Baza | PostgreSQL |
| Migracije | Flyway |
| Validacija | Hibernate Validator (Jakarta) |
| PDF | JasperReports |
| Frontend | Angular LTS + Angular Material |
| Testing | JUnit 5, Spring Boot Test |
