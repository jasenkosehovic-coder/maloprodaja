# Maloprodaja — SaaS Maloprodajni Informacioni Sistem

Web aplikacija za upravljanje maloprodajom, blagajnom, dokumentima i izvještajima.
Multi-tenant SaaS arhitektura — jedna instalacija za više kompanija i poslovnica.

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

**Ključne funkcionalnosti:**
- Šifrarnici (artikli, kupci, dobavljači, proizvođači)
- Blagajna (prodaja, posudbe, ponude, veleprodaja, storno)
- Fiskalizacija (EPSON E-link, Tring fiskalni server)
- Dokumenti (ulazne fakture, nivelacije, otpremnice)
- Izvještaji (knjiga blagajni, lager lista, stanje zaliha...)
- Realtime chat između poslovnica (WebSocket)
- Export/Import (Excel, CSV, PDF)
- Role-based access control (RBAC) sa individualnim override izbornika

---

## Tech Stack

### Backend
| Tehnologija | Verzija | Namjena |
|---|---|---|
| Java | 21 LTS | Programski jezik |
| Spring Boot | 3.3.5 | Framework |
| Spring Security | 6.x | Autentifikacija / Autorizacija |
| Spring Data JPA | 3.x | ORM sloj |
| Spring WebSocket | 6.x | Realtime chat (STOMP) |
| Hibernate | 6.x | JPA implementacija |
| PostgreSQL | 16 | Baza podataka |
| Flyway | 10.x | Migracije baze |
| JWT (jjwt) | 0.12.6 | Token autentifikacija |
| MapStruct | 1.5.5 | DTO mapiranje |
| Lombok | 1.18.34 | Boilerplate redukcija |
| Apache POI | 5.3.0 | Excel export/import |
| JasperReports | 6.21.3 | PDF izvještaji |
| OpenCSV | 5.9 | CSV export |
| SpringDoc OpenAPI | 2.6.0 | Swagger UI |
| Maven | 3.9.x | Build alat |

### Frontend
| Tehnologija | Verzija | Namjena |
|---|---|---|
| Angular | 18 LTS | SPA framework |
| Angular Material | 18 | UI komponente |
| TypeScript | 5.x | Programski jezik |
| RxJS | 7.x | Reaktivno programiranje |
| @stomp/stompjs | 7.x | WebSocket / STOMP klijent |
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

```bash
cd backend

# Pokretanje sa dev profilom
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

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

---

## Pokretanje sa Docker Compose

> **Napomena:** Docker Compose fajl bit će dodan u Fazi 10.

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
├── CLAUDE.md                         # Pravila za Claude Code agente
├── TASKS.md                          # Lista taskova i napredak
└── README.md                         # Ovaj fajl
```

---

## Environment varijable

### Backend (`application.yml`)

| Varijabla | Default (dev) | Opis |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/maloprodaja_dev` | JDBC URL baze |
| `DB_USERNAME` | `postgres` | Korisničko ime baze |
| `DB_PASSWORD` | `postgres` | Lozinka baze |
| `DB_POOL_SIZE` | `10` | Max veličina connection pool-a |
| `JWT_SECRET` | `changeme-...` | Tajni ključ za JWT (min 32 znaka) |
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

| Modul | Opis |
|---|---|
| **Šifrarnici** | Artikli, grupe, barkodovi, proizvođači, dobavljači, kupci, popusti |
| **Blagajna** | Prodaja, storno/reklamacija, posudbe, ponude, veleprodaja, zaključivanje dana |
| **Fiskalizacija** | EPSON (E-link server), Tring (fiscal server) — extensible interface |
| **Dokumenti** | Ulazne fakture + auto-nivelacija, otpremnice (međuposlovnični transfer), nivelacije |
| **Izvještaji** | Knjiga blagajni, lager lista, stanje zaliha, trgovačka knjiga, promet artikala... |
| **Chat** | Realtime WebSocket chat između poslovnica |
| **Korisnici** | CRUD + RBAC (rola + individualni izbornici) |
| **Postavke** | Programski parametri, postavke poslovnice, fiskalni printeri |

---

## Uloge korisnika

| Uloga | Pristup |
|---|---|
| **Administrator** | Sve poslovnice kompanije, svi moduli, izvještaji za cijelu kompaniju |
| **Korisnik** | Blagajna, ograničeni šifrarnici |
| **Knjigovodja** | Dokumenti, fakture, izvještaji, šifrarnici |

> Administrator može ručno dodati ili oduzeti pojedinačne izbornike svakom korisniku — sprema se u bazu.
