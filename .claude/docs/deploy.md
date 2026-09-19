# Deployment

> ## 🚫 Aplikacija se trenutno NE MOŽE deployati u produkciju
>
> Ovaj dokument opisuje **šta mora biti riješeno** i **ciljni proces**.
> Ne postoji `Dockerfile`, `docker-compose.yml`, CI pipeline ni deployment skripta.

---

## Blokatori — moraju biti riješeni prije prvog deploya

### 1. Prod profil se ne diže

`application-prod.yml` ima `ddl-auto: validate` + Flyway enabled. Validacija pada jer
`popust_procenat` postoji u entitetima `ArtikalKompanija` i `ArtikalPoslovnica` a ne
postoji u `V1__init_schema.sql`. → nova `V2__` migracija (`TASKS.md` P0.2).

### 2. Dev kod se pokreće u produkciji

Nijedan `@Profile` guard ne postoji u backendu:

| Komponenta | Šta radi u produkciji |
|---|---|
| `DataInitializer` | Seeda `admin`, `superadmin1`, `superadmin2` sa passwordom `Admin123!` + fiktivne kompanije |
| `SchemaFixer` | Raw JDBC `ALTER TABLE korisnici DROP CONSTRAINT` — uklanja CHECK na ulogu |

Oba moraju dobiti `@Profile("dev")`; `SchemaFixer` treba zamijeniti Flyway migracijom.

### 3. JWT secret

`application.yml:50`: `${JWT_SECRET:changeme-minimum-32-characters-long-secret-key}` —
default je commitovan u git i **prod profil ga ne override-a**. Ukloniti default u
potpunosti da aplikacija fail-fast pukne bez env varijable.

### 4. Build

Backend pada na JDK 24+ (Lombok 1.18.34). `ng build` pada na SCSS budget-u.

### 5. Nema fiskalizacije

**Zakonski blokator za maloprodaju u BiH.** Sistem ne može legalno izdati račun.

---

## Obavezne env varijable u produkciji

| Varijabla | Obavezno | Napomena |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | ✅ | `prod` |
| `DB_URL` | ✅ | `jdbc:postgresql://host:5432/maloprodaja` |
| `DB_USERNAME` | ✅ | ne `postgres` |
| `DB_PASSWORD` | ✅ | iz secret store-a |
| `DB_POOL_SIZE` | — | default 10; podesiti prema broju poslovnica |
| `JWT_SECRET` | ✅ | min 32 znaka, random, **nikad u gitu** |
| `JWT_EXPIRATION_MS` | — | preporuka 900000 (15 min), ne 1h |
| `JWT_REFRESH_EXPIRATION_MS` | — | 86400000 (24h) |
| `CORS_ALLOWED_ORIGINS` | ✅ | eksplicitna domena, nikad `*` |
| `SWAGGER_ENABLED` | ✅ | `false` |
| `SERVER_PORT` | — | default 8080 |

Nikad ne stavljaj vrijednosti u `application-prod.yml`. Sve iz env-a / secret store-a.

---

## Ciljna arhitektura deploya

```
        Nginx / reverse proxy (TLS)
                  │
        ┌─────────┴─────────┐
        │                   │
  Angular static      Spring Boot API
  (nginx serve)        (JAR, port 8080)
                             │
                       PostgreSQL 16
                     (managed, backup)
```

- Frontend: `ng build --configuration production` → static fileovi u nginx
- Backend: fat JAR ili Docker image, stateless (JWT), horizontalno skalabilan
  > ⚠️ **Nije još stateless-safe** — nedostaje `@Version` i lock na brojačima, pa
  > dvije instance mogu generirati duple brojeve dokumenata. Do tada: **jedna instanca**.
- Baza: managed PostgreSQL sa automatskim backupom i point-in-time recovery
  (dokumenti i nivelacije su zakonska evidencija)

---

## Migracije baze (Flyway)

- **`V1__init_schema.sql` je već primijenjen — nikad ga ne uređuj.** Izmjena mijenja
  checksum i lomi Flyway na svakom postojećem okruženju.
- Sve nove izmjene idu u `V2__`, `V3__`, ... sa opisnim imenom:
  `V2__dodaj_popust_procenat.sql`
- Migracija mora biti **idempotentna gdje je moguće** i **backward compatible** u
  jednom koraku (expand → migrate → contract) ako se deploya bez downtime-a
- `flyway.baseline-on-migrate` **nikad `true`** u produkciji
- Prije deploya: `mvn flyway:validate` na kopiji prod baze

---

## Redoslijed prvog deploya

1. Kreiraj bazu i korisnika sa minimalnim privilegijama
2. Postavi sve env varijable iz tabele iznad
3. Pokreni backend — Flyway primjeni `V1..Vn`, `validate` prođe
4. Provjeri `GET /actuator/health` → `{"status":"UP"}`
5. Provjeri da `/swagger-ui.html` vraća `404` (Swagger off)
6. Provjeri da dev korisnici (`admin`, `superadmin1`, `superadmin2`) **ne postoje**
7. Kreiraj prvu kompaniju, poslovnicu i administratora ručno (kroz kontroliranu
   seed skriptu, ne `DataInitializer`)
8. Deploy frontend static build
9. Smoke test: login → šifarnici → kreiranje ulazne fakture → potvrda

---

## Rollback

- Aplikacija: prethodni JAR / image tag
- Baza: **Flyway migracije se ne rollbackaju automatski.** Za svaku destruktivnu
  migraciju treba pripremiti ručnu `down` skriptu i testirati je na kopiji prije deploya.
  Preferiraj aditivne migracije (dodaj kolonu, ne preimenuj/briši) da rollback aplikacije
  ne zahtijeva rollback baze.

---

## Observability — treba napraviti

- Strukturirani JSON logging (Logback encoder)
- MDC sa `idKompanije`, `idKorisnika`, `correlationId` na svakom requestu
- `correlationId` se vraća u `ApiResponse` (vidi `api-standard.md` §3)
- Aktuator: `health`, `info`, `metrics` izloženi; ostalo zatvoreno
- Alarmi: `5xx` rate, DB pool exhaustion, Flyway failure na startu
