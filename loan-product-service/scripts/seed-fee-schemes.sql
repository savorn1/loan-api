-- Seed sample fee_schemes + fee_scheme_details rows (a scheme is a named bundle
-- of fee line items — see FeeSchemeDetail for type/calculationMethod/chargeTiming).
-- Run against loan_product_db, e.g.:
--   psql "postgresql://postgres:@localhost:5432/loan_product_db" -f scripts/seed-fee-schemes.sql
--
-- Idempotent for repeat full runs (ON CONFLICT on the scheme code), but if a
-- scheme code already exists from something other than this script, its
-- details won't be (re)inserted — the RETURNING id from a no-op parent insert
-- is empty, so the detail INSERT ... SELECT below naturally inserts nothing.

WITH scheme_std AS (
  INSERT INTO fee_schemes (id, code, name, status, created_at, updated_at)
  VALUES (gen_random_uuid(), 'STD-FEES', 'Standard Fee Bundle', 'ACTIVE', now(), now())
  ON CONFLICT (code) DO NOTHING
  RETURNING id
)
INSERT INTO fee_scheme_details (id, fee_scheme_id, type, calculation_method, amount, charge_timing, created_at, updated_at)
SELECT gen_random_uuid(), id, 'ORIGINATION', 'PERCENTAGE', 1.00, 'UPFRONT', now(), now() FROM scheme_std
UNION ALL
SELECT gen_random_uuid(), id, 'PROCESSING', 'FLAT', 25.00, 'ON_DISBURSEMENT', now(), now() FROM scheme_std
UNION ALL
SELECT gen_random_uuid(), id, 'LATE_PAYMENT', 'FLAT', 10.00, 'RECURRING', now(), now() FROM scheme_std;

WITH scheme_premium AS (
  INSERT INTO fee_schemes (id, code, name, status, created_at, updated_at)
  VALUES (gen_random_uuid(), 'PREMIUM-FEES', 'Premium Low-Fee Bundle', 'ACTIVE', now(), now())
  ON CONFLICT (code) DO NOTHING
  RETURNING id
)
INSERT INTO fee_scheme_details (id, fee_scheme_id, type, calculation_method, amount, charge_timing, created_at, updated_at)
SELECT gen_random_uuid(), id, 'ORIGINATION', 'PERCENTAGE', 0.50, 'UPFRONT', now(), now() FROM scheme_premium
UNION ALL
SELECT gen_random_uuid(), id, 'LATE_PAYMENT', 'FLAT', 5.00, 'RECURRING', now(), now() FROM scheme_premium;

WITH scheme_mortgage AS (
  INSERT INTO fee_schemes (id, code, name, status, created_at, updated_at)
  VALUES (gen_random_uuid(), 'MORTGAGE-FEES', 'Mortgage Fee Bundle', 'ACTIVE', now(), now())
  ON CONFLICT (code) DO NOTHING
  RETURNING id
)
INSERT INTO fee_scheme_details (id, fee_scheme_id, type, calculation_method, amount, charge_timing, created_at, updated_at)
SELECT gen_random_uuid(), id, 'ORIGINATION', 'PERCENTAGE', 1.50, 'UPFRONT', now(), now() FROM scheme_mortgage
UNION ALL
SELECT gen_random_uuid(), id, 'PROCESSING', 'FLAT', 200.00, 'ON_DISBURSEMENT', now(), now() FROM scheme_mortgage
UNION ALL
SELECT gen_random_uuid(), id, 'PREPAYMENT', 'PERCENTAGE', 2.00, 'ON_DISBURSEMENT', now(), now() FROM scheme_mortgage
UNION ALL
SELECT gen_random_uuid(), id, 'LATE_PAYMENT', 'FLAT', 50.00, 'RECURRING', now(), now() FROM scheme_mortgage;
