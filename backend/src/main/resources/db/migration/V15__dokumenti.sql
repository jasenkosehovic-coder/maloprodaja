-- V15__dokumenti.sql
-- Dokumenti: ulazne fakture, nivelacije, otpremnice

-- -------------------------------------------------------
-- ulazne_fakture
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ulazne_fakture
(
    id                BIGSERIAL      PRIMARY KEY,
    id_kompanije      BIGINT         NOT NULL,
    id_poslovnice     BIGINT         NOT NULL,
    id_dobavljaca     BIGINT         NOT NULL,
    broj              VARCHAR(50)    NOT NULL,
    datum             DATE           NOT NULL,
    datum_valute      DATE,
    status            VARCHAR(30)    NOT NULL DEFAULT 'NACRT',
    ukupno_bez_pdv    NUMERIC(15, 4) NOT NULL DEFAULT 0,
    ukupno_pdv        NUMERIC(15, 4) NOT NULL DEFAULT 0,
    ukupno            NUMERIC(15, 4) NOT NULL DEFAULT 0,
    napomena          VARCHAR(500),
    sys_created_date  TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
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
    pdv_stopa         NUMERIC(5, 4)  NOT NULL,
    iznos_pdv         NUMERIC(15, 4) NOT NULL DEFAULT 0,
    ukupno            NUMERIC(15, 4) NOT NULL DEFAULT 0,
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
    id                        BIGSERIAL   PRIMARY KEY,
    id_kompanije              BIGINT      NOT NULL,
    id_poslovnice_posiljaoca  BIGINT      NOT NULL,
    id_poslovnice_primaoca    BIGINT      NOT NULL,
    broj                      VARCHAR(50) NOT NULL,
    datum                     DATE        NOT NULL,
    status                    VARCHAR(30) NOT NULL DEFAULT 'KREIRANA',
    napomena                  VARCHAR(500),
    sys_created_date          TIMESTAMP   NOT NULL DEFAULT NOW(),
    sys_created_by            BIGINT,
    sys_modified_date         TIMESTAMP,
    sys_modified_by           BIGINT,
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
