# Projekt — Arhitektura i Trenutno Stanje

## Tech Stack
- **Backend:** Java 21, Spring Boot 3.3.5, Spring Security 6, Spring Data JPA, Hibernate 6, PostgreSQL 16, Flyway 10, JWT (jjwt 0.12.6), MapStruct, Lombok, Apache POI, JasperReports, SpringDoc OpenAPI
- **Frontend:** Angular 18 LTS, Angular Material 18, TypeScript 5, RxJS 7, @stomp/stompjs
- **Dev URLs:** Backend `localhost:8080`, Frontend `localhost:4200`, Swagger `localhost:8080/swagger-ui.html`
- **Dev pokretanje:** `mvn spring-boot:run -Dspring-boot.run.profiles=dev` | `npm start` (frontend)

## Multi-tenant model
`Kompanija → Poslovnica → Korisnik` — svaki zapis nosi `id_kompanije` i `id_poslovnice`.

## Baza podataka
- **Jedna migracija:** `backend/src/main/resources/db/migration/V1__init_schema.sql` — kreira sve tablice ispočetka (code-first, svaki `dev` restart re-kreira)
- **Kreiranje tabelnog redosljeda:** `kompanije → poslovnice → korisnici → tipovi_velicina → velicine → artikli_kompanije → varijante_artikla → artikli_poslovnice → barkodovi → varijante_artikla_poslovnica → definicije_atributa → vrijednosti_atributa → artikal_atributi → slike_artikala → web_artikli → tipovi_dokumenata → dokumenti → stavke_dokumenata → brojaci_dokumenata → nivelacije → nivelacije_stavke`

## Backend paketi (`ba.maloprodaja.*`)
```
auth/                    — JWT autentifikacija
blagajna/                — racun, storno, posudba, ponuda, veleprodaja, zakljucivanje
chat/                    — WebSocket STOMP chat
common/                  — audit, config, dto, entity, exception, export, security, tenant, websocket
dokumenti/nivelacija/    — Nivelacije (mijenjaju MPC/VPC, ne zalihu — OSTAJU odvojene)
dokumenti/pdf/           — PDF generisanje
fiskalizacija/           — epson, tring, config, entity, repository
izvjestaji/              — controller, dto, service
kompanija/               — entity, repository
korisnik/                — controller, dto, entity, repository, service
poslovnica/              — controller, dto, entity, repository, service
postavke/                — controller, dto, entity, repository, service
promet/                  — GLAVNI DOKUMENT MODUL (Sprint 4)
  ├── brojac/            — auto-number KOD-YYYY-NNNN
  ├── dokument/          — Dokument entity (tipKod, status, poslovnica)
  ├── stavka/            — StavkaDokumenta (idVarijante, kolicina, cijena)
  └── tipdokumenta/      — TipDokumenta (kod, naziv, smjerKolicine ±1)
sifarnici/
  ├── artikal/artikalkomp/artikalposl/
  ├── atribut/           — DefinicijaAtributa, VrijednostAtributa, ArtikalAtribut
  ├── barkod/            — vezani za varijantu (ne artikal direktno)
  ├── boja/              — Boja entity (naziv, hexKod, aktivan)
  ├── dobavljac/kupac/proizvodjac/popust/
  ├── grupa/grupaartikala/
  ├── slika/             — SlikaArtikla (putanja na disku)
  ├── varijanta/         — VarijantaArtikla (idArtikla × idVelicine × idBoje), VarijantaArtiklaPoslovnica (zaliha)
  └── velicina/          — TipVelicina, Velicina
webshop/                 — WebArtikal entity (web opisi, SEO polja) — SPRINT 5
```

## Frontend feature moduli (`src/app/features/`)
```
auth/login/
blagajna/
chat/
dokumenti/
  ├── fakture/           — tipKod=UF (Ulazne fakture)
  ├── izlazne-fakture/   — tipKod=IF (Veleprodaja)
  ├── medjuskladisnica/  — tipKod=MSI+MSU atomično
  ├── nivelacije/        — odvojene (mijenjaju cijene)
  ├── otpremnice/        — stare komponente (zamijeniti ili ukloniti?)
  ├── povrat-dobavljacu/ — tipKod=PD
  ├── models/            — dokumenti.models.ts
  └── services/          — promet.service.ts (i stari servisi)
fiskalni/
izvjestaji/
korisnici/
postavke/
sifarnici/
  ├── artikli/           — shell sa tabovima (Artikli, u poslovnici, Barkodovi, Tipovi veličina, Definicije atributa, Boje)
  ├── boje/
  ├── definicije-atributa/
  ├── dobavljaci/kupci/grupe/popusti/proizvodjaci/
  └── tipovi-velicina/
```

## Promet arhitektura — ključna pravila

| Tipovi dokumenata | kod | smjer |
|---|---|---|
| Ulazna faktura | UF | +1 |
| Povrat dobavljaču | PD | -1 |
| Izlazna faktura (veleprodaja) | IF | -1 |
| Međuskladišnica izlaz | MSI | -1 |
| Međuskladišnica ulaz | MSU | +1 |
| Prodaja (blagajna) | PROD | -1 |

- **Storno = novi dokument** sa minus količinama (ne mijenja status originala)
- **Međuskladišnica = 2 dokumenta** (MSI + MSU) — sve-ili-ništa transakcija
- **Broj dokumenta** auto-generisan pri potvrdi: `KOD-YYYY-NNNN`
- **Nivelacije ostaju odvojene** — mijenjaju MPC/VPC, ne zalihu — ne uklapaju se u Promet
- **Zaliha** se čuva kao snapshot u `varijante_artikla_poslovnica.kolicina` (ažurira se pri potvrdi/stornu)

## Varijanta model — ključna pravila

- **Svaka varijanta = artikal × veličina × boja** (2D: idVelicine nullable + idBoje nullable)
- Artikal bez veličina → jedna default varijanta (`idVelicine = NULL`)
- **Barkod vezan za varijantu** (ne artikal direktno)
- **Zaliha** u `varijante_artikla_poslovnica` po varijanti+poslovnici
- **Cijena** u `artikli_poslovnice` za cijeli artikal (ista za sve veličine)
- Boja se ne može deaktivirati ako postoji aktivna varijanta

## Trenutni branch
`dokumenti_refactoring` — Sprint 4 promet arhitektura završena
