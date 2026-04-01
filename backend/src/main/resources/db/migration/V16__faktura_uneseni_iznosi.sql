ALTER TABLE ulazne_fakture
  ADD COLUMN IF NOT EXISTS uneseno_ukupno_bez_pdv NUMERIC(14,4),
  ADD COLUMN IF NOT EXISTS uneseno_ukupno          NUMERIC(14,4);
