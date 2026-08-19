-- Seed example loan_products rows, one per LoanType.
-- Run against loan_product_db, e.g.:
--   psql "postgresql://postgres:@localhost:5432/loan_product_db" -f scripts/seed-loan-products.sql

INSERT INTO loan_products (
  id, code, version, name, description, loan_type, currency,
  min_amount, max_amount, min_term, max_term, status,
  effective_from, effective_to, created_by, updated_by, created_at, updated_at
) VALUES
  (gen_random_uuid(), 'PL-STD-01', 1, 'Standard Personal Loan',
   'Unsecured loan for general purposes such as medical bills, travel, or debt consolidation.',
   'PERSONAL', 'USD', 500.00, 10000.00, 6, 36, 'PUBLISHED',
   '2026-01-01', NULL, 'seed-script', NULL, now(), now()),

  (gen_random_uuid(), 'HL-30Y-01', 1, 'Home Purchase Mortgage',
   'Secured loan to purchase or build a residential property, collateralized by the property itself.',
   'HOME', 'USD', 50000.00, 500000.00, 60, 360, 'PUBLISHED',
   '2026-01-01', NULL, 'seed-script', NULL, now(), now()),

  (gen_random_uuid(), 'AL-NEW-01', 1, 'New Vehicle Auto Loan',
   'Loan to purchase a new car or motorcycle, secured by the vehicle.',
   'AUTO', 'USD', 3000.00, 40000.00, 12, 72, 'PUBLISHED',
   '2026-01-01', NULL, 'seed-script', NULL, now(), now()),

  (gen_random_uuid(), 'BL-SME-01', 1, 'Small Business Working Capital Loan',
   'Loan to start, expand, or operate a small business.',
   'BUSINESS', 'USD', 5000.00, 200000.00, 12, 84, 'DRAFT',
   '2026-09-01', NULL, 'seed-script', NULL, now(), now()),

  (gen_random_uuid(), 'EDU-TUI-01', 1, 'Tuition Support Loan',
   'Loan to cover tuition and education-related expenses.',
   'EDUCATION', 'USD', 1000.00, 30000.00, 12, 120, 'PUBLISHED',
   '2026-01-01', NULL, 'seed-script', NULL, now(), now()),

  (gen_random_uuid(), 'OTH-GEN-01', 1, 'General Purpose Loan',
   'Catch-all loan type for purposes not covered by the standard categories.',
   'OTHER', 'KHR', 2000000.00, 40000000.00, 3, 24, 'INACTIVE',
   '2025-06-01', '2026-06-01', 'seed-script', NULL, now(), now())
ON CONFLICT (code, version) DO NOTHING;
