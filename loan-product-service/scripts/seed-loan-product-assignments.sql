-- Wires the reusable catalogs (interest schemes, fee schemes, term templates,
-- rule templates, document templates) onto the loan products from
-- seed-loan-products.sql, via the five join tables:
--   loan_product_interest_scheme, loan_product_fee_scheme, loan_product_term,
--   loan_product_rule, loan_product_document
-- Run against loan_product_db, e.g.:
--   psql "postgresql://postgres:@localhost:5432/loan_product_db" -f scripts/seed-loan-product-assignments.sql
--
-- None of the join tables have a unique constraint beyond their own surrogate
-- key, so idempotency here is a NOT EXISTS guard on (loan_product_id, *_id)
-- rather than ON CONFLICT — safe to re-run.

-- ============================================================
-- PL-STD-01 — Standard Personal Loan
-- ============================================================

INSERT INTO loan_product_interest_scheme (id, loan_product_id, interest_scheme_id, priority, effective_from, effective_to, is_default, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, 1, '2026-01-01', NULL, true, 'ACTIVE', now(), now()
FROM loan_products lp, interest_schemes s
WHERE lp.code = 'PL-STD-01' AND s.code = 'STD-REDUCING'
  AND NOT EXISTS (SELECT 1 FROM loan_product_interest_scheme x WHERE x.loan_product_id = lp.id AND x.interest_scheme_id = s.id);

INSERT INTO loan_product_fee_scheme (id, loan_product_id, fee_scheme_id, is_mandatory, priority, effective_from, effective_to, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, true, 1, '2026-01-01', NULL, 'ACTIVE', now(), now()
FROM loan_products lp, fee_schemes s
WHERE lp.code = 'PL-STD-01' AND s.code = 'STD-FEES'
  AND NOT EXISTS (SELECT 1 FROM loan_product_fee_scheme x WHERE x.loan_product_id = lp.id AND x.fee_scheme_id = s.id);

INSERT INTO loan_product_term (loan_product_id, term_template_id, is_default, status, created_at, updated_at)
SELECT lp.id, t.id, (t.code = 'TERM_12M'), 'ACTIVE', now(), now()
FROM loan_products lp, term_templates t
WHERE lp.code = 'PL-STD-01' AND t.code IN ('TERM_6M', 'TERM_12M', 'TERM_24M', 'TERM_36M')
  AND NOT EXISTS (SELECT 1 FROM loan_product_term x WHERE x.loan_product_id = lp.id AND x.term_template_id = t.id);

INSERT INTO loan_product_rule (loan_product_id, rule_template_id, status, created_at, updated_at)
SELECT lp.id, r.id, 'ACTIVE', now(), now()
FROM loan_products lp, rule_templates r
WHERE lp.code = 'PL-STD-01'
  AND r.code IN ('MIN_CREDIT_SCORE', 'MIN_MONTHLY_INCOME', 'APPLICANT_AGE_RANGE', 'MAX_EXISTING_LOANS')
  AND NOT EXISTS (SELECT 1 FROM loan_product_rule x WHERE x.loan_product_id = lp.id AND x.rule_template_id = r.id);

INSERT INTO loan_product_document (loan_product_id, document_template_id, required, status, created_at, updated_at)
SELECT lp.id, d.id, (d.code != 'BANK_STATEMENT'), 'ACTIVE', now(), now()
FROM loan_products lp, document_templates d
WHERE lp.code = 'PL-STD-01' AND d.code IN ('ID_CARD', 'PROOF_OF_ADDRESS', 'PAYSLIP', 'BANK_STATEMENT')
  AND NOT EXISTS (SELECT 1 FROM loan_product_document x WHERE x.loan_product_id = lp.id AND x.document_template_id = d.id);

-- ============================================================
-- HL-30Y-01 — Home Purchase Mortgage
-- ============================================================

INSERT INTO loan_product_interest_scheme (id, loan_product_id, interest_scheme_id, priority, effective_from, effective_to, is_default, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, 1, '2026-01-01', NULL, true, 'ACTIVE', now(), now()
FROM loan_products lp, interest_schemes s
WHERE lp.code = 'HL-30Y-01' AND s.code = 'MORTGAGE-REDUCING'
  AND NOT EXISTS (SELECT 1 FROM loan_product_interest_scheme x WHERE x.loan_product_id = lp.id AND x.interest_scheme_id = s.id);

INSERT INTO loan_product_fee_scheme (id, loan_product_id, fee_scheme_id, is_mandatory, priority, effective_from, effective_to, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, true, 1, '2026-01-01', NULL, 'ACTIVE', now(), now()
FROM loan_products lp, fee_schemes s
WHERE lp.code = 'HL-30Y-01' AND s.code = 'MORTGAGE-FEES'
  AND NOT EXISTS (SELECT 1 FROM loan_product_fee_scheme x WHERE x.loan_product_id = lp.id AND x.fee_scheme_id = s.id);

-- Only 5-year sample terms exist in the catalog today — illustrative, not a
-- real 30-year amortization schedule (see seed-term-templates.sql).
INSERT INTO loan_product_term (loan_product_id, term_template_id, is_default, status, created_at, updated_at)
SELECT lp.id, t.id, (t.code = 'TERM_5Y'), 'ACTIVE', now(), now()
FROM loan_products lp, term_templates t
WHERE lp.code = 'HL-30Y-01' AND t.code IN ('TERM_60M', 'TERM_5Y')
  AND NOT EXISTS (SELECT 1 FROM loan_product_term x WHERE x.loan_product_id = lp.id AND x.term_template_id = t.id);

INSERT INTO loan_product_rule (loan_product_id, rule_template_id, status, created_at, updated_at)
SELECT lp.id, r.id, 'ACTIVE', now(), now()
FROM loan_products lp, rule_templates r
WHERE lp.code = 'HL-30Y-01' AND r.code IN ('MIN_CREDIT_SCORE_PREMIUM', 'MIN_MONTHLY_INCOME')
  AND NOT EXISTS (SELECT 1 FROM loan_product_rule x WHERE x.loan_product_id = lp.id AND x.rule_template_id = r.id);

INSERT INTO loan_product_document (loan_product_id, document_template_id, required, status, created_at, updated_at)
SELECT lp.id, d.id, (d.code != 'GUARANTOR_ID'), 'ACTIVE', now(), now()
FROM loan_products lp, document_templates d
WHERE lp.code = 'HL-30Y-01'
  AND d.code IN ('ID_CARD', 'PROOF_OF_ADDRESS', 'PAYSLIP', 'BANK_STATEMENT', 'COLLATERAL_TITLE_DEED', 'TAX_RETURN', 'GUARANTOR_ID')
  AND NOT EXISTS (SELECT 1 FROM loan_product_document x WHERE x.loan_product_id = lp.id AND x.document_template_id = d.id);

-- ============================================================
-- AL-NEW-01 — New Vehicle Auto Loan
-- ============================================================

INSERT INTO loan_product_interest_scheme (id, loan_product_id, interest_scheme_id, priority, effective_from, effective_to, is_default, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, 1, '2026-01-01', NULL, true, 'ACTIVE', now(), now()
FROM loan_products lp, interest_schemes s
WHERE lp.code = 'AL-NEW-01' AND s.code = 'STD-FLAT'
  AND NOT EXISTS (SELECT 1 FROM loan_product_interest_scheme x WHERE x.loan_product_id = lp.id AND x.interest_scheme_id = s.id);

INSERT INTO loan_product_fee_scheme (id, loan_product_id, fee_scheme_id, is_mandatory, priority, effective_from, effective_to, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, true, 1, '2026-01-01', NULL, 'ACTIVE', now(), now()
FROM loan_products lp, fee_schemes s
WHERE lp.code = 'AL-NEW-01' AND s.code = 'STD-FEES'
  AND NOT EXISTS (SELECT 1 FROM loan_product_fee_scheme x WHERE x.loan_product_id = lp.id AND x.fee_scheme_id = s.id);

INSERT INTO loan_product_term (loan_product_id, term_template_id, is_default, status, created_at, updated_at)
SELECT lp.id, t.id, (t.code = 'TERM_36M'), 'ACTIVE', now(), now()
FROM loan_products lp, term_templates t
WHERE lp.code = 'AL-NEW-01' AND t.code IN ('TERM_12M', 'TERM_24M', 'TERM_36M')
  AND NOT EXISTS (SELECT 1 FROM loan_product_term x WHERE x.loan_product_id = lp.id AND x.term_template_id = t.id);

INSERT INTO loan_product_rule (loan_product_id, rule_template_id, status, created_at, updated_at)
SELECT lp.id, r.id, 'ACTIVE', now(), now()
FROM loan_products lp, rule_templates r
WHERE lp.code = 'AL-NEW-01' AND r.code IN ('MIN_CREDIT_SCORE', 'MAX_EXISTING_LOANS')
  AND NOT EXISTS (SELECT 1 FROM loan_product_rule x WHERE x.loan_product_id = lp.id AND x.rule_template_id = r.id);

INSERT INTO loan_product_document (loan_product_id, document_template_id, required, status, created_at, updated_at)
SELECT lp.id, d.id, true, 'ACTIVE', now(), now()
FROM loan_products lp, document_templates d
WHERE lp.code = 'AL-NEW-01' AND d.code IN ('ID_CARD', 'PROOF_OF_ADDRESS', 'PAYSLIP')
  AND NOT EXISTS (SELECT 1 FROM loan_product_document x WHERE x.loan_product_id = lp.id AND x.document_template_id = d.id);

-- ============================================================
-- BL-SME-01 — Small Business Working Capital Loan
-- ============================================================

INSERT INTO loan_product_interest_scheme (id, loan_product_id, interest_scheme_id, priority, effective_from, effective_to, is_default, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, 1, '2026-09-01', NULL, true, 'ACTIVE', now(), now()
FROM loan_products lp, interest_schemes s
WHERE lp.code = 'BL-SME-01' AND s.code = 'STD-REDUCING'
  AND NOT EXISTS (SELECT 1 FROM loan_product_interest_scheme x WHERE x.loan_product_id = lp.id AND x.interest_scheme_id = s.id);

INSERT INTO loan_product_fee_scheme (id, loan_product_id, fee_scheme_id, is_mandatory, priority, effective_from, effective_to, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, true, 1, '2026-09-01', NULL, 'ACTIVE', now(), now()
FROM loan_products lp, fee_schemes s
WHERE lp.code = 'BL-SME-01' AND s.code = 'STD-FEES'
  AND NOT EXISTS (SELECT 1 FROM loan_product_fee_scheme x WHERE x.loan_product_id = lp.id AND x.fee_scheme_id = s.id);

INSERT INTO loan_product_term (loan_product_id, term_template_id, is_default, status, created_at, updated_at)
SELECT lp.id, t.id, (t.code = 'TERM_24M'), 'ACTIVE', now(), now()
FROM loan_products lp, term_templates t
WHERE lp.code = 'BL-SME-01' AND t.code IN ('TERM_12M', 'TERM_24M', 'TERM_36M')
  AND NOT EXISTS (SELECT 1 FROM loan_product_term x WHERE x.loan_product_id = lp.id AND x.term_template_id = t.id);

INSERT INTO loan_product_rule (loan_product_id, rule_template_id, status, created_at, updated_at)
SELECT lp.id, r.id, 'ACTIVE', now(), now()
FROM loan_products lp, rule_templates r
WHERE lp.code = 'BL-SME-01'
  AND r.code IN ('MIN_CREDIT_SCORE', 'EMPLOYMENT_STATUS_ELIGIBLE', 'MAX_DEBT_TO_INCOME_RATIO')
  AND NOT EXISTS (SELECT 1 FROM loan_product_rule x WHERE x.loan_product_id = lp.id AND x.rule_template_id = r.id);

INSERT INTO loan_product_document (loan_product_id, document_template_id, required, status, created_at, updated_at)
SELECT lp.id, d.id, true, 'ACTIVE', now(), now()
FROM loan_products lp, document_templates d
WHERE lp.code = 'BL-SME-01' AND d.code IN ('ID_CARD', 'BUSINESS_LICENSE', 'TAX_RETURN', 'BANK_STATEMENT')
  AND NOT EXISTS (SELECT 1 FROM loan_product_document x WHERE x.loan_product_id = lp.id AND x.document_template_id = d.id);

-- ============================================================
-- EDU-TUI-01 — Tuition Support Loan
-- ============================================================

INSERT INTO loan_product_interest_scheme (id, loan_product_id, interest_scheme_id, priority, effective_from, effective_to, is_default, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, 1, '2026-01-01', NULL, true, 'ACTIVE', now(), now()
FROM loan_products lp, interest_schemes s
WHERE lp.code = 'EDU-TUI-01' AND s.code = 'STD-FLAT'
  AND NOT EXISTS (SELECT 1 FROM loan_product_interest_scheme x WHERE x.loan_product_id = lp.id AND x.interest_scheme_id = s.id);

INSERT INTO loan_product_fee_scheme (id, loan_product_id, fee_scheme_id, is_mandatory, priority, effective_from, effective_to, status, created_at, updated_at)
SELECT gen_random_uuid(), lp.id, s.id, true, 1, '2026-01-01', NULL, 'ACTIVE', now(), now()
FROM loan_products lp, fee_schemes s
WHERE lp.code = 'EDU-TUI-01' AND s.code = 'PREMIUM-FEES'
  AND NOT EXISTS (SELECT 1 FROM loan_product_fee_scheme x WHERE x.loan_product_id = lp.id AND x.fee_scheme_id = s.id);

INSERT INTO loan_product_term (loan_product_id, term_template_id, is_default, status, created_at, updated_at)
SELECT lp.id, t.id, (t.code = 'TERM_12M'), 'ACTIVE', now(), now()
FROM loan_products lp, term_templates t
WHERE lp.code = 'EDU-TUI-01' AND t.code IN ('TERM_12M', 'TERM_24M', 'TERM_36M')
  AND NOT EXISTS (SELECT 1 FROM loan_product_term x WHERE x.loan_product_id = lp.id AND x.term_template_id = t.id);

INSERT INTO loan_product_rule (loan_product_id, rule_template_id, status, created_at, updated_at)
SELECT lp.id, r.id, 'ACTIVE', now(), now()
FROM loan_products lp, rule_templates r
WHERE lp.code = 'EDU-TUI-01' AND r.code IN ('MIN_MONTHLY_INCOME', 'APPLICANT_AGE_RANGE')
  AND NOT EXISTS (SELECT 1 FROM loan_product_rule x WHERE x.loan_product_id = lp.id AND x.rule_template_id = r.id);

INSERT INTO loan_product_document (loan_product_id, document_template_id, required, status, created_at, updated_at)
SELECT lp.id, d.id, true, 'ACTIVE', now(), now()
FROM loan_products lp, document_templates d
WHERE lp.code = 'EDU-TUI-01' AND d.code IN ('ID_CARD', 'PROOF_OF_ADDRESS', 'LOAN_APPLICATION_FORM')
  AND NOT EXISTS (SELECT 1 FROM loan_product_document x WHERE x.loan_product_id = lp.id AND x.document_template_id = d.id);
