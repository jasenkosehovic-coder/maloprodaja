# Maloprodaja — Task Lista (stvarno stanje)

> **Zadnja verifikacija:** 2026-09-17. Ovaj dokument je prepravljen nakon code reviewa od
> 5 agenata (`java-expert`, `angular-expert`, `business-analyst`, `software-architect`,
> `qa-engineer`) + direktne verifikacije koda, DDL-a i git historije.
>
> **Prethodna verzija ovog fajla bila je netočna** — označavala je Faze 4, 5, 7, 8, 9
> kao "✅ ZAVRŠENA" iako za njih **ne postoji nijedna linija koda** (potvrđeno sa
> `git log --all --diff-filter=A` — paketi `blagajna/`, `fiskalizacija/`, `izvjestaji/`,
> `chat/`, `postavke/` nikada nisu postojali u ni jednom commitu).
>
> **Stvarni napredak: ~30%** mjereno prema `PROGRAM_ZA_MALOPRODAJU-CLAUD_CODE_specifikacija.docx`
> (~63 poslovne sposobnosti: ~18 implementirano, ~13 djelomično, ~32 nedostaje).

---

## Legenda

| Oznaka | Značenje |
|---|---|
| ✅ | Implementirano i verificirano u kodu |
| 🟡 | Djelomično — postoji, ali sa poznatim defektima ili nedostajućim dijelovima |
| ❌ | Ne postoji kod. Prethodno lažno označeno kao završeno |
| 🔲 | Planirano, još nije početo |

| Prioritet | Značenje |
|---|---|
| **P0** | Blokira build, deployment, sigurnost ili integritet podataka. Radi prvo. |
| **P1** | Blokira osnovnu poslovnu funkciju proizvoda (ne može se prodati artikal). |
| **P2** | Kvalitet, dug, refaktoring, dokumentacija. |

---

# P0 — BLOKATORI (raditi prvo, ništa drugo prije ovoga)

## P0.1 — Build ne radi ni na jednoj strani ✅ ZAVRŠENO (2026-09-18)

Stanje sada: `mvn verify` → **46 testova, 0 grešaka, BUILD SUCCESS**.
`ng lint` → **0 errors** (92 `any` warninga, vidi P2.5). `ng build --configuration production` → OK.

- [x] **Backend build pada** — Lombok podignut na `1.18.42` (`pom.xml`).
- [x] **Backend testovi se ne kompajliraju** — `DokumentServiceTest` dopunjen
      `brojFakture` argumentom; uklonjena 4 mrtva Mockito stuba.
- [x] **Mockito ne radi na JDK 24+** — pinovano `mockito.version=5.20.0` +
      `byte-buddy.version=1.17.8` (novije od onoga što Boot 3.3.5 upravlja).
- [x] **Bug u produkcijskom kodu (otkriven kroz testove)** — `JwtUtil.validateToken`
      je bacao `ExpiredJwtException` umjesto da vrati `false`. Sada hvata
      `JwtException | IllegalArgumentException`.
- [x] **`@EnableJpaAuditing` na glavnoj klasi** rušio je `@WebMvcTest`
      (`JPA metamodel must not be empty`). Izdvojeno u `common/config/JpaAuditingConfig`.
- [x] **Slice testovi bez sigurnosnog konteksta** — dodana složena anotacija
      `support/ControllerTest` (`@WebMvcTest` + `@Import(SecurityConfig)` +
      `@MockBean(JwtUtil, UserDetailsService)`) da se mockovi ne dupliraju po klasama.
- [x] **Frontend `ng build` pada** — izdvojeni SCSS mixini u `src/styles/_mixins.scss`
      (`dense-form-field`, `truncate-at`, `visually-hidden`); budget `anyComponentStyle`
      podignut na 4kB/8kB jer SCSS inline-a mixine na svakom pozivu pa fajl ostaje >4kB.
- [x] **`ng lint` nikada nije pokrenut** — dodan `eslint.config.js` (flat config,
      `angular-eslint` 18 + `typescript-eslint` 8) i `lint` target. Prijavio 113 errora,
      svi riješeni:
      - 5 kopija istog "pretraga unutar `mat-select`" bloka (2 divergentne SCSS varijante,
        2 divergentna markupa) unificirano u `shared/components/select-search`
        — riješilo 10 a11y errora *i* duplikaciju umjesto da ih samo utiša.
      - Uklonjeni neiskorišteni importi (`DestroyRef`, `signal`, `AuthService`) i
        mrtva lokalna varijabla `dateFilter` u `crud-table`.
      - `faktura-form.dialogTitle` getter → `readonly` polje.
      - `template/eqeqeq` konfiguriran sa `allowNullOrUndefined: true` — `row.mpc != null`
        je namjerna provjera jer je `mpc?: number` opcionalan.
- [x] **Nema CI-a** — dodan `.github/workflows/ci.yml`: backend `mvn verify` (Java 21,
      temurin, maven cache) + frontend `npm ci` → `ng lint` → `ng test --browsers=ChromeHeadless`
      → `ng build --configuration production`.

⚠️ **Nije verificirano lokalno:** `ng test` — na ovoj mašini nema Chrome binarija
(`CHROME_BIN` nije postavljen), pa se frontend unit test (samo 1 postoji,
`app.component.spec.ts`) prvi put izvršava u CI-u. Također, 5 refaktoriranih
`select-search` dropdowna nije klik-testirano u browseru jer Postgres/backend
nisu bili pokrenuti — AOT produkcijski build je tipski provjerio sve 5 template-a.
Vidi P0.7 / QA.

## P0.2 — Produkcija se ne može pokrenuti (entity/DDL drift)

- [ ] **`popust_procenat` ne postoji u DDL-u** ali je mapiran u dvije entitete:
      - `ArtikalKompanija.popustProcenat` → kolona nema u `artikli_kompanije`
        (`V1__init_schema.sql:222-241`)
      - `ArtikalPoslovnica.popustProcenat` → kolona nema u `artikli_poslovnice`
        (`V1__init_schema.sql:267-283`)
      Sa `ddl-auto: validate` u prod profilu **aplikacija ne diže**.
      Dodati kolone u novu Flyway migraciju `V2__...`.
- [ ] **Dev nikada ne testira migraciju** — `application-dev.yml:9` `ddl-auto: create` +
      `:16` `flyway.enabled: false`, dok prod ima `validate` + Flyway `true`.
      Znači `V1__init_schema.sql` se nikada ne izvršava u razvoju → drift je nevidljiv
      dok ne pukne u produkciji. **Prebaciti dev na Flyway + `validate`.**
- [ ] **Uvesti `V2__` migraciju umjesto uređivanja `V1__`** — V1 je već primijenjen,
      izmjene V1 mijenjaju checksum i lome Flyway.

## P0.3 — Produkcija nije sigurna

- [ ] **Nijedan `@Profile` guard u cijelom backendu** (grep: 0 rezultata) →
      u produkciji se pokreću:
      - `DataInitializer` — seeda `admin`, `superadmin1`, `superadmin2` sa
        passwordom `Admin123!` i fiktivne kompanije
      - `SchemaFixer` — raw JDBC `ALTER TABLE korisnici DROP CONSTRAINT` (uklanja
        CHECK na ulogu)
      Dodati `@Profile("dev")` na oba; `SchemaFixer` zamijeniti Flyway migracijom.
- [ ] **Default JWT secret je u gitu** — `application.yml:50`
      `${JWT_SECRET:changeme-minimum-32-characters-long-secret-key}`; prod profil ga
      **ne override-a**. Ukloniti default, fail-fast ako env var nije postavljen.
- [ ] **Refresh token = access token** — nema razlike u claimovima ni validaciji,
      refresh token se može koristiti kao access token.
- [ ] **Logout je fiktivan** — token ostaje validan do expiry. Uvesti blacklist/jti
      ili kratki TTL + rotaciju refresh tokena.

## P0.4 — Cross-tenant IDOR (~9 servisa) ✅ ZAVRŠENO

Uveden zajednički sloj provjere umjesto ~50 ponovljenih inline provjera:
`TenantScopedRepository<T extends KompanijaBaseEntity>` (`findByIdAndIdKompanije`,
`existsByIdAndIdKompanije`, `findByIdInAndIdKompanije`) + `TenantGuard`
(`require`, `requireExists`, `requireAll`, `requirePoslovnica`,
`requirePoslovnicaExists`). Vraća **404, ne 403** — 403 bi potvrdio postojanje
tuđeg reda i postao oracle.

- [x] **`KorisnikService`** — `update`/`deactivate`/`getIzbornici`/`updateIzbornici`
      sada idu kroz `tenantGuard.require`; privilege escalation preko granice
      tenanta zatvoren.
- [x] Isti uzorak popravljen u: `PopustService`, `VarijantaService`, `DobavljacService`,
      `KupacService`, `ProizvodjacService`, `GrupaArtikalaService`, `SlikaArtiklaService`,
      `ArtikalKompService`, `NivelacijaService`, `AtributService`, `BarkodService`,
      `ArtikalPoslovnicaService`, `WebArtikalService`, `TipVelicinaService`,
      `BojaService`, `DokumentService`, `DokumentiPdfService`.
- [x] **FK smuggling** (id iz DTO-a se persistirao bez provjere vlasništva) zatvoren u
      `DokumentService.create` (`dto.idPoslovnice()` nije imao **nikakvu** provjeru),
      `kreirajMedjuskladisnicu` (obje poslovnice), `ArtikalPoslovnicaService.create`,
      `WebArtikalService.upsert`.
- [x] **Batch endpointi** (`batchUpdateMpc`, `batchUpdatePopust`, `reorderSlike`)
      prebačeni sa per-item `findById` loopa na `requireAll` + `saveAll` — jedan tuđi
      id sada ruši cijeli poziv umjesto da se batch djelomično primijeni.
- [x] **`SlikaArtiklaService.deactivate`** — provjera vlasništva prije brisanja fajla
      s diska (prije se fajl brisao pa se tek onda provjeravalo vlasništvo).
- [x] **`DokumentiPdfService.nivelacijaPdf`** nije primao `idKompanije` uopšte;
      `DokumentiPdfController` je bio jedini endpoint bez `Authentication auth`.
- [x] Testovi: 66 prolaze (bilo 56) — 10 novih negativnih cross-tenant testova
      (`KorisnikServiceTest`, `DokumentServiceTest`) koji dokazuju 404 na tuđi id.
- [ ] **`TenantContext` (ThreadLocal) je mrtav kod** — nula konzumenata, nadomješten
      `TenantGuard`-om. Uveden u `8c9163f` kao infrastruktura za koju nikad nije
      napisan interceptor. Brisati u P0.5 (nema nikakvu funkciju, ali brisanje nije
      hitno).
- [ ] **Dugoročno:** Hibernate `@TenantId` bi ovu provjeru pomjerio na razinu ORM-a
      pa se ne bi mogla zaboraviti. `TenantGuard` je i dalje *konvencija po metodi* —
      samo centralizirana i pokrivena testovima.

## P0.5 — Integritet podataka

- [ ] **Nema `@Version` nigdje** → `DokumentService.updateStock` (`:533-551`) radi
      `zaliha.getKolicina().add(kolicina)` read-modify-write bez locka.
      Dvije paralelne potvrde = izgubljena promjena zalihe.
- [ ] **Zaliha može ići u minus** — nema provjere prije skidanja.
- [ ] **Progutani exception truje transakciju** — `triggerAutomatskaNivelacija`
      (`:355-398`) ima `try { } catch (Exception e) { log.error(...) }` *unutar*
      transakcije → Spring markira tx rollback-only, ali servis nastavlja kao da je
      sve u redu. Izdvojiti u `REQUIRES_NEW` ili domain event.
- [ ] **Storno ponovno primjenjuje originalne cijene** — `storniraj` (`:192-234`)
      završava sa `return potvrdi(...)` pozvanim na `this` (obilazi proxy, gubi
      `@Transactional` semantiku) i re-trigera nivelaciju sa cijenama iz originala.
- [ ] **Nivelacija piše `vpcStara == vpcNova`** → historija cijena je falsificirana.
      Ovo je zakonska evidencija u BiH.
- [ ] **Ručna nivelacija ima `iznosNivelacije = ZERO` hardkodirano** (TODO u kodu).
- [ ] **`dokumenti.broj_dokumenta` nema unique constraint** → duplikati brojeva
      dokumenata pri konkurentnoj potvrdi. `BrojacDokumenta` nema pesimistički lock.
- [ ] **`varijante_artikla` UNIQUE je samo `(id_artikla, id_velicine)`** — bez
      `id_boje`. Znači "ista veličina, dvije boje" je nemoguća, što ruši cijeli
      2D varijanta model iz Sprinta 3.
- [ ] **Nedostaju FK i tenant indexi** na više tablica.

## P0.6 — Tri različite formule za MPC, jedna pogrešna

- [ ] **`ArtikalPoslovnicaService.kalkulisajMpc` (`:156-170`) — PDV se računa na goli
      VPC**, ne na cijenu s maržom:
      ```java
      BigDecimal cijenaSaMarza = vpc.add(vpc.multiply(marza100));
      BigDecimal cijenaSaPdv = cijenaSaMarza.add(vpc.multiply(pdv100)); // ← bug
      ```
      vpc=100, marža=50%, PDV=17% → vraća **167.00** umjesto **175.50**.
      Svaki artikal je tiho podcijenjen ~4.8%. Koristi se na `:67` i `:104`.
- [ ] **Unificirati u jedan `CijenaCalculator`** — trenutno postoje tri divergentne
      implementacije. Ovo direktno krši CLAUDE.md pravilo o zabrani duplikacije.
- [ ] Napisati unit testove za formulu prije popravke (zlatni testovi).

## P0.7 — FE/BE kontrakt je pokvaren

- [ ] **Status enum mismatch.** Backend: `StatusDokumenta { NACRT, POTVRDEN, STORNIRAN }`
      (ASCII). Frontend: `'POTVRĐEN'` (sa Đ) u:
      `models/promet.models.ts:10`, `faktura-detail.component.ts:436,445`,
      `fakture-list.component.ts:127,139`, `faktura-detail.component.html:437`,
      `medjuskladisnica-list.component.ts:91,103`,
      `povrat-dobavljacu-list.component.ts:84,116`.
      → Filtriranje po statusu **nikad ne pronalazi ništa**; slanje statusa baca
      `BusinessException("Nepoznat status dokumenta")`.
      **Odluka:** uskladiti na ASCII `POTVRDEN` (enum vrijednost) + prikazni label
      "Potvrđen" u UI-u.
- [ ] **`errors` polje** — frontend ga čita, backend ga nikad ne šalje.
- [ ] **`PageResponseDTO`** — oblik se ne poklapa između FE i BE.
- [ ] **Servisi koji zovu nepostojeće endpointe:** `fakture.service.ts`,
      `otpremnice.service.ts`, `nivelacije.service.ts → kreirajRucnu`.
- [ ] **`Barkod` model** na frontendu je vezan na pre-refactor kolone (prije
      varijanta modela).

---

# P1 — PROIZVOD NE MOŽE PRODATI NIŠTA

Ovo je jezgro maloprodajnog sistema i **potpuno ne postoji**, iako je stara verzija
ovog dokumenta tvrdila da je završeno.

## P1.1 — Blagajna / POS ❌ (Faza 4 — lažno označena završenom)

Backend paket `blagajna/` nikada nije postojao. Frontend `blagajna.component.ts` je
25 linija: `.empty-state` div i `<button mat-raised-button>` **bez `(click)` handlera**.

- [ ] Entiteti: `Racun`, `RacunStavka`, `StornoRacun`, `StornoStavka`, `Posudba`,
      `PosudbaStavka`, `Ponuda`, `PonudaStavka`, `VeleprodajniRacun`,
      `VeleprodajnaStavka`, `ZakljucenDan`
- [ ] Model plaćanja (gotovina / kartica / mješovito) — trenutno ne postoji nikakav
- [ ] Prodaja preko `PROD` tipa dokumenta (tip je seedan, koristi ga niko)
- [ ] Storno / reklamacija
- [ ] Posudbe, Ponude, Veleprodaja
- [ ] Zaključivanje dana
- [ ] **`idKupca` se nikada ne upisuje** iako kolona postoji
- [ ] Nema računa (ni PDF ni print)
- [ ] **BLAGAJNIK je zaključan iz svih dokument-write operacija** — ne može ni prodati
- [ ] Frontend POS ekran (barkod scan, brzi unos, tastatura/touch)

## P1.2 — Fiskalizacija ❌ (Faza 5 — lažno označena završenom)

Paket `fiskalizacija/` nikada nije postojao. `application.yml` ima
`app.fiskalizacija.timeout-ms` za servis koji ne postoji.
**Ovo je zakonski blokator za rad u BiH.**

- [ ] `IFiskalniPrinter` interface + factory
- [ ] `EpsonFiskalniPrinterService` (E-link)
- [ ] `TringFiskalniPrinterService`
- [ ] Konfiguracija po poslovnici
- [ ] Frontend fiskalni panel (trenutno stub, 24 linije)

## P1.3 — Pravilo popusta ❌ 0% implementirano

CLAUDE.md ga propisuje kao poslovno pravilo. Grep za
`resolvePopust|izracunajPopust|maxPopust|datumPrimjene` → **0 rezultata**.
Popust se trenutno uzima **nevalidirano iz klijentskog DTO-a**.

- [ ] **Schema:** `popusti` tablica (`V1:437-451`) **nema `id_artikla`** → pravilo je
      neizvedivo kako je dizajnirano. Dodati scope kolone: `id_artikla`,
      `id_grupe`, `id_proizvodjaca`, `id_dobavljaca` (nullable, hijerarhija scope-a).
- [ ] **Repository:** `PopustRepository` ima **tačno jednu metodu**
      (`findByIdKompanijeOrderByNazivAsc`). Dodati query po artiklu + poslovnici + datumu.
- [ ] **Servis:** `PopustResolverService.resolve(idArtikla, idPoslovnice, datumPrimjene)
      → BigDecimal` — MAX od tri izvora (centrala / poslovnica / kampanja), **nikad zbir**.
- [ ] Pozvati iz: kreiranja stavke dokumenta, prikaza cijene na kasi.
- [ ] Unit testovi za sve kombinacije izvora + granice datuma.

## P1.4 — Izvještaji 🟡 (Faza 7 — lažno označena završenom)

Postoji samo `dokumenti/pdf/`. Paket `izvjestaji/` nikada nije postojao.
Frontend `izvjestaji.component.ts` je stub od 24 linije.

- [ ] Knjiga blagajni
- [ ] Lager lista
- [ ] Stanje zaliha
- [ ] Trgovačka knjiga (zakonski obavezna)
- [ ] Promet artikala
- [ ] Frontend ekrani + filteri + export

## P1.5 — Export / Import ❌ (Faza 3 — lažno označena završenom)

`ExcelExportService`, `CsvExportService`, `PdfExportService`, `ExcelImportService`
**ne postoje**. Ovisnosti (Apache POI, OpenCSV) su deklarirane i neiskorištene.
`common/export` paket ne postoji. `app.export.temp-dir` konfiguriran za ništa.

- [ ] Excel export (generički, po entitetu)
- [ ] CSV export
- [ ] Excel import + validacijski report
- [ ] Import templati za module

## P1.6 — Chat ❌ (Faza 8 — lažno označena završenom)

**Backend:** nema WebSocket koda. `common/websocket` paket ne postoji.
`spring.websocket.*` konfiguracija postoji za ništa.
**Frontend:** `features/chat/` POSTOJI i `chat-websocket.service.ts` stvarno koristi
`@stomp/stompjs` + `sockjs-client` — dakle klijent je napisan protiv servera koji ne
postoji. (Ispravka: starija verzija ovog plana tvrdila je da su te ovisnosti
neiskorištene; `ng build` ih prijavljuje kao non-ESM warning, što to demantuje.)

- [ ] Odluka: implementirati ili ukloniti iz scope-a i skinuti ovisnosti/konfiguraciju
- [ ] Ako se implementira: `ChatPoruka` entity, STOMP broker, frontend komponenta

## P1.7 — Postavke ❌ (Faza 9 — djelomično lažno označena)

`korisnici` **postoji** (u `auth/` paketu, ne `korisnik/`). `postavke/` ne postoji.
Frontend `postavke.component.ts` je stub od 24 linije.

- [ ] `ProgramskiParametri` entity + CRUD
- [ ] `PostavkePoslovnice` entity + CRUD
- [ ] Frontend ekran

## P1.8 — Izlazne fakture ❌ (Sprint 4 — lažno označeno završenim)

Stara verzija je tvrdila `[x] Frontend — Izlazne fakture (tipKod=IF)`.
**Direktorij, ruta i komponenta ne postoje.** Backend podržava `IF` tip.

- [ ] Frontend ekran za `tipKod=IF` (veleprodaja)
- [ ] Ruta + navigacija

## P1.9 — Mrtve rute i lažni izbornici

- [ ] **`DataInitializer.ADMIN_IZBORNICI` daje 11+ izbornika** za nepostojeće
      backendove (`blagajna`, `fiskalni.*`, `izvjestaji.*`, `postavke.parametri`, `chat`).
      Ovo je glavni razlog zašto je projekt izgledao ~90% završen.
- [ ] **~15 mrtvih linkova u topbaru** — `app.routes.ts:97-100` catch-all
      `{ path: '**', redirectTo: '' }` ih tiho guta.
- [ ] **`app.routes.ts:24` `redirectTo: 'blagajna'`** — default ruta vodi na stub.

---

# P2 — KVALITET, DUG, DOKUMENTACIJA

## P2.1 — Mrtvi kod (~1600 LOC)

- [ ] `features/dokumenti/otpremnice/` — 1170 LOC, zamijenjeno sa `povrat-dobavljacu`
- [ ] `fakture.service.ts` — 80 LOC, zove nepostojeće endpointe
- [ ] `layout/sidebar` — 132 LOC, ne koristi se
- [ ] `shared/components/`: `data-table`, `search-filter`, `export-buttons`,
      `form-field-error` — ~340 LOC, zamijenjeni `crud-table`-om
- [ ] Neiskorištene ovisnosti: MapStruct (0 mappera), JasperReports (samo dokumenti/pdf),
      Apache POI, OpenCSV.
      *NE i `@stomp/stompjs`/`sockjs-client` — njih koristi frontend chat servis (P1.6).*

## P2.2 — Duplikacija (~1100-1200 LOC) — krši CLAUDE.md hard pravilo

- [ ] **`PrometStavkeComponent`** — svaki dokument ekran (fakture, povrat-dobavljacu,
      medjuskladisnica) ima vlastitu kopiju forme za stavke.
      *Ovo je bila jedina iskreno neoznačena stvar u staroj verziji plana.*
- [ ] Unificirati `*-list` komponente (identična logika filtera/paginacije/akcija)
- [ ] Unificirati `*-detail` komponente
- [ ] `DokumentService` ima 9 kolaboratora i 696 linija — razdvojiti
      (kreiranje / potvrda / storno / nivelacija / zaliha)

## P2.3 — Testovi

- [ ] Popraviti kompilaciju testova (P0.1) prije bilo čega
- [ ] Unit: `CijenaCalculator`, `PopustResolverService`, `NivelacijaService`,
      `BrojacDokumenta` (konkurentnost)
- [ ] Integracijski: **Testcontainers, ne H2** — H2 ne reproducira PostgreSQL
      constraint/sequence semantiku koja je ovdje kritična
- [ ] Auth flow, tenant izolacija (negativni testovi za svaki IDOR iz P0.4)
- [ ] Frontend: postoji **samo 1 spec** (`app.component.spec.ts`). `ng test` traži
      Chrome (`CHROME_BIN`) — lokalno se ne može pokrenuti, u CI-u da.
- [ ] E2E: Playwright je instaliran, testovi nisu napisani

## P2.4 — Pristupačnost (WCAG)

- [ ] **Nema nijednog `<h1>`** u cijeloj aplikaciji
- [ ] **Nema error state-ova nigdje** — samo loading i success
- [ ] **Hand-rolled modal bez focus trapa** na 16 ekrana → zamijeniti `MatDialog`
- [ ] Engleski `aria-label`-i u bosanskom UI-u
- [ ] `#9e9e9e` na bijelom = 2.85:1 kontrast (minimum je 4.5:1)
- [ ] Keyboard navigacija kroz tabele i forme

## P2.5 — Dokumentacija

- [x] `TASKS.md` — prepravljen (ovaj fajl)
- [x] `.claude/ARCHITECTURE.md` — uklonjeni nepostojeći paketi, popravljene kontradikcije
- [x] `README.md` — usklađen sa stvarnim modulima
- [x] `TEXTILE_ARCHITECTURE_PLAN.md` — popravljena kontradikcija o predznaku `kolicina`
- [x] `.claude/docs/api-standard.md` — bio prazan, napisan
- [x] `.claude/docs/testing.md` — bio prazan, napisan
- [x] `.claude/docs/deploy.md` — bio prazan, napisan
- [x] `.claude/docs/HANDOFF.md` — novo, za nastavak rada u novoj sesiji
- [ ] Swagger anotacije — nepotpune
- [ ] **92 `no-explicit-any` warninga** — `crud-table` još nije generičan po tipu reda.
      Pravilo je namjerno spušteno na `warn` u `eslint.config.js`; vratiti na `error`
      kad `crud-table<T>` bude tipiziran.
- [ ] ADR-ovi za otvorene odluke (vidi ispod)

## P2.6 — Infrastruktura

- [ ] `Dockerfile` backend + frontend
- [ ] `docker-compose.yml` (README ga obećava od Faze 10)
- [ ] Strukturirani logging (SLF4J + Logback, MDC za tenant)
- [ ] Health/readiness probe konfiguracija

---

# SPRINT 5 — Web shop 🔲 (ne dirati prije nego P0 i P1 prođu)

- [ ] Backend webshop modul (`WebArtikal` entity postoji, treba dopuniti)
- [ ] Frontend — Tab "Artikli web" + Tab "Slike artikala"
- [ ] Javni API `/api/public/...` sa rate limitingom
- [ ] Web korisnici (autentifikacija kupaca)
- [ ] Košarica i narudžbe
- [ ] Integracija platnih sistema

---

# Otvorene odluke (traže korisnikov input)

1. **Tenancy strategija** — Hibernate `@TenantId` (globalno enforce) vs. eksplicitni
   repo parametri. Preporuka: `@TenantId`, jer P0.4 pokazuje da konvencija ne radi.
2. **Status enum** — uskladiti FE na ASCII `POTVRDEN` (preporučeno) ili BE na `POTVRĐEN`.
3. **Chat** — implementirati ili izbaciti iz scope-a?
4. **Popust scope hijerarhija** — koji nivoi (artikal / grupa / proizvođač / dobavljač)
   i koji prioritet ako više njih važi (MAX preko svih, ili najspecifičniji pobjeđuje)?
5. **Storno semantika** — CLAUDE.md/docs kažu "ne mijenja status originala", kod
   postavlja original na `STORNIRAN`. Koje je ispravno poslovno pravilo?
6. ~~**Prioritet P1** — POS/blagajna ili fiskalizacija prvo?~~
   ✅ **ODLUČENO (2026-09-17, korisnik):** prvo **P0**, zatim **POS/blagajna**,
   zatim **fiskalizacija**.

---

# Napredak — stvarni

| Faza / Sprint | Prethodno tvrđeno | Stvarno |
|---|---|---|
| 1 — Infrastruktura | ✅ | 🟡 CI ✅ (2026-09-18); nema Docker, export paketa |
| 2 — Auth & RBAC | ✅ | 🟡 Radi, ali refresh=access, fiktivni logout, IDOR |
| 3 — Šifrarnici | ✅ | 🟡 CRUD radi; export/import servisi ❌ |
| 4 — Blagajna | ✅ | ❌ **0% — paket nikad nije postojao** |
| 5 — Fiskalizacija | ✅ | ❌ **0% — paket nikad nije postojao** |
| 6 — Dokumenti (stara) | ✅ zamijenjena | ✅ zamijenjena Prometom |
| 7 — Izvještaji | ✅ | ❌ **0% — samo dokumenti/pdf postoji** |
| 8 — Chat | ✅ | ❌ **0% backend**; FE servis postoji ali nema server |
| 9 — Postavke | ✅ | 🟡 korisnici ✅ (u `auth/`), postavke ❌ |
| Textile Sprint 1 | ✅ | ✅ |
| Textile Sprint 2 | ✅ | ✅ |
| Textile Sprint 3 | ✅ | 🟡 UNIQUE constraint ne uključuje `id_boje` |
| Textile Sprint 4 (Promet) | ✅ | 🟡 BE ✅; FE: Izlazne fakture ❌, status mismatch |
| Sprint 5 — Web shop | 🔲 | 🔲 |
| Faza 10 — Deployment | 🔲 | 🔲 |

**Ukupno: ~30%** (18 od 63 sposobnosti iz specifikacije potpuno implementirano).
