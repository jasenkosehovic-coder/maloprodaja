-- V1__init_schema.sql
-- Consolidated initial schema

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
-- tipovi_velicina
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS tipovi_velicina
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(100) NOT NULL,
    opis              VARCHAR(300),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_tip_velicina_naziv_kompanija UNIQUE (naziv, id_kompanije)
);

-- -------------------------------------------------------
-- velicine
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS velicine
(
    id                BIGSERIAL   PRIMARY KEY,
    id_tipa_velicina  BIGINT      NOT NULL REFERENCES tipovi_velicina(id),
    oznaka            VARCHAR(20) NOT NULL,
    redosljed         INT         NOT NULL DEFAULT 0,
    aktivan           BOOLEAN     NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT      NOT NULL,
    sys_created_date  TIMESTAMP   NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_velicina_tip_oznaka UNIQUE (id_tipa_velicina, oznaka)
);

CREATE INDEX IF NOT EXISTS idx_velicine_tip       ON velicine(id_tipa_velicina);
CREATE INDEX IF NOT EXISTS idx_velicine_kompanija  ON velicine(id_kompanije);

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
    id_tipa_velicina  BIGINT         REFERENCES tipovi_velicina(id),
    id_kompanije      BIGINT         NOT NULL,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_artikal_sifra_kompanija UNIQUE (sifra, id_kompanije)
);

-- -------------------------------------------------------
-- varijante_artikla
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS varijante_artikla
(
    id                BIGSERIAL PRIMARY KEY,
    id_artikla        BIGINT    NOT NULL REFERENCES artikli_kompanije(id),
    id_velicine       BIGINT    REFERENCES velicine(id),
    aktivan           BOOLEAN   NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT    NOT NULL,
    sys_created_date  TIMESTAMP NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_varijanta_artikal_velicina UNIQUE (id_artikla, id_velicine)
);

CREATE INDEX IF NOT EXISTS idx_varijante_artikla   ON varijante_artikla(id_artikla);
CREATE INDEX IF NOT EXISTS idx_varijante_kompanija  ON varijante_artikla(id_kompanije);

-- -------------------------------------------------------
-- artikli_poslovnice  (samo cijena, zaliha je u varijante_artikla_poslovnica)
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
    aktivan           BOOLEAN        NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT         NOT NULL,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_artikal_poslovnica UNIQUE (id_artikla, id_poslovnice)
);

-- -------------------------------------------------------
-- barkodovi  (vezani za varijantu, ne direktno za artikal)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS barkodovi
(
    id                BIGSERIAL    PRIMARY KEY,
    barkod            VARCHAR(100) NOT NULL,
    id_varijante      BIGINT       NOT NULL REFERENCES varijante_artikla(id),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_barkod_kompanija UNIQUE (barkod, id_kompanije)
);

CREATE INDEX IF NOT EXISTS idx_barkodovi_varijanta  ON barkodovi(id_varijante);
CREATE INDEX IF NOT EXISTS idx_barkodovi_kompanija   ON barkodovi(id_kompanije);

-- -------------------------------------------------------
-- varijante_artikla_poslovnica  (zaliha po varijanti po poslovnici)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS varijante_artikla_poslovnica
(
    id                BIGSERIAL      PRIMARY KEY,
    id_varijante      BIGINT         NOT NULL REFERENCES varijante_artikla(id),
    id_poslovnice     BIGINT         NOT NULL,
    kolicina          NUMERIC(15, 4) NOT NULL DEFAULT 0,
    min_zaliha        NUMERIC(15, 4),
    optimalna_zaliha  NUMERIC(15, 4),
    id_kompanije      BIGINT         NOT NULL,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_varijanta_poslovnica UNIQUE (id_varijante, id_poslovnice)
);

CREATE INDEX IF NOT EXISTS idx_var_posl_varijanta   ON varijante_artikla_poslovnica(id_varijante);
CREATE INDEX IF NOT EXISTS idx_var_posl_poslovnica  ON varijante_artikla_poslovnica(id_poslovnice);
CREATE INDEX IF NOT EXISTS idx_var_posl_kompanija   ON varijante_artikla_poslovnica(id_kompanije);

-- -------------------------------------------------------
-- definicije_atributa  (šta kompanija želi pratiti: Boja, Sezona, Fit...)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS definicije_atributa
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(100) NOT NULL,
    redosljed         INT          NOT NULL DEFAULT 0,
    obavezno          BOOLEAN      NOT NULL DEFAULT FALSE,
    za_web            BOOLEAN      NOT NULL DEFAULT FALSE,
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_definicija_atributa_naziv_kompanija UNIQUE (naziv, id_kompanije)
);

-- -------------------------------------------------------
-- vrijednosti_atributa  (predefinisane opcije: Crvena, Slim Fit, Proljeće 2024...)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS vrijednosti_atributa
(
    id                BIGSERIAL    PRIMARY KEY,
    id_definicije     BIGINT       NOT NULL REFERENCES definicije_atributa(id),
    vrijednost        VARCHAR(200) NOT NULL,
    redosljed         INT          NOT NULL DEFAULT 0,
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_vrijednost_atributa UNIQUE (id_definicije, vrijednost)
);

CREATE INDEX IF NOT EXISTS idx_vrijednosti_atributa_definicija ON vrijednosti_atributa(id_definicije);

-- -------------------------------------------------------
-- artikal_atributi  (koje vrijednosti ima konkretni artikal)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS artikal_atributi
(
    id                BIGSERIAL PRIMARY KEY,
    id_artikla        BIGINT    NOT NULL REFERENCES artikli_kompanije(id),
    id_definicije     BIGINT    NOT NULL REFERENCES definicije_atributa(id),
    id_vrijednosti    BIGINT    NOT NULL REFERENCES vrijednosti_atributa(id),
    id_kompanije      BIGINT    NOT NULL,
    sys_created_date  TIMESTAMP NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_artikal_atribut UNIQUE (id_artikla, id_definicije)
);

CREATE INDEX IF NOT EXISTS idx_artikal_atributi_artikal    ON artikal_atributi(id_artikla);
CREATE INDEX IF NOT EXISTS idx_artikal_atributi_kompanija  ON artikal_atributi(id_kompanije);
CREATE INDEX IF NOT EXISTS idx_artikal_atributi_filter     ON artikal_atributi(id_kompanije, id_definicije, id_vrijednosti);

-- -------------------------------------------------------
-- slike_artikala
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS slike_artikala
(
    id                BIGSERIAL    PRIMARY KEY,
    id_artikla        BIGINT       NOT NULL REFERENCES artikli_kompanije(id),
    putanja           VARCHAR(500) NOT NULL,
    redosljed         INT          NOT NULL DEFAULT 0,
    je_naslovna       BOOLEAN      NOT NULL DEFAULT FALSE,
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);

CREATE INDEX IF NOT EXISTS idx_slike_artikala_artikal ON slike_artikala(id_artikla);

-- -------------------------------------------------------
-- web_artikli  (artikli objavljeni na web shopu sa web opisima i cijenama)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS web_artikli
(
    id                BIGSERIAL      PRIMARY KEY,
    id_artikla        BIGINT         NOT NULL REFERENCES artikli_kompanije(id),
    web_naziv         VARCHAR(300),
    web_opis          TEXT,
    aktivan           BOOLEAN        NOT NULL DEFAULT TRUE,
    mpc               NUMERIC(15, 4),
    popust            NUMERIC(10, 4),
    nova_mpc          NUMERIC(15, 4),
    meta_title        VARCHAR(160),
    meta_opis         VARCHAR(320),
    id_kompanije      BIGINT         NOT NULL,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_web_artikal_kompanija UNIQUE (id_artikla, id_kompanije)
);

CREATE INDEX IF NOT EXISTS idx_web_artikli_artikal   ON web_artikli(id_artikla);
CREATE INDEX IF NOT EXISTS idx_web_artikli_kompanija  ON web_artikli(id_kompanije, aktivan);

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
    godina                   INTEGER        NOT NULL,
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

CREATE INDEX IF NOT EXISTS idx_ulazne_fakture_godina ON ulazne_fakture(id_kompanije, id_poslovnice, godina);

-- -------------------------------------------------------
-- ulazne_fakture_stavke
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ulazne_fakture_stavke
(
    id                BIGSERIAL      PRIMARY KEY,
    id_fakture        BIGINT         NOT NULL REFERENCES ulazne_fakture(id),
    id_artikla        BIGINT         NOT NULL,
    id_varijante      BIGINT         REFERENCES varijante_artikla(id),
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
    godina                   INTEGER     NOT NULL,
    status                   VARCHAR(30) NOT NULL DEFAULT 'KREIRANA',
    napomena                 VARCHAR(500),
    sys_created_date         TIMESTAMP   NOT NULL DEFAULT NOW(),
    sys_created_by           BIGINT,
    sys_modified_date        TIMESTAMP,
    sys_modified_by          BIGINT,
    CONSTRAINT uq_otpremnica_kompanija_broj UNIQUE (id_kompanije, broj)
);

CREATE INDEX IF NOT EXISTS idx_otpremnice_godina ON otpremnice(id_kompanije, godina);

-- -------------------------------------------------------
-- otpremnice_stavke
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS otpremnice_stavke
(
    id                BIGSERIAL      PRIMARY KEY,
    id_otpremnice     BIGINT         NOT NULL REFERENCES otpremnice(id),
    id_artikla        BIGINT         NOT NULL,
    id_varijante      BIGINT         REFERENCES varijante_artikla(id),
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
    id_varijante      BIGINT         REFERENCES varijante_artikla(id),
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
