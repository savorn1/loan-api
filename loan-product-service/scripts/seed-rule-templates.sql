-- Seed sample rule_templates rows (the reusable eligibility-rule catalog —
-- see RuleTemplateSeeder for the same data via the idempotent app-startup seed).
-- Run against loan_product_db, e.g.:
--   psql "postgresql://postgres:@localhost:5432/loan_product_db" -f scripts/seed-rule-templates.sql

INSERT INTO rule_templates (
  id, code, name, field, operator, value, value2, description, status, created_at, updated_at
) VALUES
  (gen_random_uuid(), 'MIN_CREDIT_SCORE', 'Minimum Credit Score',
   'CREDIT_SCORE', 'GREATER_THAN_OR_EQUAL', '650', NULL,
   'Applicant''s credit score must be at least 650.',
   'ACTIVE', now(), now()),

  (gen_random_uuid(), 'MIN_CREDIT_SCORE_PREMIUM', 'Minimum Credit Score (Premium)',
   'CREDIT_SCORE', 'GREATER_THAN_OR_EQUAL', '750', NULL,
   'Higher credit score bar for premium/lower-rate products.',
   'ACTIVE', now(), now()),

  (gen_random_uuid(), 'MIN_MONTHLY_INCOME', 'Minimum Monthly Income',
   'MONTHLY_INCOME', 'GREATER_THAN_OR_EQUAL', '500', NULL,
   'Applicant''s verified monthly income must be at least 500.',
   'ACTIVE', now(), now()),

  (gen_random_uuid(), 'APPLICANT_AGE_RANGE', 'Applicant Age Range',
   'AGE', 'BETWEEN', '21', '60',
   'Applicant must be between 21 and 60 years old.',
   'ACTIVE', now(), now()),

  (gen_random_uuid(), 'EMPLOYMENT_STATUS_ELIGIBLE', 'Eligible Employment Status',
   'EMPLOYMENT_STATUS', 'IN', 'FULL_TIME,PART_TIME,SELF_EMPLOYED,CONTRACT', NULL,
   'Applicant must be employed (full-time, part-time, self-employed, or contract) — not unemployed.',
   'ACTIVE', now(), now()),

  (gen_random_uuid(), 'MAX_EXISTING_LOANS', 'Maximum Existing Loans',
   'EXISTING_LOAN_COUNT', 'LESS_THAN_OR_EQUAL', '2', NULL,
   'Applicant may not already have more than 2 active loans.',
   'ACTIVE', now(), now()),

  (gen_random_uuid(), 'MAX_DEBT_TO_INCOME_RATIO', 'Maximum Debt-to-Income Ratio',
   'DEBT_TO_INCOME_RATIO', 'LESS_THAN_OR_EQUAL', '40', NULL,
   'Applicant''s total debt-to-income ratio must not exceed 40%.',
   'ACTIVE', now(), now()),

  (gen_random_uuid(), 'NO_EXISTING_LOANS', 'No Existing Loans',
   'EXISTING_LOAN_COUNT', 'EQUALS', '0', NULL,
   'Applicant must have no other active loans — for first-time-borrower products.',
   'ACTIVE', now(), now())
ON CONFLICT (code) DO NOTHING;
