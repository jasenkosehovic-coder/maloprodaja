-- Add godina column to ulazne_fakture (extracted from datum)
ALTER TABLE ulazne_fakture ADD COLUMN IF NOT EXISTS godina INTEGER;
UPDATE ulazne_fakture SET godina = EXTRACT(YEAR FROM datum) WHERE godina IS NULL;
ALTER TABLE ulazne_fakture ALTER COLUMN godina SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ulazne_fakture_godina ON ulazne_fakture(id_kompanije, id_poslovnice, godina);

-- Add godina column to otpremnice (extracted from datum)
ALTER TABLE otpremnice ADD COLUMN IF NOT EXISTS godina INTEGER;
UPDATE otpremnice SET godina = EXTRACT(YEAR FROM datum) WHERE godina IS NULL;
ALTER TABLE otpremnice ALTER COLUMN godina SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_otpremnice_godina ON otpremnice(id_kompanije, godina);
