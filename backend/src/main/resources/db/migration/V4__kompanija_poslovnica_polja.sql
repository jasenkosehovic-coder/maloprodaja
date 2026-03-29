-- V4__kompanija_poslovnica_polja.sql
-- Add contact/media fields to kompanije and poslovnice

ALTER TABLE kompanije
    ADD COLUMN grad     VARCHAR(100),
    ADD COLUMN telefon  VARCHAR(30),
    ADD COLUMN email    VARCHAR(150),
    ADD COLUMN web      VARCHAR(200),
    ADD COLUMN logo     BYTEA;

ALTER TABLE poslovnice
    ADD COLUMN grad     VARCHAR(100),
    ADD COLUMN telefon  VARCHAR(30),
    ADD COLUMN email    VARCHAR(150);
