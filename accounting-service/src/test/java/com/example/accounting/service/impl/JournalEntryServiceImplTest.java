package com.example.accounting.service.impl;

import com.example.accounting.dto.JournalEntryGenerateRequest;
import com.example.accounting.dto.JournalEntryResponse;
import com.example.accounting.entity.AccountingScheme;
import com.example.accounting.entity.AccountingSchemeStatus;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.FinancialPeriodStatus;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.entity.JournalEntry;
import com.example.accounting.entity.JournalEntryStatus;
import com.example.accounting.entity.JournalTemplate;
import com.example.accounting.entity.JournalTemplateLine;
import com.example.accounting.entity.JournalTemplateStatus;
import com.example.accounting.entity.TransactionType;
import com.example.accounting.exception.AppException;
import com.example.accounting.repository.AccountingSchemeRepository;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.repository.JournalEntryRepository;
import com.example.accounting.repository.JournalTemplateRepository;
import com.example.accounting.service.GeneralLedgerService;
import com.example.accounting.service.JournalAuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Covers JournalEntryServiceImpl.generate() — the endpoint loan-service/payment-service call
// instead of building lines themselves (see AccountingClient in loan-service). These are the
// behaviors that actually matter for correctness: template/scheme resolution, idempotency
// (loan-service retries on Feign timeout must not double-post), and the config-gap error
// cases (missing template/scheme) that block a real disbursement/payment if misconfigured.
@ExtendWith(MockitoExtension.class)
class JournalEntryServiceImplTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private FinancialPeriodRepository financialPeriodRepository;
    @Mock
    private GlAccountRepository glAccountRepository;
    @Mock
    private JournalTemplateRepository journalTemplateRepository;
    @Mock
    private AccountingSchemeRepository accountingSchemeRepository;
    @Mock
    private GeneralLedgerService generalLedgerService;
    @Mock
    private JournalAuditLogService journalAuditLogService;

    private JournalEntryServiceImpl service;

    private FinancialPeriod openPeriod;
    private GlAccount receivableAccount;
    private GlAccount cashAccount;
    private JournalTemplate disbursementTemplate;

    @BeforeEach
    void setUp() {
        service = new JournalEntryServiceImpl(journalEntryRepository, financialPeriodRepository, glAccountRepository,
                journalTemplateRepository, accountingSchemeRepository, generalLedgerService, journalAuditLogService);

        openPeriod = FinancialPeriod.builder()
                .periodName("2026-08")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .status(FinancialPeriodStatus.OPEN)
                .build();
        openPeriod.setId(1L);

        receivableAccount = GlAccount.builder().accountNo("1100").allowPosting(true).build();
        receivableAccount.setId(7L);
        cashAccount = GlAccount.builder().accountNo("1010").allowPosting(true).build();
        cashAccount.setId(6L);

        disbursementTemplate = JournalTemplate.builder()
                .code("DISBURSEMENT")
                .transactionType(TransactionType.DISBURSEMENT)
                .status(JournalTemplateStatus.ACTIVE)
                .build();
        disbursementTemplate.setId(2L);
        disbursementTemplate.getLines().add(JournalTemplateLine.builder()
                .journalTemplate(disbursementTemplate).lineNo(1).accountRole("LOAN_RECEIVABLE")
                .entrySide(EntrySide.DEBIT).description("Principal disbursed").build());
        disbursementTemplate.getLines().add(JournalTemplateLine.builder()
                .journalTemplate(disbursementTemplate).lineNo(2).accountRole("CASH")
                .entrySide(EntrySide.CREDIT).description("Cash disbursed to borrower").build());
    }

    private JournalEntryGenerateRequest disbursementRequest() {
        JournalEntryGenerateRequest request = new JournalEntryGenerateRequest();
        request.setTransactionType(TransactionType.DISBURSEMENT);
        request.setTransactionDate(LocalDate.of(2026, 8, 8));
        request.setBranchId(1L);
        request.setReferenceType("LoanTransaction");
        request.setReferenceId("257");
        request.setAmount(new BigDecimal("1000.00"));
        return request;
    }

    // In-memory "database" for journal_entries: save() assigns an id on first insert and
    // findById returns whatever was last saved under that id, same as the two-phase
    // save-then-set-entryNo-then-save-again dance in create()/post().
    private void stubJournalEntryPersistence() {
        AtomicLong nextId = new AtomicLong(1);
        java.util.Map<Long, JournalEntry> table = new java.util.HashMap<>();
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> {
            JournalEntry entry = inv.getArgument(0);
            if (entry.getId() == null) {
                entry.setId(nextId.getAndIncrement());
            }
            table.put(entry.getId(), entry);
            return entry;
        });
        when(journalEntryRepository.findById(any())).thenAnswer(inv -> Optional.ofNullable(table.get(inv.getArgument(0))));
    }

    @Test
    void generate_resolvesTemplateAndSchemeAndPostsBalancedEntry() {
        stubJournalEntryPersistence();
        when(journalEntryRepository.findByTransactionTypeAndReferenceTypeAndReferenceId(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(journalTemplateRepository.findByTransactionType(TransactionType.DISBURSEMENT))
                .thenReturn(List.of(disbursementTemplate));
        when(accountingSchemeRepository.findByJournalTemplateIdAndAccountRoleAndCurrency(2L, "LOAN_RECEIVABLE", "USD"))
                .thenReturn(Optional.of(AccountingScheme.builder()
                        .journalTemplate(disbursementTemplate).accountRole("LOAN_RECEIVABLE")
                        .glAccount(receivableAccount).currency("USD").status(AccountingSchemeStatus.ACTIVE).build()));
        when(accountingSchemeRepository.findByJournalTemplateIdAndAccountRoleAndCurrency(2L, "CASH", "USD"))
                .thenReturn(Optional.of(AccountingScheme.builder()
                        .journalTemplate(disbursementTemplate).accountRole("CASH")
                        .glAccount(cashAccount).currency("USD").status(AccountingSchemeStatus.ACTIVE).build()));
        when(financialPeriodRepository.findByDateWithinRange(any())).thenReturn(Optional.of(openPeriod));
        when(glAccountRepository.findById(7L)).thenReturn(Optional.of(receivableAccount));
        when(glAccountRepository.findById(6L)).thenReturn(Optional.of(cashAccount));

        JournalEntryResponse response = service.generate(disbursementRequest());

        assertThat(response.getStatus()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getLines()).hasSize(2);
        assertThat(response.getLines())
                .anySatisfy(line -> {
                    assertThat(line.getGlAccountNo()).isEqualTo("1100");
                    assertThat(line.getEntrySide()).isEqualTo(EntrySide.DEBIT);
                    assertThat(line.getAmount()).isEqualByComparingTo("1000.00");
                })
                .anySatisfy(line -> {
                    assertThat(line.getGlAccountNo()).isEqualTo("1010");
                    assertThat(line.getEntrySide()).isEqualTo(EntrySide.CREDIT);
                    assertThat(line.getAmount()).isEqualByComparingTo("1000.00");
                });
        verify(generalLedgerService).applyPostedEntry(any(JournalEntry.class));
    }

    @Test
    void generate_isIdempotentOnRetriedReference() {
        JournalEntry alreadyPosted = JournalEntry.builder()
                .transactionType(TransactionType.DISBURSEMENT)
                .transactionDate(LocalDate.of(2026, 8, 8))
                .financialPeriod(openPeriod)
                .referenceType("LoanTransaction")
                .referenceId("257")
                .currency("USD")
                .status(JournalEntryStatus.POSTED)
                .build();
        alreadyPosted.setId(99L);
        when(journalEntryRepository.findByTransactionTypeAndReferenceTypeAndReferenceId(
                TransactionType.DISBURSEMENT, "LoanTransaction", "257"))
                .thenReturn(Optional.of(alreadyPosted));

        JournalEntryResponse response = service.generate(disbursementRequest());

        assertThat(response.getId()).isEqualTo(99L);
        verifyNoInteractions(journalTemplateRepository, accountingSchemeRepository);
    }

    @Test
    void generate_throwsWhenNoActiveTemplateConfigured() {
        when(journalEntryRepository.findByTransactionTypeAndReferenceTypeAndReferenceId(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(journalTemplateRepository.findByTransactionType(TransactionType.DISBURSEMENT)).thenReturn(List.of());

        assertThatThrownBy(() -> service.generate(disbursementRequest()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("No active journal template");
    }

    @Test
    void generate_throwsWhenMultipleActiveTemplatesConfigured() {
        JournalTemplate duplicate = JournalTemplate.builder()
                .code("jour1").transactionType(TransactionType.DISBURSEMENT)
                .status(JournalTemplateStatus.ACTIVE).build();
        duplicate.setId(3L);
        when(journalEntryRepository.findByTransactionTypeAndReferenceTypeAndReferenceId(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(journalTemplateRepository.findByTransactionType(TransactionType.DISBURSEMENT))
                .thenReturn(List.of(disbursementTemplate, duplicate));

        assertThatThrownBy(() -> service.generate(disbursementRequest()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Multiple active journal templates");
    }

    @Test
    void generate_throwsWhenAccountingSchemeMissingForRole() {
        when(journalEntryRepository.findByTransactionTypeAndReferenceTypeAndReferenceId(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(journalTemplateRepository.findByTransactionType(TransactionType.DISBURSEMENT))
                .thenReturn(List.of(disbursementTemplate));
        when(accountingSchemeRepository.findByJournalTemplateIdAndAccountRoleAndCurrency(eq(2L), any(), eq("USD")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(disbursementRequest()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("No active accounting scheme binds role");
    }
}
