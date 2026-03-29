-- V7__fix_uloga_all_constraints.sql
-- Drop ALL check constraints on korisnici.uloga (regardless of name),
-- then add back one correct constraint with all KorisnikUloga values.

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_attribute att
             ON att.attrelid = rel.oid
            AND att.attnum   = ANY(con.conkey)
        WHERE rel.relname  = 'korisnici'
          AND con.contype  = 'c'
          AND att.attname  = 'uloga'
    ) LOOP
        EXECUTE 'ALTER TABLE korisnici DROP CONSTRAINT ' || quote_ident(r.conname);
    END LOOP;
END;
$$;

ALTER TABLE korisnici
    ADD CONSTRAINT korisnici_uloga_check
        CHECK (uloga IN ('SUPER_ADMIN', 'ADMIN', 'MENADZER', 'BLAGAJNIK', 'SKLADISTAR'));
