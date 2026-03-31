-- V10__sifarnici.sql
-- Šifrarnici: grupe artikala, proizvođači, dobavljači, kupci, artikli kompanije, barkodovi, popusti

-- -------------------------------------------------------
-- grupe_artikala
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS grupe_artikala
(
    id                  BIGSERIAL    PRIMARY KEY,
    naziv               VARCHAR(200) NOT NULL,
    opis                VARCHAR(500),
    aktivan             BOOLEAN      NOT NULL DEFAULT TRUE,
    id_roditeljske_grupe BIGINT      REFERENCES grupe_artikala(id),
    id_kompanije        BIGINT       NOT NULL,
    id_poslovnice       BIGINT,
    sys_created_date    TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by      BIGINT,
    sys_modified_date   TIMESTAMP,
    sys_modified_by     BIGINT
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
    id_poslovnice     BIGINT,
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
    id_poslovnice     BIGINT,
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
    id_poslovnice     BIGINT,
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
    vpc               NUMERIC(15, 4),
    marza             NUMERIC(10, 4),
    tip_marze         VARCHAR(30)    NOT NULL DEFAULT 'SLOBODNA',
    mpc               NUMERIC(15, 4),
    aktivan           BOOLEAN        NOT NULL DEFAULT TRUE,
    id_grupe          BIGINT         REFERENCES grupe_artikala(id),
    id_proizvodjaca   BIGINT         REFERENCES proizvodjaci(id),
    id_dobavljaca     BIGINT         REFERENCES dobavljaci(id),
    id_kompanije      BIGINT         NOT NULL,
    id_poslovnice     BIGINT,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_artikal_sifra_kompanija UNIQUE (sifra, id_kompanije)
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
    id_poslovnice     BIGINT,
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
    id_poslovnice     BIGINT,
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);
