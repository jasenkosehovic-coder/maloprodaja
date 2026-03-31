-- V14__popust_id_poslovnice.sql
-- Popust može biti za kompaniju (NULL) ili za konkretnu poslovnicu

ALTER TABLE popusti
    ADD COLUMN id_poslovnice BIGINT REFERENCES poslovnice(id);
