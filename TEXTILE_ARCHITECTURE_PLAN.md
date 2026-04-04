# Plan refaktorisanja arhitekture artikala — Tekstilna industrija

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
[postojeći tabovi]  Artikli | Artikli u poslovnici | Barkodovi | Grupe artikala | Popusti
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

### Sifarnici → Tipovi veličina (posebna stranica)

Tipovi veličina su **master data** — postavljaju se jednom i referenciraju iz taba Artikli:

```
Sifarnici
  └── Tipovi veličina
        ├── [Lista tipova]         — "Konfekcija S-XXL", "Cipele 36-46", "One size"...
        └── [Odabrani tip]
              └── Inline tabela    — oznaka veličine (S/M/L...), redosljed, aktivan
                                     dodavanje/brisanje veličina unutar tipa
```

### Sifarnici → Definicije atributa (posebna stranica)

Definicije atributa su **master data** — kompanija ih kreira jednom, a pojavljuju se
automatski kao kolone u tabu Artikli:

```
Sifarnici
  └── Definicije atributa
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

- [ ] **Backend — TipVelicina modul** (entity, repository, service, controller)
  - CRUD `/api/tipovi-velicina`
  - CRUD `/api/tipovi-velicina/{id}/velicine`
- [ ] **Backend — Atribut modul** (entity, repository, service, controller)
  - CRUD `/api/atributi/definicije` + `/vrijednosti`
  - CRUD `/api/artikli/{id}/atributi`
- [ ] **Backend — Slike modul** (entity, repository, service, controller)
  - CRUD `/api/artikli/{id}/slike` (upload fajla + čuvanje putanje)
- [ ] **Frontend — Sifarnici → Tipovi veličina** (nova posebna stranica: lista tipova + inline tabela veličina)
- [ ] **Frontend — Sifarnici → Definicije atributa** (nova posebna stranica: lista definicija + inline tabela vrijednosti)

---

### Sprint 2 — Varijante artikala + zaliha
**Cilj:** Novi model zalihe aktivan, stari još uvijek radi.

- [ ] **Backend — Varijanta modul**
  - Entity `VarijantaArtikla`, `VarijantaArtiklaPoslovnica`
  - Service + repository + controller
  - `ArtikalKompService.create` → auto-kreira default varijantu
- [ ] **Backend — ArtikalKompanija**
  - Dodati `idTipaVelicina` na entity i DTO
  - Update endpoint za `idTipaVelicina`
- [ ] **Backend — Stanje zalihe**
  - `/api/artikli/{id}/stanje` — pregled po poslovnicama + veličinama
  - `/api/varijante-poslovnica/{id}` — update zalihe direktno
- [ ] **Frontend — Tab "Artikli": dodati kolonu Tip veličine** (selectbox → tipovi_velicina)
- [ ] **Frontend — Tab "Artikli": dinamičke kolone atributa** (učitava se iz definicije_atributa, svaka je selectbox → vrijednosti_atributa)
- [ ] **Frontend — Tab "Veličine i barkodovi"** — prilagodba novom modelu (varijante + barkodovi)

---

### Sprint 3 — Refaktoring barkodova + uklanjanje zalihe sa artikli_poslovnice
**Cilj:** Barkodovi vezani za varijante, stanje zalihe samo u `varijante_artikla_poslovnica`.

- [ ] **Backend — Barkod entity + service**
  - Entity: `id_varijante` FK (šema već ispravna u V1)
  - Ukloniti stari `BarkodService`, logika ide u `VarijantaService`
- [ ] **Backend — ArtikalPoslovnicaService**
  - Ukloniti sve reference na `kolicina`, `minZaliha`, `optimalnaZaliha` (kolone ne postoje u V1 šemi)
  - Ukloniti ta polja iz DTO-a
- [ ] **Frontend — Tab "Barkodovi"**: prilagodba da prikazuje varijante (artikal + veličina → barkodovi)

---

### Sprint 4 — Dokumenti svjesni veličina + web shop modul
**Cilj:** Fakture/otpremnice biraju veličinu; web shop modul aktivan.

- [ ] **Backend — UlaznaFaktura stavka**
  - Dodati `id_varijante` na entity i DTO
  - Stock update na `varijante_artikla_poslovnica` umjesto `artikli_poslovnice`
- [ ] **Backend — Otpremnica stavka**
  - Dodati `id_varijante` na entity i DTO
  - Stock update na `varijante_artikla_poslovnica`
- [ ] **Frontend — Ulazna faktura** — biranje veličine pri unosu stavke
- [ ] **Frontend — Otpremnica** — biranje veličine pri unosu stavke
- [ ] **Backend — Web shop modul**
  - Entity `WebArtikal`, repository, service, controller
  - CRUD `/api/web-artikli`
  - Logika: pri objavi artikla popuniti `mpc` iz `artikli_poslovnice`, korisnik unosi `popust`, sistem računa `nova_mpc`
- [ ] **Frontend — novi Tab "Artikli web"**: dodavanje artikala iz kompanije na web shop (analogno "Artikli u poslovnici")
  - Selectbox za artikal, web_naziv, web_opis, mpc, popust, auto nova_mpc, meta polja, aktivan
- [ ] **Frontend — novi Tab "Slike artikala"**: upload i upravljanje slikama za artikle koji su u web shopu
  - Vidljivo samo za artikle koji postoje u `web_artikli`
  - Upload, naslovna slika, redosljed, brisanje
- [ ] **Testovi** — JUnit testovi za sve nove servise

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
