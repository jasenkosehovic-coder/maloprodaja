# Maloprodaja — SaaS Maloprodajni Informacioni Sistem

Web aplikacija za upravljanje maloprodajom, blagajnom, dokumentima i izvještajima.
Multi-tenant SaaS arhitektura — jedna instalacija za više kompanija i poslovnica.

> ## ⚠️ Status projekta: ~30% — RAZVOJ U TOKU, NIJE ZA PRODUKCIJU
>
> Verificirano 2026-09-17. Prije ovog datuma dokumentacija je navodila module
> (Blagajna, Fiskalizacija, Izvještaji, Chat, Postavke) **za koje ne postoji kod**.
>
> **Trenutna ograničenja:**
> - **Build pada na oba stack-a** — vidi [Pokretanje lokalno](#pokretanje-lokalno)
> - **Produkcijski profil se ne može pokrenuti** (entity/DDL drift)
> - **Nema POS/blagajne** — sistem trenutno ne može prodati artikal
> - **Nema fiskalizacije** — zakonski blokator za rad u BiH
> - **Nema izvještaja, export/import-a, chata, postavki**
> - Dev seed korisnici i `SchemaFixer` se pokreću i u produkciji (nema `@Profile` guarda)
>
> **Šta radi:** autentifikacija/RBAC, šifarnici (artikli, varijante, boje, veličine,
> atributi, barkodovi, kupci, dobavljači, proizvođači, grupe), Promet dokumenti
> (ulazne fakture, povrat dobavljaču, međuskladišnica), nivelacije, PDF dokumenata.
>
> **Kompletna lista defekata i prioriteta:** [`TASKS.md`](TASKS.md)
> **Arhitektura i stvarna struktura koda:** [`.claude/ARCHITECTURE.md`](.claude/ARCHITECTURE.md)
> **Nastavak rada / onboarding:** [`.claude/docs/HANDOFF.md`](.claude/docs/HANDOFF.md)

---

## Sadržaj

- [Pregled projekta](#pregled-projekta)
- [Tech Stack](#tech-stack)
- [Arhitektura](#arhitektura)
- [Preduvjeti](#preduvjeti)
- [Pokretanje lokalno](#pokretanje-lokalno)
  - [Baza podataka](#1-baza-podataka)
  - [Backend](#2-backend)
  - [Frontend](#3-frontend)
- [Pokretanje sa Docker Compose](#pokretanje-sa-docker-compose)
- [Swagger API dokumentacija](#swagger-api-dokumentacija)
- [Struktura projekta](#struktura-projekta)
- [Environment varijable](#environment-varijable)
- [Moduli aplikacije](#moduli-aplikacije)
- [Uloge korisnika](#uloge-korisnika)

---

## Pregled projekta

Maloprodaja je SaaS web aplikacija namijenjena maloprodajnim objektima u BiH.
Podržava više kompanija i poslovnica na jednoj instalaciji.

**Funkcionalnosti — implementirano:**
- Autentifikacija (JWT) i RBAC sa individualnim override izbornika
- Šifrarnici: artikli, varijante (veličina × boja), boje, tipovi veličina, atributi,
  barkodovi, grupe, kupci, dobavljači, proizvođači, popusti (CRUD samo)
- Promet dokumenti: ulazne fakture (UF), povrat dobavljaču (PD),
  međuskladišnica (MSI+MSU atomično) — nacrt / potvrda / storno
- Nivelacije (izmjena MPC/VPC)
- PDF dokumenata (JasperReports)

**Funkcionalnosti — planirano, kod ne postoji:**
- Blagajna / POS (prodaja, posudbe, ponude, veleprodaja, storno, zaključivanje dana)
- Fiskalizacija (EPSON E-link, Tring fiskalni server)
- Izlazne fakture — frontend (backend tip `IF` postoji)
- Izvještaji (knjiga blagajni, lager lista, stanje zaliha, trgovačka knjiga...)
- Export/Import (Excel, CSV)
- Realtime chat između poslovnica
- Postavke (programski parametri, postavke poslovnice)
- Pravilo popusta (resolver po MAX pravilu — vidi `.claude/CLAUDE.md`)

---

## Tech Stack

### Backend
| Tehnologija | Verzija | Namjena |
|---|---|---|
| Java | 21 LTS | Programski jezik |
| Spring Boot | 3.3.5 | Framework |
| Spring Security | 6.x | Autentifikacija / Autorizacija |
| Spring Data JPA | 3.x | ORM sloj |
| Spring WebSocket | 6.x | Realtime chat (STOMP) — ⚠️ deklarirano, neiskorišteno |
| Hibernate | 6.x | JPA implementacija |
| PostgreSQL | 16 | Baza podataka |
| Flyway | 10.x | Migracije baze |
| JWT (jjwt) | 0.12.6 | Token autentifikacija |
| MapStruct | 1.5.5 | DTO mapiranje — ⚠️ deklarirano, 0 mappera |
| Lombok | 1.18.34 | Boilerplate redukcija |
| Apache POI | 5.3.0 | Excel export/import — ⚠️ deklarirano, neiskorišteno |
| JasperReports | 6.21.3 | PDF (samo `dokumenti/pdf`) |
| OpenCSV | 5.9 | CSV export — ⚠️ deklarirano, neiskorišteno |
| SpringDoc OpenAPI | 2.6.0 | Swagger UI |
| Maven | 3.9.x | Build alat |

### Frontend
| Tehnologija | Verzija | Namjena |
|---|---|---|
| Angular | 18 LTS | SPA framework |
| Angular Material | 18 | UI komponente |
| TypeScript | 5.x | Programski jezik |
| RxJS | 7.x | Reaktivno programiranje |
| @stomp/stompjs | 7.x | WebSocket / STOMP klijent — koristi ga `chat-websocket.service.ts`, ali backend `chat` ne postoji |
| SCSS | - | Stilizacija |

### Infrastruktura
| Tehnologija | Namjena |
|---|---|
| Docker | Kontejnerizacija |
| Docker Compose | Lokalni razvoj |
| PostgreSQL 16 | Baza podataka |
| Nginx | Serving Angular aplikacije (produkcija) |

---

## Arhitektura

```
┌─────────────────────────────────────────────┐
│              Angular Frontend               │
│         (SPA — localhost:4200)              │
└──────────────────┬──────────────────────────┘
                   │ HTTP REST / WebSocket
┌──────────────────▼──────────────────────────┐
│           Spring Boot Backend               │
│         (REST API — localhost:8080)         │
│                                             │
│  ┌─────────┐  ┌──────────┐  ┌───────────┐  │
│  │ Auth /  │  │ Business │  │ WebSocket │  │
│  │Security │  │ Services │  │   Chat    │  │
│  └─────────┘  └──────────┘  └───────────┘  │
└──────────────────┬──────────────────────────┘
                   │ JPA / Hibernate
┌──────────────────▼──────────────────────────┐
│             PostgreSQL 16                   │
│           (localhost:5432)                  │
└─────────────────────────────────────────────┘
```

**Multi-tenant model:** Kompanija → Poslovnica → Korisnik
Svaki zapis u bazi nosi `id_kompanije` i `id_poslovnice` za izolaciju podataka.

---

## Preduvjeti

### Lokalni razvoj
- **Java 21+** — [Preuzmi](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.9+** — [Preuzmi](https://maven.apache.org/download.cgi)
- **Node.js 20+** — [Preuzmi](https://nodejs.org/)
- **PostgreSQL 16** — lokalno ili putem Dockera (vidi ispod)

### Docker pokretanje
- **Docker Desktop** — [Preuzmi](https://www.docker.com/products/docker-desktop/)

---

## Pokretanje lokalno

### 1. Baza podataka

#### Opcija A — Docker (preporučeno)
```bash
docker run -d \
  --name postgres-maloprodaja \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=maloprodaja_dev \
  -p 5432:5432 \
  postgres:16
```

#### Opcija B — WSL2 Docker (Windows)
```bash
# Pokretanje postojećeg containera
docker start postgres-db

# Kreiranje baze ako ne postoji
docker exec -i postgres-db psql -U postgres -c "CREATE DATABASE maloprodaja_dev;"
```

#### Opcija C — lokalni PostgreSQL
```bash
psql -U postgres -c "CREATE DATABASE maloprodaja_dev;"
```

---

### 2. Backend

> ⚠️ **Build pada sa Lombok 1.18.34 na JDK 24+** (`TypeTag :: UNKNOWN`).
> Dok `pom.xml` ne bude ažuriran, koristi override:
> `mvn -Dlombok.version=1.18.42 spring-boot:run -Dspring-boot.run.profiles=dev`

```bash
cd backend

# Pokretanje sa dev profilom
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

> ⚠️ **Dev profil ne koristi Flyway** (`ddl-auto: create`, `flyway.enabled: false`) —
> shema se re-kreira iz entiteta pri svakom restartu. `V1__init_schema.sql` se
> izvršava samo u prod profilu, gdje trenutno **ne prolazi validaciju**.
>
> ⚠️ `mvn test` **ne kompajlira se** — `DokumentServiceTest.java:90` je zaostao za DTO-om.

Backend se pokreće na: **http://localhost:8080**

#### Provjera
```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

---

### 3. Frontend

```bash
cd frontend

# Instalacija zavisnosti (samo prvi put)
npm install

# Pokretanje dev servera
npm start
# ili
npx ng serve --proxy-config proxy.conf.json
```

Frontend se pokreće na: **http://localhost:4200**

> `proxy.conf.json` automatski preusmjerava `/api` i `/ws` pozive na backend `localhost:8080`.

> ✅ `ng serve`, `ng lint` (0 errora) i `ng build --configuration production` rade.
> `npm test` zahtijeva Chrome (`CHROME_BIN`) — izvršava se u CI-u.

### Dev pristupni podaci

Seedani od `DataInitializer` (dev-only namjena, ali **trenutno se pokreće i u produkciji**):
`admin` / `superadmin1` / `superadmin2`, password `Admin123!`.

---

## Pokretanje sa Docker Compose

> ⚠️ **Ne postoji.** Nema `Dockerfile` ni `docker-compose.yml` u repozitoriju.
> Komande ispod su ciljno stanje, ne trenutno. Vidi `TASKS.md` P2.6.

```bash
# Pokretanje svih servisa (backend + frontend + baza)
docker compose up -d

# Zaustavljanje
docker compose down

# Zaustavljanje + brisanje podataka
docker compose down -v
```

Servisi nakon pokretanja:
| Servis | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |

---

## Swagger API dokumentacija

Swagger UI je dostupan na:

```
http://localhost:8080/swagger-ui.html
```

API docs (JSON):
```
http://localhost:8080/api-docs
```

> U produkcijskom profilu Swagger je isključen (`SWAGGER_ENABLED=false`).

### Autentifikacija u Swagger UI
1. Otvori `http://localhost:8080/swagger-ui.html`
2. Klikni **Authorize** dugme (ikona katanca)
3. Unesi token u formatu: `Bearer <tvoj-jwt-token>`
4. Token dobijaš sa `POST /api/auth/login`

---

## Struktura projekta

```
Maloprodaja/
├── backend/                          # Spring Boot aplikacija
│   ├── src/main/java/ba/maloprodaja/
│   │   ├── MaloprodajaApplication.java
│   │   ├── common/                   # Infrastrukturni kod
│   │   │   ├── entity/BaseEntity.java
│   │   │   ├── dto/ApiResponse.java
│   │   │   ├── exception/
│   │   │   ├── security/
│   │   │   ├── audit/
│   │   │   ├── tenant/TenantContext.java
│   │   │   ├── export/
│   │   │   └── websocket/
│   │   ├── auth/                     # JWT autentifikacija
│   │   ├── korisnik/                 # Upravljanje korisnicima
│   │   ├── sifarnici/                # Artikli, kupci, dobavljači...
│   │   ├── blagajna/                 # Prodaja, storno, posudbe...
│   │   ├── dokumenti/                # Fakture, nivelacije, otpremnice
│   │   ├── fiskalizacija/            # EPSON / Tring fiskalni printeri
│   │   ├── izvjestaji/               # Izvještaji i analize
│   │   ├── chat/                     # WebSocket chat
│   │   └── postavke/                 # Programski parametri
│   └── src/main/resources/
│       ├── application.yml           # Glavna konfiguracija
│       ├── application-dev.yml       # Dev profil
│       ├── application-prod.yml      # Prod profil
│       ├── db/migration/             # Flyway SQL migracije
│       ├── reports/jasper/           # JasperReports predlošci
│       └── templates/excel/          # Excel import predlošci
│
├── frontend/                         # Angular aplikacija
│   ├── src/app/
│   │   ├── core/                     # Auth, guards, interceptors
│   │   ├── shared/                   # Reusable komponente, pipes, direktive
│   │   ├── layout/                   # Main layout, topbar, sidebar
│   │   └── features/                 # Feature moduli (lazy loaded)
│   │       ├── auth/
│   │       ├── sifarnici/
│   │       ├── blagajna/
│   │       ├── dokumenti/
│   │       ├── fiskalni/
│   │       ├── izvjestaji/
│   │       ├── chat/
│   │       ├── korisnici/
│   │       └── postavke/
│   ├── src/environments/
│   ├── src/styles/                   # SCSS tema i varijable
│   └── proxy.conf.json               # Dev proxy na backend
│
├── .claude/
│   ├── CLAUDE.md                     # Pravila za Claude Code agente + poslovna pravila
│   ├── ARCHITECTURE.md               # Stvarna struktura koda i poznata odstupanja
│   └── docs/
│       ├── HANDOFF.md                # Kontekst za nastavak rada u novoj sesiji
│       ├── api-standard.md           # REST API konvencije
│       ├── testing.md                # Test strategija
│       └── deploy.md                 # Deployment i konfiguracija
├── TASKS.md                          # Prioritizirana lista rada (P0/P1/P2)
├── TEXTILE_ARCHITECTURE_PLAN.md      # Historijski dizajn dokument (Sprint 1-4)
└── README.md                         # Ovaj fajl
```

> ⚠️ Stablo `backend/src/main/java` iznad prikazuje **ciljnu** strukturu.
> Paketi `blagajna/`, `fiskalizacija/`, `izvjestaji/`, `chat/`, `postavke/`, `korisnik/`
> **ne postoje**. Za stvarnu strukturu vidi [`.claude/ARCHITECTURE.md`](.claude/ARCHITECTURE.md).

---

## Environment varijable

### Backend (`application.yml`)

| Varijabla | Default (dev) | Opis |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/maloprodaja_dev` | JDBC URL baze |
| `DB_USERNAME` | `postgres` | Korisničko ime baze |
| `DB_PASSWORD` | `postgres` | Lozinka baze |
| `DB_POOL_SIZE` | `10` | Max veličina connection pool-a |
| `JWT_SECRET` | `changeme-...` | Tajni ključ za JWT (min 32 znaka). ⚠️ **Default je u gitu i prod profil ga ne override-a — sigurnosni defekt, `TASKS.md` P0.3** |
| `JWT_EXPIRATION_MS` | `3600000` | Trajanje JWT tokena (1h) |
| `JWT_REFRESH_EXPIRATION_MS` | `86400000` | Trajanje refresh tokena (24h) |
| `JWT_INACTIVITY_MS` | `3600000` | Logout pri neaktivnosti (1h) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Dozvoljeni CORS origini |
| `SERVER_PORT` | `8080` | Port servera |
| `SWAGGER_ENABLED` | `true` | Swagger UI (false u prod) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Aktivni profil |

### Frontend (`environment.ts`)

| Varijabla | Dev | Opis |
|---|---|---|
| `apiUrl` | `http://localhost:4200/api` | Base URL (proxy) |
| `wsUrl` | `http://localhost:4200/ws` | WebSocket URL (proxy) |
| `production` | `false` | Production mode flag |

---

## Moduli aplikacije

| Modul | Status | Opis |
|---|---|---|
| **Šifrarnici** | ✅ | Artikli, varijante, boje, veličine, atributi, barkodovi, grupe, proizvođači, dobavljači, kupci, popusti (CRUD) |
| **Korisnici** | ✅ | CRUD + RBAC (rola + individualni izbornici). Kod je u `auth/` paketu |
| **Dokumenti (Promet)** | 🟡 | UF, PD, MSI/MSU + auto-nivelacija. Izlazne fakture (IF) nemaju frontend |
| **Nivelacije** | 🟡 | Rade, ali `vpcStara == vpcNova` i ručna nivelacija ima `iznos = 0` |
| **Blagajna** | ❌ | Nema koda. Frontend je stub od 25 linija |
| **Fiskalizacija** | ❌ | Nema koda. Zakonski blokator za rad u BiH |
| **Izvještaji** | ❌ | Nema koda |
| **Export/Import** | ❌ | Nema koda (POI/OpenCSV su deklarirani neiskorišteno) |
| **Chat** | ❌ | Nema koda |
| **Postavke** | ❌ | Nema koda |

> Detalji i prioriteti u [`TASKS.md`](TASKS.md).

---

## Uloge korisnika

| Uloga | Pristup |
|---|---|
| **Administrator** | Sve poslovnice kompanije, svi moduli, izvještaji za cijelu kompaniju |
| **Blagajnik** | Blagajna ili korisnik, ograničeni šifrarnici |
| **Knjigovodja** | Dokumenti, fakture, izvještaji, šifrarnici |
| **Menadžer** | izvještaji, šifrarnici |

> Administrator može ručno dodati ili oduzeti pojedinačne izbornike svakom korisniku — sprema se u bazu.
