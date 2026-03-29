-- V9__kompanija_logo.sql
-- Add logo column to kompanije if it does not already exist

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'kompanije' AND column_name = 'logo'
    ) THEN
        ALTER TABLE kompanije ADD COLUMN logo BYTEA;
    END IF;
END
$$;
