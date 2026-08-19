package com.example.loanproduct.config;

import com.example.loanproduct.entity.RuleField;
import com.example.loanproduct.entity.RuleOperator;
import com.example.loanproduct.entity.RuleTemplate;
import com.example.loanproduct.entity.RuleTemplateStatus;
import com.example.loanproduct.repository.RuleTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Sample eligibility-rule catalog so a fresh environment isn't an empty list — same
// idempotent-seed pattern as DocumentTemplateSeeder. These are just the reusable
// catalog entries; which ones a given loan product actually enforces is configured
// separately via loan-service's /loan-products/{id}/rules join.
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleTemplateSeeder implements CommandLineRunner {

    private final RuleTemplateRepository ruleTemplateRepository;

    @Value("${seed.rule-templates.enabled:true}")
    private boolean enabled;

    private record Def(
            String code, String name, RuleField field, RuleOperator operator,
            String value, String value2, String description
    ) {
        Def(String code, String name, RuleField field, RuleOperator operator, String value, String description) {
            this(code, name, field, operator, value, null, description);
        }
    }

    private static final List<Def> TEMPLATES = List.of(
            new Def("MIN_CREDIT_SCORE", "Minimum Credit Score",
                    RuleField.CREDIT_SCORE, RuleOperator.GREATER_THAN_OR_EQUAL, "650",
                    "Applicant's credit score must be at least 650."),
            new Def("MIN_CREDIT_SCORE_PREMIUM", "Minimum Credit Score (Premium)",
                    RuleField.CREDIT_SCORE, RuleOperator.GREATER_THAN_OR_EQUAL, "750",
                    "Higher credit score bar for premium/lower-rate products."),
            new Def("MIN_MONTHLY_INCOME", "Minimum Monthly Income",
                    RuleField.MONTHLY_INCOME, RuleOperator.GREATER_THAN_OR_EQUAL, "500",
                    "Applicant's verified monthly income must be at least 500."),
            new Def("APPLICANT_AGE_RANGE", "Applicant Age Range",
                    RuleField.AGE, RuleOperator.BETWEEN, "21", "60",
                    "Applicant must be between 21 and 60 years old."),
            new Def("EMPLOYMENT_STATUS_ELIGIBLE", "Eligible Employment Status",
                    RuleField.EMPLOYMENT_STATUS, RuleOperator.IN, "EMPLOYED,SELF_EMPLOYED",
                    "Applicant must be employed or self-employed."),
            new Def("MAX_EXISTING_LOANS", "Maximum Existing Loans",
                    RuleField.EXISTING_LOAN_COUNT, RuleOperator.LESS_THAN_OR_EQUAL, "2",
                    "Applicant may not already have more than 2 active loans."),
            new Def("MAX_DEBT_TO_INCOME_RATIO", "Maximum Debt-to-Income Ratio",
                    RuleField.DEBT_TO_INCOME_RATIO, RuleOperator.LESS_THAN_OR_EQUAL, "40",
                    "Applicant's total debt-to-income ratio must not exceed 40%."),
            new Def("NO_EXISTING_LOANS", "No Existing Loans",
                    RuleField.EXISTING_LOAN_COUNT, RuleOperator.EQUALS, "0",
                    "Applicant must have no other active loans — for first-time-borrower products.")
    );

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        int seeded = 0;
        for (Def def : TEMPLATES) {
            if (ruleTemplateRepository.existsByCode(def.code())) {
                continue;
            }
            ruleTemplateRepository.save(RuleTemplate.builder()
                    .code(def.code())
                    .name(def.name())
                    .field(def.field())
                    .operator(def.operator())
                    .value(def.value())
                    .value2(def.value2())
                    .description(def.description())
                    .status(RuleTemplateStatus.ACTIVE)
                    .build());
            seeded++;
        }
        if (seeded > 0) {
            log.info("Seeded {} default rule template(s)", seeded);
        }
    }
}
