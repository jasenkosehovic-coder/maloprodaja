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
  + web_naziv, web_opis, web_aktivan, meta_title, meta_opis
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

### Izmjene postojećih modula

```
sifarnici/artikalkomp/
  entity/ArtikalKompanija.java    + idTipaVelicina, web polja
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
```

---

## Flyway migracije

| Skripta | Sadržaj | Kada |
|---|---|---|
| `V3__add_textile_schema.sql` | Kreiranje svih novih tablica, nullable FK na postojećim | Sprint 1 |
| `V4__update_barkodovi.sql` | `id_artikla → id_varijante`, brisanje `id_poslovnice` | Sprint 2 |
| `V5__deprecate_artikalposl_stock.sql` | Rename `kolicina → kolicina_legacy` na `artikli_poslovnice` | Sprint 3 |
| `V6__drop_legacy_stock.sql` | Brisanje `_legacy` kolona nakon stabilizacije | Sprint 4 |

> Napomena: Nema migracije podataka jer je sistem u razvoju bez produkcijskih podataka.

---

## Plan rada — sprintovi

---

### Sprint 1 — Nova šema + master data moduli
**Cilj:** Additive-only promjene, ništa se ne ruši, sve novo radi paralelno sa starim.

- [ ] **V3__add_textile_schema.sql** — kreiranje novih tablica
  - `tipovi_velicina`, `velicine`
  - `varijante_artikla`, `varijante_artikla_poslovnica`
  - `definicije_atributa`, `vrijednosti_atributa`, `artikal_atributi`
  - `slike_artikala`
  - Nullable FK `id_tipa_velicina` na `artikli_kompanije`
  - Web polja na `artikli_kompanije`
- [ ] **Backend — TipVelicina modul** (entity, repository, service, controller)
  - CRUD `/api/tipovi-velicina`
  - CRUD `/api/tipovi-velicina/{id}/velicine`
- [ ] **Backend — Atribut modul** (entity, repository, service, controller)
  - CRUD `/api/atributi/definicije` + `/vrijednosti`
  - CRUD `/api/artikli/{id}/atributi`
- [ ] **Backend — Slike modul** (entity, repository, service, controller)
  - CRUD `/api/artikli/{id}/slike` (upload fajla + čuvanje putanje)
- [ ] **Frontend — Tipovi veličina** (lista + forma)
- [ ] **Frontend — Definicije atributa** (lista + forma + vrijednosti)

---

### Sprint 2 — Varijante artikala + zaliha
**Cilj:** Novi model zalihe aktivan, stari još uvijek radi.

- [ ] **Backend — Varijanta modul**
  - Entity `VarijantaArtikla`, `VarijantaArtiklaPoslovnica`
  - Service + repository + controller
  - `ArtikalKompService.create` → auto-kreira default varijantu
- [ ] **Backend — ArtikalKompanija**
  - Dodati `idTipaVelicina` + web polja na entity i DTO
  - Update endpoint za web polja
- [ ] **Backend — Stanje zalihe**
  - `/api/artikli/{id}/stanje` — pregled po poslovnicama + veličinama
  - `/api/varijante-poslovnica/{id}` — update zalihe direktno
- [ ] **Frontend — Varijante** (pregled i unos veličina + barkodova za artikal)
- [ ] **Frontend — Web polja na formi artikla**

---

### Sprint 3 — Migracija barkodova + uklanjanje zalihe sa artikli_poslovnice
**Cilj:** Barkodovi korektno vezani za varijante, stanje zalihe više nije na `artikli_poslovnice`.

- [ ] **V4__update_barkodovi.sql**
  - Dodati `id_varijante` kolonu na `barkodovi`
  - Ukloniti `id_poslovnice` kolonu
  - Migracijski upit: vezati stare barkodove za default varijantu
- [ ] **Backend — Barkod entity + service**
  - Promijeniti vezu `id_artikla → id_varijante`
  - `BarkodService` delegira na `VarijantaService`
- [ ] **V5__deprecate_artikalposl_stock.sql**
  - Rename: `kolicina → kolicina_legacy`
- [ ] **Backend — ArtikalPoslovnicaService**
  - Ukloniti pisanje u `kolicina` (sada legacy)
  - Ukloniti `kolicina`, `minZaliha`, `optimalnaZaliha` iz DTO-a
- [ ] **Frontend — Forma artikla u poslovnici** (ukloniti polje zalihe, zaliha se vidi kroz varijante)

---

### Sprint 4 — Čišćenje + dokumenti svjesni veličina
**Cilj:** Sve legacy kolone uklonjene, fakture/otpremnice biraju veličinu.

- [ ] **V6__drop_legacy_stock.sql** — brisanje `_legacy` kolona
- [ ] **Backend — UlaznaFaktura stavka**
  - Dodati `id_varijante` na entity i DTO
  - Stock update na `varijante_artikla_poslovnica` umjesto `artikli_poslovnice`
- [ ] **Backend — Otpremnica stavka**
  - Dodati `id_varijante` na entity i DTO
  - Stock update na `varijante_artikla_poslovnica`
- [ ] **Frontend — Ulazna faktura** — biranje veličine pri unosu stavke
- [ ] **Frontend — Otpremnica** — biranje veličine pri unosu stavke
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
