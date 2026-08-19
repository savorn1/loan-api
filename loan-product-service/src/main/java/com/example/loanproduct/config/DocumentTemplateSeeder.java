package com.example.loanproduct.config;

import com.example.loanproduct.entity.DocumentTemplate;
import com.example.loanproduct.entity.DocumentTemplateStatus;
import com.example.loanproduct.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Sample document-type catalog so a fresh environment isn't an empty list — same
// idempotent-seed pattern as accounting-service's GlAccountSeeder and
// payment-service's PaymentTransactionDefaultsSeeder. These are just the reusable
// catalog entries; which ones a given loan product actually requires is configured
// separately via loan-service's /loan-products/{id}/documents join.
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentTemplateSeeder implements CommandLineRunner {

    private final DocumentTemplateRepository documentTemplateRepository;

    @Value("${seed.document-templates.enabled:true}")
    private boolean enabled;

    private record Def(String code, String name, String description) {}

    private static final List<Def> TEMPLATES = List.of(
            new Def("ID_CARD", "National ID Card",
                    "A valid, unexpired government-issued national ID card."),
            new Def("PASSPORT", "Passport",
                    "Valid passport, required when a national ID isn't available."),
            new Def("PROOF_OF_ADDRESS", "Proof of Address",
                    "A recent utility bill, lease agreement, or similar document confirming current residence."),
            new Def("PAYSLIP", "Payslip",
                    "Most recent payslip(s) evidencing regular employment income."),
            new Def("BANK_STATEMENT", "Bank Statement",
                    "Bank statements covering the last 3-6 months, used to verify cash flow."),
            new Def("BUSINESS_LICENSE", "Business License",
                    "Valid business registration or operating license, for self-employed or business-loan applicants."),
            new Def("TAX_RETURN", "Tax Return",
                    "Most recent annual tax filing, for income verification on larger loans."),
            new Def("COLLATERAL_TITLE_DEED", "Collateral Title Deed",
                    "Ownership title/deed for any asset pledged as collateral."),
            new Def("GUARANTOR_ID", "Guarantor ID Card",
                    "Valid government-issued ID for each guarantor on the loan."),
            new Def("LOAN_APPLICATION_FORM", "Signed Loan Application Form",
                    "The completed and signed loan application form.")
    );

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        int seeded = 0;
        for (Def def : TEMPLATES) {
            if (documentTemplateRepository.existsByCode(def.code())) {
                continue;
            }
            documentTemplateRepository.save(DocumentTemplate.builder()
                    .code(def.code())
                    .name(def.name())
                    .description(def.description())
                    .status(DocumentTemplateStatus.ACTIVE)
                    .build());
            seeded++;
        }
        if (seeded > 0) {
            log.info("Seeded {} default document template(s)", seeded);
        }
    }
}
