-- Seed sample term_templates rows (the reusable loan-duration catalog — termValue
-- is interpreted using whichever termUnit the assigned loan product uses).
-- Run against loan_product_db, e.g.:
--   psql "postgresql://postgres:@localhost:5432/loan_product_db" -f scripts/seed-term-templates.sql

INSERT INTO term_templates (
  id, code, name, term_value, status, created_at, updated_at
) VALUES
  (gen_random_uuid(), 'TERM_30D', '30 Days', 30, 'ACTIVE', now(), now()),
  (gen_random_uuid(), 'TERM_90D', '90 Days', 90, 'ACTIVE', now(), now()),
  (gen_random_uuid(), 'TERM_6M', '6 Months', 6, 'ACTIVE', now(), now()),
  (gen_random_uuid(), 'TERM_12M', '12 Months', 12, 'ACTIVE', now(), now()),
  (gen_random_uuid(), 'TERM_24M', '24 Months', 24, 'ACTIVE', now(), now()),
  (gen_random_uuid(), 'TERM_36M', '36 Months', 36, 'ACTIVE', now(), now()),
  (gen_random_uuid(), 'TERM_60M', '60 Months', 60, 'ACTIVE', now(), now()),
  (gen_random_uuid(), 'TERM_5Y', '5 Years', 5, 'ACTIVE', now(), now())
ON CONFLICT (code) DO NOTHING;
