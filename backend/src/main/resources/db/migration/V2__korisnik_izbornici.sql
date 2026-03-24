-- V2__korisnik_izbornici.sql
-- Per-user menu override table

CREATE TABLE IF NOT EXISTS korisnik_izbornici
(
    id                BIGSERIAL    PRIMARY KEY,
    korisnik_id       BIGINT       NOT NULL REFERENCES korisnici(id),
    izbornik_kljuc    VARCHAR(50)  NOT NULL,
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL,
    id_poslovnice     BIGINT,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT,
    CONSTRAINT uq_korisnik_izbornik UNIQUE (korisnik_id, izbornik_kljuc)
);
