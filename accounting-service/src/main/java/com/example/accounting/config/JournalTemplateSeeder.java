package com.example.accounting.config;

import com.example.accounting.entity.AccountingScheme;
import com.example.accounting.entity.AccountingSchemeStatus;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.entity.JournalTemplate;
import com.example.accounting.entity.JournalTemplateLine;
import com.example.accounting.entity.JournalTemplateStatus;
import com.example.accounting.entity.TransactionType;
import com.example.accounting.repository.AccountingSchemeRepository;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.repository.JournalTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Runs after GlAccountSeeder (see @Order) and binds a default two-line template + USD
// scheme for every TransactionType that JournalEntryServiceImpl.generate can be called
// with today (loan-service's disbursement/payment/write-off flows). Without this, a fresh
// environment has a chart of accounts but generate() 400s on every call — there's nothing
// for an account role to resolve to. PAYMENT_REVERSAL is intentionally not seeded: nothing
// calls generate() with it, and /{id}/reverse builds its entry directly from the original
// rather than through a template.
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class JournalTemplateSeeder implements CommandLineRunner {

    private final JournalTemplateRepository journalTemplateRepository;
    private final AccountingSchemeRepository accountingSchemeRepository;
    private final GlAccountRepository glAccountRepository;

    @Value("${seed.journal-templates.enabled:true}")
    private boolean enabled;

    @Value("${seed.gl-accounts.currency:USD}")
    private String currency;

    private record LineDef(int lineNo, String accountRole, EntrySide entrySide, String glAccountNo, String description) {
    }

    private record TemplateDef(String code, String name, TransactionType transactionType, List<LineDef> lines) {
    }

    private static final List<TemplateDef> TEMPLATES = List.of(
            new TemplateDef("DISBURSEMENT", "Loan Disbursement", TransactionType.DISBURSEMENT, List.of(
                    new LineDef(1, "LOAN_RECEIVABLE", EntrySide.DEBIT, "1100", "Principal disbursed"),
                    new LineDef(2, "CASH", EntrySide.CREDIT, "1010", "Cash disbursed to borrower"))),
            new TemplateDef("PRINCIPAL_PAYMENT", "Principal Repayment", TransactionType.PRINCIPAL_PAYMENT, List.of(
                    new LineDef(1, "CASH", EntrySide.DEBIT, "1010", "Principal received"),
                    new LineDef(2, "LOAN_RECEIVABLE", EntrySide.CREDIT, "1100", "Principal balance reduced"))),
            new TemplateDef("INTEREST_PAYMENT", "Interest Repayment", TransactionType.INTEREST_PAYMENT, List.of(
                    new LineDef(1, "CASH", EntrySide.DEBIT, "1010", "Interest received"),
                    new LineDef(2, "INTEREST_INCOME", EntrySide.CREDIT, "4010", "Interest income earned"))),
            new TemplateDef("FEE_CHARGE", "Fee Collection", TransactionType.FEE_CHARGE, List.of(
                    new LineDef(1, "CASH", EntrySide.DEBIT, "1010", "Fee received"),
                    new LineDef(2, "FEE_INCOME", EntrySide.CREDIT, "4020", "Fee income earned"))),
            new TemplateDef("PENALTY_CHARGE", "Penalty Collection", TransactionType.PENALTY_CHARGE, List.of(
                    new LineDef(1, "CASH", EntrySide.DEBIT, "1010", "Penalty received"),
                    new LineDef(2, "PENALTY_INCOME", EntrySide.CREDIT, "4030", "Penalty income earned"))),
            new TemplateDef("LOAN_WRITE_OFF", "Loan Write-off", TransactionType.LOAN_WRITE_OFF, List.of(
                    new LineDef(1, "WRITEOFF_EXPENSE", EntrySide.DEBIT, "5010", "Write-off expense recognized"),
                    new LineDef(2, "LOAN_RECEIVABLE", EntrySide.CREDIT, "1100", "Uncollectible principal removed"))),
            new TemplateDef("RECOVERY", "Bad Debt Recovery", TransactionType.RECOVERY, List.of(
                    new LineDef(1, "CASH", EntrySide.DEBIT, "1010", "Cash recovered on a written-off loan"),
                    new LineDef(2, "RECOVERY_INCOME", EntrySide.CREDIT, "4040", "Bad debt recovery income")))
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        int seeded = 0;
        for (TemplateDef def : TEMPLATES) {
            JournalTemplate template = journalTemplateRepository.findByCode(def.code())
                    .orElse(null);
            if (template == null) {
                template = JournalTemplate.builder()
                        .code(def.code())
                        .name(def.name())
                        .transactionType(def.transactionType())
                        .description("Seeded default template for " + def.transactionType())
                        .status(JournalTemplateStatus.ACTIVE)
                        .build();
                for (LineDef lineDef : def.lines()) {
                    template.getLines().add(JournalTemplateLine.builder()
                            .journalTemplate(template)
                            .lineNo(lineDef.lineNo())
                            .accountRole(lineDef.accountRole())
                            .entrySide(lineDef.entrySide())
                            .description(lineDef.description())
                            .build());
                }
                template = journalTemplateRepository.save(template);
                seeded++;
            }

            for (LineDef lineDef : def.lines()) {
                if (accountingSchemeRepository.existsByJournalTemplateIdAndAccountRoleAndCurrency(
                        template.getId(), lineDef.accountRole(), currency)) {
                    continue;
                }
                GlAccount glAccount = glAccountRepository.findByAccountNo(lineDef.glAccountNo())
                        .orElseThrow(() -> new IllegalStateException(
                                "Seed order bug: GL account " + lineDef.glAccountNo()
                                        + " must be seeded before journal templates (see GlAccountSeeder)"));
                accountingSchemeRepository.save(AccountingScheme.builder()
                        .journalTemplate(template)
                        .accountRole(lineDef.accountRole())
                        .glAccount(glAccount)
                        .currency(currency)
                        .status(AccountingSchemeStatus.ACTIVE)
                        .build());
            }
        }
        if (seeded > 0) {
            log.info("Seeded {} default journal template(s) with matching {} accounting schemes", seeded, currency);
        }
    }
}
