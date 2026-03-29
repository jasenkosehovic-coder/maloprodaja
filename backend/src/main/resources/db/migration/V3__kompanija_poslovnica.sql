-- V3__kompanija_poslovnica.sql
-- Tenant root: kompanije and poslovnice tables

CREATE TABLE IF NOT EXISTS kompanije
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(200) NOT NULL,
    pib               VARCHAR(50)  UNIQUE,
    adresa            VARCHAR(300),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_modified_date TIMESTAMP,
    sys_created_by    BIGINT,
    sys_modified_by   BIGINT
);

CREATE TABLE IF NOT EXISTS poslovnice
(
    id                BIGSERIAL    PRIMARY KEY,
    naziv             VARCHAR(200) NOT NULL,
    adresa            VARCHAR(300),
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    id_kompanije      BIGINT       NOT NULL REFERENCES kompanije(id),
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_modified_date TIMESTAMP,
    sys_created_by    BIGINT,
    sys_modified_by   BIGINT
);

-- Insert seed companies before adding FK constraints (V1 already inserted admin with id_kompanije=1)
INSERT INTO kompanije (id, naziv, pib, aktivan, sys_created_date)
VALUES
    (1, 'Trgovina d.o.o.',   '1234567890', TRUE, NOW()),
    (2, 'Prodavnica d.o.o.', '0987654321', TRUE, NOW());

-- Advance the sequence past the manually-assigned IDs
SELECT setval('kompanije_id_seq', 2, true);

-- Insert seed branches
INSERT INTO poslovnice (id, naziv, adresa, aktivan, id_kompanije, sys_created_date)
VALUES
    (1, 'Centrala',      'Sarajevo, Titova 1',    TRUE, 1, NOW()),
    (2, 'Poslovnica 2',  'Sarajevo, Ferhadija 10',TRUE, 1, NOW()),
    (3, 'Centrala',      'Mostar, Bulevar 5',     TRUE, 2, NOW()),
    (4, 'Poslovnica 2',  'Mostar, Rondo 3',       TRUE, 2, NOW());

-- Advance the sequence past the manually-assigned IDs
SELECT setval('poslovnice_id_seq', 4, true);

-- Link the existing V1 admin to poslovnica 1 (id_kompanije=1 was already set)
UPDATE korisnici
SET id_poslovnice = 1
WHERE username = 'admin'
  AND id_poslovnice IS NULL;

-- Add FK constraints now that all referenced rows exist
ALTER TABLE korisnici
    ADD CONSTRAINT fk_korisnici_kompanija
        FOREIGN KEY (id_kompanije) REFERENCES kompanije(id);

ALTER TABLE korisnici
    ADD CONSTRAINT fk_korisnici_poslovnica
        FOREIGN KEY (id_poslovnice) REFERENCES poslovnice(id);
