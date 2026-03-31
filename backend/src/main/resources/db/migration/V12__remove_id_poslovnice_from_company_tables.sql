-- Ukloni id_poslovnice iz svih company-level tabela (sifarnici i korisnik_izbornici).
-- korisnici tabela ZADRZAVA id_poslovnice (to je poslovni podatak korisnika, ne scope).
-- artikli_poslovnice tabela ZADRZAVA id_poslovnice (nasljedjena iz PoslovnicaBaseEntity).

ALTER TABLE grupe_artikala       DROP COLUMN IF EXISTS id_poslovnice;
ALTER TABLE proizvodjaci         DROP COLUMN IF EXISTS id_poslovnice;
ALTER TABLE dobavljaci           DROP COLUMN IF EXISTS id_poslovnice;
ALTER TABLE kupci                DROP COLUMN IF EXISTS id_poslovnice;
ALTER TABLE artikli_kompanije    DROP COLUMN IF EXISTS id_poslovnice;
ALTER TABLE barkodovi            DROP COLUMN IF EXISTS id_poslovnice;
ALTER TABLE popusti              DROP COLUMN IF EXISTS id_poslovnice;
ALTER TABLE korisnik_izbornici   DROP COLUMN IF EXISTS id_poslovnice;
