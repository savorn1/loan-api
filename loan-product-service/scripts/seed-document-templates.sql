-- Seed sample document_templates rows (the reusable document-type catalog —
-- see DocumentTemplateSeeder for the same data via the idempotent app-startup seed).
-- Run against loan_product_db, e.g.:
--   psql "postgresql://postgres:@localhost:5432/loan_product_db" -f scripts/seed-document-templates.sql

INSERT INTO document_templates (
  id, code, name, description, status, sample_file_name, sample_file_url, created_at, updated_at
) VALUES
  (gen_random_uuid(), 'ID_CARD', 'National ID Card',
   'A valid, unexpired government-issued national ID card.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'PASSPORT', 'Passport',
   'Valid passport, required when a national ID isn''t available.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'PROOF_OF_ADDRESS', 'Proof of Address',
   'A recent utility bill, lease agreement, or similar document confirming current residence.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'PAYSLIP', 'Payslip',
   'Most recent payslip(s) evidencing regular employment income.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'BANK_STATEMENT', 'Bank Statement',
   'Bank statements covering the last 3-6 months, used to verify cash flow.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'BUSINESS_LICENSE', 'Business License',
   'Valid business registration or operating license, for self-employed or business-loan applicants.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'TAX_RETURN', 'Tax Return',
   'Most recent annual tax filing, for income verification on larger loans.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'COLLATERAL_TITLE_DEED', 'Collateral Title Deed',
   'Ownership title/deed for any asset pledged as collateral.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'GUARANTOR_ID', 'Guarantor ID Card',
   'Valid government-issued ID for each guarantor on the loan.',
   'ACTIVE', NULL, NULL, now(), now()),

  (gen_random_uuid(), 'LOAN_APPLICATION_FORM', 'Signed Loan Application Form',
   'The completed and signed loan application form.',
   'ACTIVE', NULL, NULL, now(), now())
ON CONFLICT (code) DO NOTHING;
