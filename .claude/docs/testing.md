# Test strategija

---

## Trenutno stanje (2026-09-17)

| Sloj | Stanje |
|---|---|
| Backend unit | 46 testova napisano, **0 se izvršava** |
| Backend integracijski | ❌ nema |
| Frontend unit | Karma/Jasmine konfiguriran, coverage nije mjeren |
| Frontend E2E | Playwright instaliran, **0 testova napisano** |
| Lint | `ng lint` **nije konfiguriran** |
| CI | ❌ **ne postoji** |

**Blokator #1:** `DokumentServiceTest.java:90` poziva `CreateDokumentDTO` bez
`brojFakture` argumenta → test source set se ne kompajlira → **svih 46 testova pada
prije izvršavanja**. Ovo mora biti popravljeno prvo (`TASKS.md` P0.1); do tada nijedna
tvrdnja o pokrivenosti nije provjerljiva.

**Blokator #2:** backend build pada na JDK 24+ (Lombok 1.18.34).

---

## Piramida

```
        E2E (Playwright)        — 5-10 kritičnih tokova, ne više
     Integracijski (Testcontainers) — svaki endpoint, tenant izolacija
  Unit (JUnit 5 + Mockito / Jasmine) — sva poslovna logika i kalkulacije
```

---

## Backend

### Unit testovi

Mockito, bez Spring konteksta. Obavezni za:

- **`CijenaCalculator`** — MPC formula. Trenutno postoje **tri divergentne
  implementacije**, jedna pogrešna (PDV na goli VPC → podcjenjuje ~4.8%).
  Napiši zlatne testove **prije** unifikacije, sa tablicom
  `(vpc, marža, pdv) → očekivani MPC`, uključujući `vpc=100, marža=50, pdv=17 → 175.50`.
- **`PopustResolverService`** — MAX pravilo (nikad zbir), sva tri izvora, sve
  kombinacije, granice datuma (`datumOd`, `datumDo`, inclusive/exclusive).
- **`NivelacijaService`** — `vpcStara ≠ vpcNova`, `iznosNivelacije` izračunat.
- **`DokumentService`** — potvrda, storno, međuskladišnica, predznak količine
  (`kolicina * smjerKolicine`), izolacija nivelacijskog failure-a.
- **`BrojacDokumenta`** — format `KOD-YYYY-NNNN`, reset po godini, konkurentnost.

Pravila:
- Jedan `@Test` = jedna tvrdnja o ponašanju
- `@DisplayName` na bosanskom, opisuje poslovno pravilo, ne metodu
- Nikad ne mockaj klasu koja se testira; nikad ne testiraj gettere
- `BigDecimal` poredi sa `compareTo` / `assertThat(x).isEqualByComparingTo(y)`,
  **nikad `equals`** (scale)

### Integracijski testovi — Testcontainers, ne H2

**H2 je zabranjen.** Ovaj projekt ovisi o PostgreSQL semantici koju H2 ne reproducira:
partial unique indexi, `NUMERIC` scale ponašanje, sekvence, `ON CONFLICT`,
constraint deferral, i sam Flyway `V1__init_schema.sql`. Test koji prođe na H2 a padne
na Postgresu je gori od nenapisanog testa.

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
abstract class IntegrationTestBase {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
}
```

`application-test.yml` mora imati **`ddl-auto: validate` + Flyway enabled** — to je
jedino mjesto gdje se entity/DDL drift uhvati automatski. (Dev profil ga ne hvata jer
koristi `ddl-auto: create` bez Flyway-a.)

Obavezno pokriveno:
- **Tenant izolacija — negativan test za svaki tenant-scoped endpoint.** Seedaj dva
  tenanta, autentificiraj se kao tenant A, pokušaj čitati i mijenjati zapis tenanta B,
  očekuj `404`. Ovo je regresijski štit za IDOR-e iz `TASKS.md` P0.4, uključujući
  `KorisnikService.update` (promjena uloge/passworda tuđeg korisnika).
- **Auth flow** — login, refresh (refresh token **ne smije** raditi kao access token),
  logout, expiry, RBAC po roli.
- **Flyway migracija se primjenjuje čisto** na praznu bazu.
- **Konkurentnost** — dvije paralelne potvrde istog dokumenta / dvije potvrde koje
  diraju istu zalihu; očekuj `409`, ne izgubljenu promjenu.
- **Zaliha ne može u minus.**

---

## Frontend

### Unit (Jasmine + Karma)

- Servisi: `HttpTestingController`, provjeri URL, verb, body i mapiranje odgovora
- Komponente: samo javno ponašanje (input → render, klik → output), ne interna stanja
- Signals: testiraj izvedene vrijednosti, ne implementaciju
- **Kontraktni test za enume** — assert da su literal vrijednosti u
  `promet.models.ts` identične backend enumu (`POTVRDEN`, ASCII). Ovo bi uhvatilo
  `'POTVRĐEN'` bug.

### E2E (Playwright) — samo kritični tokovi

1. Login → dashboard → logout
2. Kreiranje artikla sa varijantama (veličina × boja) + barkod
3. Ulazna faktura: nacrt → dodavanje stavki → potvrda → provjera zalihe
4. Povrat dobavljaču: potvrda → provjera da je zaliha smanjena
5. Međuskladišnica: potvrda → zaliha smanjena u izvoru, povećana u cilju
6. Storno dokumenta → provjera stanja zalihe i statusa
7. (kad postoji) Prodaja na kasi → račun → fiskalizacija

Pravila:
- Nikad `page.waitForTimeout` — koristi `expect(locator).toBeVisible()` auto-wait
- Selektori: `getByRole` / `getByLabel`, nikad CSS klase
- Svaki test seeda i čisti svoje podatke; testovi ne smiju zavisiti od redoslijeda

### Pristupačnost

`@axe-core/playwright` na svakom E2E ekranu. Poznati failovi koje treba popraviti
prije nego axe postane blocking (`TASKS.md` P2.4): nema `<h1>`, hand-rolled modal bez
focus trapa na 16 ekrana, kontrast `#9e9e9e` = 2.85:1, engleski `aria-label`-i.

---

## CI (GitHub Actions) — treba napraviti

```
.github/workflows/ci.yml
  backend:
    - setup-java 21 (temurin)
    - mvn -B verify            # unit + Testcontainers integracijski
  frontend:
    - setup-node 20
    - npm ci
    - npx ng lint
    - npx ng build --configuration production
    - npx ng test --no-watch --browsers=ChromeHeadless --code-coverage
  e2e:
    - needs: [backend, frontend]
    - npx playwright test
```

Merge u `master` je blokiran dok sva tri joba ne prođu.

---

## Definicija završenog taska

Task nije završen dok:

1. Postoje unit testovi za novu poslovnu logiku
2. Postoji integracijski test za novi endpoint, **uključujući negativan tenant test**
3. `mvn verify` prolazi
4. `ng lint && ng build && ng test` prolazi
5. Ako task dira UI: ekran je otvoren u browseru i ručno provjeren (golden path +
   prazno stanje + error stanje)
6. Ako task popravlja bug: postoji test koji pada bez popravke

---

## Pokretanje

```bash
# Backend — dok pom.xml nije popravljen, treba Lombok override
cd backend && mvn -Dlombok.version=1.18.42 verify

# Jedan test
mvn -Dlombok.version=1.18.42 -Dtest=DokumentServiceTest test

# Frontend
cd frontend && npx ng test --no-watch --code-coverage
npx playwright test
npx playwright test --ui     # debug
```
