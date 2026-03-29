-- V6__fix_uloga_check_constraint.sql
-- Drop the Hibernate-generated uloga check constraint and recreate it
-- with all current KorisnikUloga enum values (including SUPER_ADMIN).

ALTER TABLE korisnici DROP CONSTRAINT IF EXISTS korisnici_uloga_check;

ALTER TABLE korisnici
    ADD CONSTRAINT korisnici_uloga_check
        CHECK (uloga IN ('SUPER_ADMIN', 'ADMIN', 'MENADZER', 'BLAGAJNIK', 'SKLADISTAR'));
