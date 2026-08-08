package com.example.accounting.config;

import com.example.accounting.entity.AccountType;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.repository.GlAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

// Without this, a fresh environment has no chart of accounts at all and every
// journal template/accounting scheme has nothing to bind to (see AdminSeeder
// in auth-service for the same idempotent-seed pattern this mirrors).
// @Order(1) — must run before JournalTemplateSeeder, which looks up these accounts by
// number; CommandLineRunner beans without an explicit @Order sort last (Integer.MAX_VALUE),
// which would otherwise run this after JournalTemplateSeeder's @Order(2).
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class GlAccountSeeder implements CommandLineRunner {

    private final GlAccountRepository glAccountRepository;

    @Value("${seed.gl-accounts.enabled:true}")
    private boolean enabled;

    @Value("${seed.gl-accounts.currency:USD}")
    private String currency;

    // Definition order doubles as creation order — a child's parentAccountNo
    // must already have been created (and therefore appear earlier in this
    // list) by the time its own row is processed.
    private record Def(
            String accountNo,
            String accountName,
            AccountType accountType,
            EntrySide normalBalance,
            boolean allowPosting,
            String parentAccountNo
    ) {
        Def(String accountNo, String accountName, AccountType accountType, EntrySide normalBalance) {
            this(accountNo, accountName, accountType, normalBalance, false, null);
        }

        Def(String accountNo, String accountName, AccountType accountType, EntrySide normalBalance, String parentAccountNo) {
            this(accountNo, accountName, accountType, normalBalance, true, parentAccountNo);
        }
    }

    private static final List<Def> CHART_OF_ACCOUNTS = List.of(
            // Top-level summary accounts (not directly postable) — one per AccountType.
            new Def("1000", "Assets", AccountType.ASSET, EntrySide.DEBIT),
            new Def("2000", "Liabilities", AccountType.LIABILITY, EntrySide.CREDIT),
            new Def("3000", "Equity", AccountType.EQUITY, EntrySide.CREDIT),
            new Def("4000", "Income", AccountType.INCOME, EntrySide.CREDIT),
            new Def("5000", "Expenses", AccountType.EXPENSE, EntrySide.DEBIT),

            // Assets
            new Def("1010", "Cash and Cash Equivalents", AccountType.ASSET, EntrySide.DEBIT, "1000"),
            new Def("1100", "Loans Receivable", AccountType.ASSET, EntrySide.DEBIT, "1000"),
            new Def("1150", "Interest Receivable", AccountType.ASSET, EntrySide.DEBIT, "1000"),
            new Def("1200", "Fees Receivable", AccountType.ASSET, EntrySide.DEBIT, "1000"),

            // Liabilities
            new Def("2100", "Customer Deposits", AccountType.LIABILITY, EntrySide.CREDIT, "2000"),

            // Equity
            new Def("3010", "Owner's Capital", AccountType.EQUITY, EntrySide.CREDIT, "3000"),
            new Def("3020", "Retained Earnings", AccountType.EQUITY, EntrySide.CREDIT, "3000"),

            // Income
            new Def("4010", "Interest Income", AccountType.INCOME, EntrySide.CREDIT, "4000"),
            new Def("4020", "Fee Income", AccountType.INCOME, EntrySide.CREDIT, "4000"),
            new Def("4030", "Penalty Income", AccountType.INCOME, EntrySide.CREDIT, "4000"),

            // Expenses
            new Def("5010", "Loan Write-off Expense", AccountType.EXPENSE, EntrySide.DEBIT, "5000"),
            new Def("5020", "Operating Expenses", AccountType.EXPENSE, EntrySide.DEBIT, "5000")
    );

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        int seeded = 0;
        for (Def def : CHART_OF_ACCOUNTS) {
            if (glAccountRepository.existsByAccountNo(def.accountNo())) {
                continue;
            }
            GlAccount parent = def.parentAccountNo() == null
                    ? null
                    : glAccountRepository.findByAccountNo(def.parentAccountNo())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Seed order bug: parent account " + def.parentAccountNo()
                                            + " must be seeded before " + def.accountNo()));
            glAccountRepository.save(GlAccount.builder()
                    .parent(parent)
                    .accountNo(def.accountNo())
                    .accountName(def.accountName())
                    .accountType(def.accountType())
                    .normalBalance(def.normalBalance())
                    .currency(currency)
                    .allowPosting(def.allowPosting())
                    .build());
            seeded++;
        }
        if (seeded > 0) {
            log.info("Seeded {} default GL account(s)", seeded);
        }
    }
}
