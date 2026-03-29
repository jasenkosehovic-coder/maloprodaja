-- V5__korisnik_aktivan_do.sql
-- User licence expiry date (null = no expiry check)

ALTER TABLE korisnici
    ADD COLUMN aktivan_do DATE;
