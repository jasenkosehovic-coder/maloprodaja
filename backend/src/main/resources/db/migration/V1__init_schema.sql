-- V1__init_schema.sql
-- Consolidated initial schema (merged from V1-V18)

-- -------------------------------------------------------
-- kompanije
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS kompanije
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(200) NOT NULL,
    pib               VARCHAR(50)  UNIQUE,
    adresa            VARCHAR(300),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    grad              VARCHAR(100),
    telefon           VARCHAR(30),
    email             VARCHAR(150),
    web               VARCHAR(200),
    logo              BYTEA,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_modified_date TIMESTAMP,
    sys_created_by    BIGINT,
    sys_modified_by   BIGINT
);

-- -------------------------------------------------------
-- poslovnice
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS poslovnice
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(200) NOT NULL,
    adresa            VARCHAR(300),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL REFERENCES kompanije(id),
    grad              VARCHAR(100),
    telefon           VARCHAR(30),
    email             VARCHAR(150),
    pib               VARCHAR(50),
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_modified_date TIMESTAMP,
    sys_created_by    BIGINT,
    sys_modified_by   BIGINT
);

-- -------------------------------------------------------
-- korisnici
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS korisnici
(
    id                BIGSERIAL    PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    ime               VARCHAR(100),
    prezime           VARCHAR(100),
    email             VARCHAR(150) UNIQUE,
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    aktivan_do        DATE,
    uloga             VARCHAR(20)  NOT NULL,
    id_kompanije      BIGINT       NOT NULL REFERENCES kompanije(id),
    id_poslovnice     BIGINT       REFERENCES poslovnice(id),
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT korisnici_uloga_check
        CHECK (uloga IN ('SUPER_ADMIN', 'ADMIN', 'MENADZER', 'BLAGAJNIK', 'KNJIGOVODJA'))
);

-- -------------------------------------------------------
-- korisnik_izbornici
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS korisnik_izbornici
(
    id                BIGSERIAL    PRIMARY KEY,
    korisnik_id       BIGINT       NOT NULL REFERENCES korisnici(id),
    izbornik_kljuc    VARCHAR(50)  NOT NULL,
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_korisnik_izbornik UNIQUE (korisnik_id, izbornik_kljuc)
);

-- -------------------------------------------------------
-- grupe_artikala
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS grupe_artikala
(
    id                   BIGSERIAL    PRIMARY KEY,
    naziv                VARCHAR(200) NOT NULL,
    opis                 VARCHAR(500),
    aktivan              BOOLEAN      NOT NULL DEFAULT TRUE,
    id_roditeljske_grupe BIGINT       REFERENCES grupe_artikala(id),
    id_kompanije         BIGINT       NOT NULL,
    sys_created_date     TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by       BIGINT,
    sys_modified_date    TIMESTAMP,
    sys_modified_by      BIGINT
);

-- -------------------------------------------------------
-- proizvodjaci
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS proizvodjaci
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(200) NOT NULL,
    drzava            VARCHAR(100),
    kontakt_osoba     VARCHAR(150),
    telefon           VARCHAR(50),
    email             VARCHAR(150),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);

-- -------------------------------------------------------
-- dobavljaci
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS dobavljaci
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(200) NOT NULL,
    adresa            VARCHAR(300),
    grad              VARCHAR(100),
    telefon           VARCHAR(50),
    email             VARCHAR(150),
    pib               VARCHAR(50),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);

-- -------------------------------------------------------
-- kupci
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS kupci
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(200) NOT NULL,
    adresa            VARCHAR(300),
    grad              VARCHAR(100),
    telefon           VARCHAR(50),
    email             VARCHAR(150),
    pib               VARCHAR(50),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);

-- -------------------------------------------------------
-- artikli_kompanije
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS artikli_kompanije
(
    id                BIGSERIAL      PRIMARY KEY,
    naziv             VARCHAR(300)   NOT NULL,
    sifra             VARCHAR(100)   NOT NULL,
    opis              VARCHAR(1000),
    jedin             VARCHAR(20),
    pdv               NUMERIC(10, 4) NOT NULL DEFAULT 17.00,
    aktivan           BOOLEAN        NOT NULL DEFAULT TRUE,
    id_grupe          BIGINT         REFERENCES grupe_artikala(id),
    id_proizvodjaca   BIGINT         REFERENCES proizvodjaci(id),
    id_dobavljaca     BIGINT         REFERENCES dobavljaci(id),
    id_kompanije      BIGINT         NOT NULL,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_artikal_sifra_kompanija UNIQUE (sifra, id_kompanije)
);

-- -------------------------------------------------------
-- artikli_poslovnice
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS artikli_poslovnice
(
    id                BIGSERIAL      PRIMARY KEY,
    id_artikla        BIGINT         NOT NULL REFERENCES artikli_kompanije(id),
    id_poslovnice     BIGINT         NOT NULL,
    vpc               NUMERIC(15, 4),
    marza             NUMERIC(10, 4),
    tip_marze         VARCHAR(30)    NOT NULL DEFAULT 'SLOBODNA',
    mpc               NUMERIC(15, 4),
    kolicina          NUMERIC(15, 4) NOT NULL DEFAULT 0,
    min_zaliha        NUMERIC(15, 4),
    optimalna_zaliha  NUMERIC(15, 4),
    aktivan           BOOLEAN        NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT         NOT NULL,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_artikal_poslovnica UNIQUE (id_artikla, id_poslovnice)
);

-- -------------------------------------------------------
-- barkodovi
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS barkodovi
(
    id                BIGSERIAL    PRIMARY KEY,
    barkod            VARCHAR(100) NOT NULL,
    id_artikla        BIGINT       NOT NULL REFERENCES artikli_kompanije(id),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_barkod_kompanija UNIQUE (barkod, id_kompanije)
);

-- -------------------------------------------------------
-- popusti
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS popusti
(
    id                BIGSERIAL      PRIMARY KEY,
    naziv             VARCHAR(200)   NOT NULL,
    procenat          NUMERIC(10, 4) NOT NULL,
    datum_od          DATE,
    datum_do          DATE,
    aktivan           BOOLEAN        NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT         NOT NULL,
    id_poslovnice     BIGINT         REFERENCES poslovnice(id),
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);

-- -------------------------------------------------------
-- ulazne_fakture
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ulazne_fakture
(
    id                       BIGSERIAL      PRIMARY KEY,
    id_kompanije             BIGINT         NOT NULL,
    id_poslovnice            BIGINT         NOT NULL,
    id_dobavljaca            BIGINT         NOT NULL,
    broj                     VARCHAR(50)    NOT NULL,
    datum                    DATE           NOT NULL,
    datum_valute             DATE,
    status                   VARCHAR(30)    NOT NULL DEFAULT 'NACRT',
    ukupno_bez_pdv           NUMERIC(15, 4) NOT NULL DEFAULT 0,
    ukupno_pdv               NUMERIC(15, 4) NOT NULL DEFAULT 0,
    ukupno                   NUMERIC(15, 4) NOT NULL DEFAULT 0,
    uneseno_ukupno_bez_pdv   NUMERIC(14, 4),
    uneseno_ukupno           NUMERIC(14, 4),
    napomena                 VARCHAR(500),
    sys_created_date         TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by           BIGINT,
    sys_modified_date        TIMESTAMP,
    sys_modified_by          BIGINT,
    CONSTRAINT uq_faktura_kompanija_poslovnica_broj UNIQUE (id_kompanije, id_poslovnice, broj)
);

-- -------------------------------------------------------
-- ulazne_fakture_stavke
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ulazne_fakture_stavke
(
    id                BIGSERIAL      PRIMARY KEY,
    id_fakture        BIGINT         NOT NULL REFERENCES ulazne_fakture(id),
    id_artikla        BIGINT         NOT NULL,
    kolicina          NUMERIC(15, 4) NOT NULL,
    vpc               NUMERIC(15, 4) NOT NULL,
    pdv_stopa         NUMERIC(10, 4) NOT NULL,
    iznos_pdv         NUMERIC(15, 4) NOT NULL DEFAULT 0,
    ukupno            NUMERIC(15, 4) NOT NULL DEFAULT 0,
    popust            NUMERIC(10, 4),
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);

-- -------------------------------------------------------
-- otpremnice
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS otpremnice
(
    id                       BIGSERIAL   PRIMARY KEY,
    id_kompanije             BIGINT      NOT NULL,
    id_poslovnice_posiljaoca BIGINT      NOT NULL,
    id_poslovnice_primaoca   BIGINT      NOT NULL,
    broj                     VARCHAR(50) NOT NULL,
    datum                    DATE        NOT NULL,
    status                   VARCHAR(30) NOT NULL DEFAULT 'KREIRANA',
    napomena                 VARCHAR(500),
    sys_created_date         TIMESTAMP   NOT NULL DEFAULT NOW(),
    sys_created_by           BIGINT,
    sys_modified_date        TIMESTAMP,
    sys_modified_by          BIGINT,
    CONSTRAINT uq_otpremnica_kompanija_broj UNIQUE (id_kompanije, broj)
);

-- -------------------------------------------------------
-- otpremnice_stavke
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS otpremnice_stavke
(
    id                BIGSERIAL      PRIMARY KEY,
    id_otpremnice     BIGINT         NOT NULL REFERENCES otpremnice(id),
    id_artikla        BIGINT         NOT NULL,
    kolicina          NUMERIC(15, 4) NOT NULL,
    vpc_posiljalac    NUMERIC(15, 4) NOT NULL,
    mpc_posiljalac    NUMERIC(15, 4) NOT NULL,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);

-- -------------------------------------------------------
-- nivelacije
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS nivelacije
(
    id                BIGSERIAL   PRIMARY KEY,
    id_kompanije      BIGINT      NOT NULL,
    id_poslovnice     BIGINT      NOT NULL,
    id_fakture        BIGINT      REFERENCES ulazne_fakture(id),
    id_otpremnice     BIGINT      REFERENCES otpremnice(id),
    broj              VARCHAR(50) NOT NULL,
    datum             DATE        NOT NULL,
    vrsta             VARCHAR(30) NOT NULL,
    napomena          VARCHAR(500),
    sys_created_date  TIMESTAMP   NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_nivelacija_kompanija_poslovnica_broj UNIQUE (id_kompanije, id_poslovnice, broj)
);

-- -------------------------------------------------------
-- nivelacije_stavke
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS nivelacije_stavke
(
    id                BIGSERIAL      PRIMARY KEY,
    id_nivelacije     BIGINT         NOT NULL REFERENCES nivelacije(id),
    id_artikla        BIGINT         NOT NULL,
    kolicina          NUMERIC(15, 4) NOT NULL,
    vpc               NUMERIC(15, 4) NOT NULL,
    mpc_stara         NUMERIC(15, 4) NOT NULL,
    mpc_nova          NUMERIC(15, 4) NOT NULL,
    iznos_nivelacije  NUMERIC(15, 4) NOT NULL,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);
