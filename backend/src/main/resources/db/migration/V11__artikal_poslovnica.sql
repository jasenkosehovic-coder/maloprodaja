-- V11__artikal_poslovnica.sql
-- Premještanje cijena (vpc, marza, tip_marze, mpc) sa artikli_kompanije na artikli_poslovnice

-- -------------------------------------------------------
-- 1. Ukloni kolone cijena iz artikli_kompanije
-- -------------------------------------------------------
ALTER TABLE artikli_kompanije
    DROP COLUMN IF EXISTS vpc,
    DROP COLUMN IF EXISTS marza,
    DROP COLUMN IF EXISTS tip_marze,
    DROP COLUMN IF EXISTS mpc;

-- -------------------------------------------------------
-- 2. Kreiraj tabelu artikli_poslovnice
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS artikli_poslovnice
(
    id                  BIGSERIAL      PRIMARY KEY,
    id_artikla          BIGINT         NOT NULL REFERENCES artikli_kompanije(id),
    id_poslovnice       BIGINT         NOT NULL,
    vpc                 NUMERIC(15, 4),
    marza               NUMERIC(10, 4),
    tip_marze           VARCHAR(30)    NOT NULL DEFAULT 'SLOBODNA',
    mpc                 NUMERIC(15, 4),
    kolicina            NUMERIC(15, 4) NOT NULL DEFAULT 0,
    min_zaliha          NUMERIC(15, 4),
    optimalna_zaliha    NUMERIC(15, 4),
    aktivan             BOOLEAN        NOT NULL DEFAULT TRUE,
    id_kompanije        BIGINT         NOT NULL,
    sys_created_date    TIMESTAMP      NOT NULL DEFAULT NOW(),
    sys_created_by      BIGINT,
    sys_modified_date   TIMESTAMP,
    sys_modified_by     BIGINT,
    CONSTRAINT uq_artikal_poslovnica UNIQUE (id_artikla, id_poslovnice)
);
