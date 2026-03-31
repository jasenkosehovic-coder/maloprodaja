-- V13__poslovnica_pib.sql
-- Add PIB field to poslovnice

ALTER TABLE poslovnice
    ADD COLUMN pib VARCHAR(50);
