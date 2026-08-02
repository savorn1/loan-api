package com.example.accounting.service.impl;

import com.example.accounting.dto.BranchLedgerLineResponse;
import com.example.accounting.dto.BranchLedgerResponse;
import com.example.accounting.dto.DateRangeLedgerResponse;
import com.example.accounting.dto.GeneralLedgerResponse;
import com.example.accounting.dto.GeneralLedgerSummaryRow;
import com.example.accounting.dto.LedgerLineResponse;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.GeneralLedger;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.entity.JournalEntry;
import com.example.accounting.entity.JournalEntryLine;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.repository.GeneralLedgerRepository;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.repository.JournalEntryLineRepository;
import com.example.accounting.service.GeneralLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneralLedgerServiceImpl implements GeneralLedgerService {

    private final GeneralLedgerRepository generalLedgerRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GlAccountRepository glAccountRepository;
    private final FinancialPeriodRepository financialPeriodRepository;

    @Override
    public GeneralLedgerResponse getLedger(Long glAccountId, Long financialPeriodId) {
        GlAccount account = glAccountRepository.findById(glAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("GL account", glAccountId));
        FinancialPeriod period = financialPeriodRepository.findById(financialPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial period", financialPeriodId));

        GeneralLedger ledger = generalLedgerRepository.findByGlAccountIdAndFinancialPeriodId(glAccountId, financialPeriodId)
                .orElse(null);
        BigDecimal opening = ledger != null ? ledger.getOpeningBalance() : resolveOpeningBalance(glAccountId, period);
        BigDecimal periodDebit = ledger != null ? ledger.getPeriodDebitTotal() : BigDecimal.ZERO;
        BigDecimal periodCredit = ledger != null ? ledger.getPeriodCreditTotal() : BigDecimal.ZERO;
        BigDecimal closing = ledger != null ? ledger.getClosingBalance() : opening;

        List<JournalEntryLine> postedLines = journalEntryLineRepository.findPostedLinesForAccountAndPeriod(glAccountId, financialPeriodId);
        List<LedgerLineResponse> lines = new ArrayList<>();
        BigDecimal runningBalance = opening;
        for (JournalEntryLine line : postedLines) {
            runningBalance = runningBalance.add(signedAmount(line, account.getNormalBalance()));
            lines.add(LedgerLineResponse.builder()
                    .entryNo(line.getJournalEntry().getEntryNo())
                    .transactionDate(line.getJournalEntry().getTransactionDate())
                    .description(line.getDescription())
                    .entrySide(line.getEntrySide())
                    .amount(line.getAmount())
                    .runningBalance(runningBalance)
                    .build());
        }

        return GeneralLedgerResponse.builder()
                .glAccountId(account.getId())
                .accountNo(account.getAccountNo())
                .accountName(account.getAccountName())
                .financialPeriodId(period.getId())
                .financialPeriodName(period.getPeriodName())
                .openingBalance(opening)
                .periodDebitTotal(periodDebit)
                .periodCreditTotal(periodCredit)
                .closingBalance(closing)
                .lines(lines)
                .build();
    }

    @Override
    public List<GeneralLedgerSummaryRow> getAllLedgers(Long financialPeriodId) {
        FinancialPeriod period = financialPeriodRepository.findById(financialPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial period", financialPeriodId));
        Map<Long, GeneralLedger> ledgerByAccountId = generalLedgerRepository.findByFinancialPeriodId(financialPeriodId).stream()
                .collect(Collectors.toMap(gl -> gl.getGlAccount().getId(), gl -> gl));

        return glAccountRepository.findAll().stream()
                .map(account -> {
                    GeneralLedger ledger = ledgerByAccountId.get(account.getId());
                    BigDecimal opening = ledger != null ? ledger.getOpeningBalance() : resolveOpeningBalance(account.getId(), period);
                    BigDecimal periodDebit = ledger != null ? ledger.getPeriodDebitTotal() : BigDecimal.ZERO;
                    BigDecimal periodCredit = ledger != null ? ledger.getPeriodCreditTotal() : BigDecimal.ZERO;
                    BigDecimal closing = ledger != null ? ledger.getClosingBalance() : opening;
                    return GeneralLedgerSummaryRow.builder()
                            .glAccountId(account.getId())
                            .accountNo(account.getAccountNo())
                            .accountName(account.getAccountName())
                            .openingBalance(opening)
                            .periodDebitTotal(periodDebit)
                            .periodCreditTotal(periodCredit)
                            .closingBalance(closing)
                            .build();
                })
                .sorted(Comparator.comparing(GeneralLedgerSummaryRow::getAccountNo))
                .toList();
    }

    @Override
    public DateRangeLedgerResponse getLedgerForDateRange(Long glAccountId, LocalDate dateFrom, LocalDate dateTo) {
        GlAccount account = glAccountRepository.findById(glAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("GL account", glAccountId));

        BigDecimal opening = BigDecimal.ZERO;
        if (dateFrom != null) {
            for (JournalEntryLine line : journalEntryLineRepository.findPostedLinesForAccountBeforeDate(glAccountId, dateFrom)) {
                opening = opening.add(signedAmount(line, account.getNormalBalance()));
            }
        }

        List<JournalEntryLine> rangeLines = journalEntryLineRepository.findPostedLinesForAccountAndDateRange(glAccountId, dateFrom, dateTo);
        List<LedgerLineResponse> lines = new ArrayList<>();
        BigDecimal runningBalance = opening;
        BigDecimal periodDebit = BigDecimal.ZERO;
        BigDecimal periodCredit = BigDecimal.ZERO;
        for (JournalEntryLine line : rangeLines) {
            runningBalance = runningBalance.add(signedAmount(line, account.getNormalBalance()));
            if (line.getEntrySide() == EntrySide.DEBIT) {
                periodDebit = periodDebit.add(line.getAmount());
            } else {
                periodCredit = periodCredit.add(line.getAmount());
            }
            lines.add(LedgerLineResponse.builder()
                    .entryNo(line.getJournalEntry().getEntryNo())
                    .transactionDate(line.getJournalEntry().getTransactionDate())
                    .description(line.getDescription())
                    .entrySide(line.getEntrySide())
                    .amount(line.getAmount())
                    .runningBalance(runningBalance)
                    .build());
        }

        return DateRangeLedgerResponse.builder()
                .glAccountId(account.getId())
                .accountNo(account.getAccountNo())
                .accountName(account.getAccountName())
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .openingBalance(opening)
                .periodDebitTotal(periodDebit)
                .periodCreditTotal(periodCredit)
                .closingBalance(runningBalance)
                .lines(lines)
                .build();
    }

    @Override
    public BranchLedgerResponse getBranchLedger(Long branchId, LocalDate dateFrom, LocalDate dateTo) {
        List<JournalEntryLine> lines = journalEntryLineRepository.findPostedLinesForBranchAndDateRange(branchId, dateFrom, dateTo);
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<BranchLedgerLineResponse> rows = new ArrayList<>();
        for (JournalEntryLine line : lines) {
            if (line.getEntrySide() == EntrySide.DEBIT) {
                totalDebit = totalDebit.add(line.getAmount());
            } else {
                totalCredit = totalCredit.add(line.getAmount());
            }
            rows.add(BranchLedgerLineResponse.builder()
                    .entryNo(line.getJournalEntry().getEntryNo())
                    .transactionDate(line.getJournalEntry().getTransactionDate())
                    .glAccountNo(line.getGlAccount().getAccountNo())
                    .glAccountName(line.getGlAccount().getAccountName())
                    .description(line.getDescription())
                    .entrySide(line.getEntrySide())
                    .amount(line.getAmount())
                    .build());
        }

        return BranchLedgerResponse.builder()
                .branchId(branchId)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .lines(rows)
                .build();
    }

    @Override
    @Transactional
    public void applyPostedEntry(JournalEntry entry) {
        for (JournalEntryLine line : entry.getLines()) {
            GlAccount account = line.getGlAccount();
            FinancialPeriod period = entry.getFinancialPeriod();
            GeneralLedger ledger = generalLedgerRepository.findByGlAccountIdAndFinancialPeriodId(account.getId(), period.getId())
                    .orElseGet(() -> newLedgerRow(account, period));
            if (line.getEntrySide() == EntrySide.DEBIT) {
                ledger.setPeriodDebitTotal(ledger.getPeriodDebitTotal().add(line.getAmount()));
            } else {
                ledger.setPeriodCreditTotal(ledger.getPeriodCreditTotal().add(line.getAmount()));
            }
            ledger.setClosingBalance(computeClosingBalance(ledger, account.getNormalBalance()));
            generalLedgerRepository.save(ledger);
        }
    }

    private GeneralLedger newLedgerRow(GlAccount account, FinancialPeriod period) {
        BigDecimal opening = resolveOpeningBalance(account.getId(), period);
        return GeneralLedger.builder()
                .glAccount(account)
                .financialPeriod(period)
                .openingBalance(opening)
                .periodDebitTotal(BigDecimal.ZERO)
                .periodCreditTotal(BigDecimal.ZERO)
                .closingBalance(opening)
                .build();
    }

    private BigDecimal resolveOpeningBalance(Long glAccountId, FinancialPeriod period) {
        return generalLedgerRepository
                .findFirstByGlAccountIdAndFinancialPeriod_EndDateLessThanOrderByFinancialPeriod_EndDateDesc(glAccountId, period.getStartDate())
                .map(GeneralLedger::getClosingBalance)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal computeClosingBalance(GeneralLedger ledger, EntrySide normalBalance) {
        BigDecimal net = normalBalance == EntrySide.DEBIT
                ? ledger.getPeriodDebitTotal().subtract(ledger.getPeriodCreditTotal())
                : ledger.getPeriodCreditTotal().subtract(ledger.getPeriodDebitTotal());
        return ledger.getOpeningBalance().add(net);
    }

    private BigDecimal signedAmount(JournalEntryLine line, EntrySide normalBalance) {
        return line.getEntrySide() == normalBalance ? line.getAmount() : line.getAmount().negate();
    }
}
