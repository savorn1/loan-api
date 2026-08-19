-- Seed sample interest_schemes + interest_scheme_details rows (a scheme is a
-- named rate table — each detail row is a term/amount tier with its own rate).
-- Run against loan_product_db, e.g.:
--   psql "postgresql://postgres:@localhost:5432/loan_product_db" -f scripts/seed-interest-schemes.sql
--
-- Idempotent for repeat full runs (ON CONFLICT on the scheme code) — same caveat
-- as seed-fee-schemes.sql: a pre-existing scheme code from elsewhere won't get
-- its tiers (re)inserted, since the parent INSERT's RETURNING id is then empty.

WITH scheme_flat AS (
  INSERT INTO interest_schemes (id, code, name, interest_type, calculation_method, status, created_at, updated_at)
  VALUES (gen_random_uuid(), 'STD-FLAT', 'Standard Flat Rate', 'FLAT', 'ACTUAL_365', 'ACTIVE', now(), now())
  ON CONFLICT (code) DO NOTHING
  RETURNING id
)
INSERT INTO interest_scheme_details (id, interest_scheme_id, min_term, max_term, min_amount, max_amount, interest_rate, created_at, updated_at)
SELECT gen_random_uuid(), id, 1, 12, 500.00, 5000.00, 12.00, now(), now() FROM scheme_flat
UNION ALL
SELECT gen_random_uuid(), id, 13, 36, 500.00, 5000.00, 15.00, now(), now() FROM scheme_flat
UNION ALL
SELECT gen_random_uuid(), id, 1, 36, 5000.01, 50000.00, 10.00, now(), now() FROM scheme_flat;

WITH scheme_reducing AS (
  INSERT INTO interest_schemes (id, code, name, interest_type, calculation_method, status, created_at, updated_at)
  VALUES (gen_random_uuid(), 'STD-REDUCING', 'Standard Reducing Balance', 'REDUCING', 'ACTUAL_360', 'ACTIVE', now(), now())
  ON CONFLICT (code) DO NOTHING
  RETURNING id
)
INSERT INTO interest_scheme_details (id, interest_scheme_id, min_term, max_term, min_amount, max_amount, interest_rate, created_at, updated_at)
SELECT gen_random_uuid(), id, 1, 12, 500.00, 10000.00, 18.00, now(), now() FROM scheme_reducing
UNION ALL
SELECT gen_random_uuid(), id, 13, 60, 500.00, 10000.00, 20.00, now(), now() FROM scheme_reducing
UNION ALL
SELECT gen_random_uuid(), id, 1, 60, 10000.01, 100000.00, 14.00, now(), now() FROM scheme_reducing;

WITH scheme_mortgage AS (
  INSERT INTO interest_schemes (id, code, name, interest_type, calculation_method, status, created_at, updated_at)
  VALUES (gen_random_uuid(), 'MORTGAGE-REDUCING', 'Mortgage Reducing Balance', 'REDUCING', 'THIRTY_360', 'ACTIVE', now(), now())
  ON CONFLICT (code) DO NOTHING
  RETURNING id
)
INSERT INTO interest_scheme_details (id, interest_scheme_id, min_term, max_term, min_amount, max_amount, interest_rate, created_at, updated_at)
SELECT gen_random_uuid(), id, 60, 360, 50000.00, 500000.00, 6.50, now(), now() FROM scheme_mortgage;
