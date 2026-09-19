# HANDOFF — kontekst za nastavak rada

**Datum:** 2026-09-17 · **Branch:** `master` · **Stvarni napredak: ~30%**

Ovaj dokument je ulazna točka za novu sesiju. Pročitaj ga prije `TASKS.md`.

---

## 1. Šta se dogodilo u sesiji od 2026-09-17

Proveden je potpuni review projekta: 5 agenata (`java-expert`, `angular-expert`,
`business-analyst`, `software-architect`, `qa-engineer`) je odvojeno pregledalo kod,
nakon čega su tvrdnje **neovisno verificirane** direktnim čitanjem koda, DDL-a,
konfiguracije i git historije.

**Glavni nalaz: dokumentacija je bila sistematski netočna.**
`TASKS.md` je označavao Faze 4 (Blagajna), 5 (Fiskalizacija), 7 (Izvještaji),
8 (Chat) i dio 9 (Postavke) kao "✅ ZAVRŠENA", a `git log --all --diff-filter=A`
dokazuje da ti paketi **nikada nisu postojali u ni jednom commitu**.

Stvarni napredak je ~30% (18 od ~63 poslovne sposobnosti iz
`PROGRAM_ZA_MALOPRODAJU-CLAUD_CODE_specifikacija.docx`), ne ~90%.

**Zašto je izgledalo završeno:**
1. `DataInitializer.ADMIN_IZBORNICI` daje ADMIN-u 11+ izbornika koji pokazuju na
   nepostojeće backendove
2. ~15 linkova u topbaru vodi na mrtve rute koje `app.routes.ts:97-100` catch-all
   (`{ path: '**', redirectTo: '' }`) tiho guta
3. Konfiguracija i ovisnosti postoje za neizgrađene feature (`spring.websocket.*`,
   `app.fiskalizacija.timeout-ms`, `app.export.temp-dir`, POI, OpenCSV, MapStruct,
   `@stomp/stompjs`)
4. Frontend stub komponente (`blagajna`, `fiskalni`, `izvjestaji`, `postavke`) su po
   24-25 linija sa praznim div-om i dugmetom **bez `(click)` handlera** — izgledaju kao
   ekran, ne rade ništa

## 2. Šta je promijenjeno

**Nije commitovano ni u jednoj sesiji** (pravilo iz `CLAUDE.md`).

### Sesija 2026-09-17 — dokumentacija

| Fajl | Promjena |
|---|---|
| `TASKS.md` | Kompletno prepisan — stvarno stanje + svi defekti prioritizirani P0/P1/P2 |
| `.claude/ARCHITECTURE.md` | Uklonjeno 10+ nepostojećih paketa, popravljene kontradikcije, branch `master` |
| `README.md` | Status warning na vrhu, stvarna lista modula, build caveati, Docker realnost |
| `TEXTILE_ARCHITECTURE_PLAN.md` | Popravljen predznak `kolicina`, status enum, odznačene lažne stavke |
| `.claude/docs/api-standard.md` | Bio prazan → napisan |
| `.claude/docs/testing.md` | Bio prazan → napisan |
| `.claude/docs/deploy.md` | Bio prazan → napisan |
| `.claude/docs/HANDOFF.md` | Novo (ovaj fajl) |

### Sesija 2026-09-18 — P0.1 zatvoren (kod)

Korisnik je odlučio red izvršavanja: **P0 → POS/blagajna → fiskalizacija.**

| Fajl | Promjena |
|---|---|
| `backend/pom.xml` | Lombok 1.18.42; pinovani `mockito` 5.20.0 + `byte-buddy` 1.17.8 (JDK 24+) |
| `common/security/JwtUtil.java` | **Prod bug:** `validateToken` je bacao `ExpiredJwtException` umjesto `false` |
| `common/config/JpaAuditingConfig.java` | Novo — `@EnableJpaAuditing` izdvojen sa glavne klase (rušio `@WebMvcTest`) |
| `test/support/ControllerTest.java` | Novo — složena anotacija za web slice sa pravim security chainom |
| `DokumentServiceTest.java` | Dodan `brojFakture` arg; uklonjena 4 mrtva stuba |
| `frontend/eslint.config.js` | Novo — flat config, `angular-eslint` 18 |
| `frontend/src/styles/_mixins.scss` | Novo — `dense-form-field`, `truncate-at`, `visually-hidden` |
| `shared/components/select-search/` | **Novo** — 5 kopija "pretraga u `mat-select`" unificirano; riješilo 10 a11y errora |
| `.github/workflows/ci.yml` | Novo — prvi CI u projektu |

**Rezultat:** `mvn verify` → 46 testova zeleno; `ng lint` → 0 errora; prod build OK.

## 3. Šta radi, šta ne

**Radi:** auth/RBAC · šifarnici (artikli, varijante, boje, veličine, atributi,
barkodovi, grupe, kupci, dobavljači, proizvođači, popusti CRUD) · Promet dokumenti
(UF, PD, MSI/MSU) · nivelacije · PDF dokumenata.

**Ne postoji (0% koda):** POS/blagajna · fiskalizacija · izvještaji · export/import ·
chat *backend* (FE `chat-websocket.service.ts` postoji i koristi STOMP, ali nema servera) ·
postavke · pravilo popusta · frontend za izlazne fakture (IF).

## 4. Prvih pet koraka u novoj sesiji

Sve reference su na sekcije u `TASKS.md`.

1. ~~**Popravi build** (P0.1)~~ ✅ **ZAVRŠENO 2026-09-18.** Kreni od koraka 2.
   *Nije verificirano lokalno:* `ng test` (nema Chrome na mašini) i klik-test 5
   refaktoriranih `select-search` dropdowna (nije bilo Postgresa/backenda).
2. **`V2__` migracija + Flyway u dev** (P0.2) — dodaj `popust_procenat` u
   `artikli_kompanije` i `artikli_poslovnice`; prebaci dev na `validate` + Flyway.
   Bez ovoga drift ostaje nevidljiv.
3. **`@Profile("dev")`** na `DataInitializer` i `SchemaFixer` + ukloni default
   `JWT_SECRET` (P0.3).
4. **Zatvori cross-tenant IDOR-e** (P0.4) — počni sa `KorisnikService.update:69-84`
   (mijenja ulogu i password tuđeg korisnika). Svaki popravak dobija negativni test.
5. **Unificiraj MPC formulu** (P0.6) — napiši zlatne testove **prije** popravke
   (`vpc=100, marža=50%, pdv=17% → 175.50`, trenutno vraća 167.00).

**Ne počinji Sprint 5 (web shop) ni P1 feature dok P0 nije zatvoren.**

## 5. Top defekti sa lokacijama (verificirano čitanjem koda)

| # | Defekt | Lokacija |
|---|---|---|
| 1 | PDV se računa na goli VPC → svaki artikal podcijenjen ~4.8% | `ArtikalPoslovnicaService.java:156-170` (poziva se :67, :104) |
| 2 | Cross-tenant promjena uloge i passworda | `KorisnikService.java:69-84`, `:89`, `:112` |
| 3 | Dokument se može knjižiti u tuđu poslovnicu | `DokumentService.java:109-110`, `:300-349` |
| 4 | Status enum mismatch — filtriranje nikad ne radi | BE `StatusDokumenta` (`POTVRDEN`) vs FE `promet.models.ts:10` (`'POTVRĐEN'`) + 6 komponenti |
| 5 | Zaliha: read-modify-write bez locka, nema `@Version` | `DokumentService.java:533-551` |
| 6 | Progutani exception truje transakciju | `DokumentService.java:355-398` |
| 7 | Storno re-primjenjuje originalne cijene, `this.potvrdi()` obilazi proxy | `DokumentService.java:192-234` |
| 8 | `popust_procenat` nema u DDL-u → prod ne diže | `V1__init_schema.sql:222-241`, `:267-283` |
| 9 | `popusti` nema `id_artikla` → pravilo popusta neizvedivo | `V1__init_schema.sql:437-451`; `PopustRepository` ima 1 metodu |
| 10 | `varijante_artikla` UNIQUE bez `id_boje` → 2D model ne radi | `V1__init_schema.sql` |
| 11 | Default JWT secret u gitu, prod ne override-a | `application.yml:50` |
| 12 | Dev seed + `SchemaFixer` rade u produkciji | nema `@Profile` nigdje |
| 13 | `TenantContext` je mrtav kod — tenancy je konvencija | `common/tenant/TenantContext.java` |
| 14 | ~1600 LOC mrtvog koda | `features/dokumenti/otpremnice/` (1170), sidebar (132), 4 shared komponente (~340) |
| 15 | ~1100-1200 LOC duplikacije u dokument ekranima | `PrometStavkeComponent` nikad napravljen |

## 6. Otvorene odluke — traže korisnikov input

1. **Tenancy** — Hibernate `@TenantId` (preporučeno) vs eksplicitni repo parametri?
2. **Status enum** — uskladiti FE na ASCII `POTVRDEN` (preporučeno) ili BE na `POTVRĐEN`?
3. **Chat** — implementirati ili izbaciti iz scope-a i skinuti ovisnosti?
4. **Popust scope** — koji nivoi (artikal/grupa/proizvođač/dobavljač) i prioritet?
5. **Storno semantika** — dokumentacija je govorila "ne mijenja status originala", kod
   postavlja original na `STORNIRAN`. Šta je ispravno?
6. ~~**P1 prioritet** — POS ili fiskalizacija prvo?~~
   ✅ **ODLUČENO (korisnik, 2026-09-17):** P0 → POS/blagajna → fiskalizacija.

## 7. Radna pravila iz `.claude/CLAUDE.md` koja vrijede

- **Nikad ne commituj ni deployaj bez eksplicitne instrukcije korisnika.**
- Korisnik je UI/UX dizajner — odgovori kratki, 1-2 rečenice, bez narativa i liste fajlova.
- Zlatni standard: nikad shortcut, nikad duplikacija logike, unificiraj u kompozabilne
  patterne.
- Dokazi prije rješenja: pročitaj kod i izlistaj sve consumere **prije** predlaganja.
- Za QA i fix rad koristi `/fix` (9-fazni workflow).

## 8. Referentni fajlovi

| Fajl | Sadržaj |
|---|---|
| `TASKS.md` | **Prioritizirana lista rada — glavni radni dokument** |
| `.claude/ARCHITECTURE.md` | Stvarna struktura koda + poznata odstupanja |
| `.claude/CLAUDE.md` | Agent routing + poslovna pravila (pravilo popusta) |
| `.claude/docs/api-standard.md` | REST konvencije |
| `.claude/docs/testing.md` | Test strategija (Testcontainers, ne H2) |
| `.claude/docs/deploy.md` | Deployment blokatori i ciljni proces |
| `TEXTILE_ARCHITECTURE_PLAN.md` | Historijski dizajn Sprint 1-4 (ne radni dokument) |
| `PROGRAM_ZA_MALOPRODAJU-CLAUD_CODE_specifikacija.docx` | Autoritativna poslovna specifikacija (~63 sposobnosti) |
