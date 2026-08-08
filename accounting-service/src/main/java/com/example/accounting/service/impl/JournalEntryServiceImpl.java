package com.example.accounting.service.impl;

import com.example.accounting.dto.JournalEntryGenerateRequest;
import com.example.accounting.dto.JournalEntryLineRequest;
import com.example.accounting.dto.JournalEntryLineResponse;
import com.example.accounting.dto.JournalEntryRequest;
import com.example.accounting.dto.JournalEntryResponse;
import com.example.accounting.entity.AccountingScheme;
import com.example.accounting.entity.AccountingSchemeStatus;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.FinancialPeriodStatus;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.entity.JournalAuditAction;
import com.example.accounting.entity.JournalEntry;
import com.example.accounting.entity.JournalEntryLine;
import com.example.accounting.entity.JournalEntryStatus;
import com.example.accounting.entity.JournalTemplate;
import com.example.accounting.entity.JournalTemplateStatus;
import com.example.accounting.exception.AppException;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.AccountingSchemeRepository;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.repository.JournalEntryRepository;
import com.example.accounting.repository.JournalTemplateRepository;
import com.example.accounting.service.GeneralLedgerService;
import com.example.accounting.service.JournalAuditLogService;
import com.example.accounting.service.JournalEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JournalEntryServiceImpl implements JournalEntryService {

    // Every AccountingScheme configured today is USD — the generate() endpoint exists so
    // upstream services never have to resolve account roles themselves, so it isn't given
    // a currency to pass through either. Revisit if a non-USD scheme is ever added.
    private static final String GENERATED_CURRENCY = "USD";

    private final JournalEntryRepository journalEntryRepository;
    private final FinancialPeriodRepository financialPeriodRepository;
    private final GlAccountRepository glAccountRepository;
    private final JournalTemplateRepository journalTemplateRepository;
    private final AccountingSchemeRepository accountingSchemeRepository;
    private final GeneralLedgerService generalLedgerService;
    private final JournalAuditLogService journalAuditLogService;

    @Override
    @Transactional
    public JournalEntryResponse create(JournalEntryRequest request) {
        FinancialPeriod period = financialPeriodRepository.findByDateWithinRange(request.getTransactionDate())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "No financial period covers transaction date " + request.getTransactionDate()));
        if (period.getStatus() != FinancialPeriodStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Financial period " + period.getPeriodName() + " is closed");
        }

        JournalEntry entry = JournalEntry.builder()
                .transactionType(request.getTransactionType())
                .transactionDate(request.getTransactionDate())
                .branchId(request.getBranchId())
                .financialPeriod(period)
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .status(JournalEntryStatus.DRAFT)
                .build();
        applyLines(entry, request.getLines());
        assertBalanced(entry);

        entry = journalEntryRepository.save(entry);
        entry.setEntryNo(generateEntryNo(entry.getId()));
        entry = journalEntryRepository.save(entry);
        journalAuditLogService.record(entry, JournalAuditAction.CREATED,
                "Created as draft with " + entry.getLines().size() + " lines");
        return toResponse(entry);
    }

    @Override
    @Transactional
    public JournalEntryResponse generate(JournalEntryGenerateRequest request) {
        Optional<JournalEntry> existing = journalEntryRepository.findByTransactionTypeAndReferenceTypeAndReferenceId(
                request.getTransactionType(), request.getReferenceType(), request.getReferenceId());
        if (existing.isPresent()) {
            // A retried Feign call (timeout, network blip) for the same business event —
            // return what's already posted instead of duplicating it.
            return toResponse(existing.get());
        }

        List<JournalTemplate> templates = journalTemplateRepository
                .findByTransactionType(request.getTransactionType())
                .stream()
                .filter(t -> t.getStatus() == JournalTemplateStatus.ACTIVE)
                .toList();
        if (templates.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "No active journal template configured for " + request.getTransactionType());
        }
        if (templates.size() > 1) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Multiple active journal templates configured for " + request.getTransactionType()
                            + " — cannot pick one automatically");
        }
        JournalTemplate template = templates.get(0);

        List<JournalEntryLineRequest> lines = new ArrayList<>();
        for (var templateLine : template.getLines()) {
            AccountingScheme scheme = accountingSchemeRepository
                    .findByJournalTemplateIdAndAccountRoleAndCurrency(
                            template.getId(), templateLine.getAccountRole(), GENERATED_CURRENCY)
                    .filter(s -> s.getStatus() == AccountingSchemeStatus.ACTIVE)
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                            "No active accounting scheme binds role " + templateLine.getAccountRole()
                                    + " on template " + template.getCode() + " to a " + GENERATED_CURRENCY
                                    + " GL account"));

            JournalEntryLineRequest line = new JournalEntryLineRequest();
            line.setLineNo(templateLine.getLineNo());
            line.setGlAccountId(scheme.getGlAccount().getId());
            line.setEntrySide(templateLine.getEntrySide());
            line.setAmount(request.getAmount());
            line.setDescription(templateLine.getDescription());
            lines.add(line);
        }

        JournalEntryRequest createRequest = new JournalEntryRequest();
        createRequest.setTransactionType(request.getTransactionType());
        createRequest.setTransactionDate(request.getTransactionDate());
        createRequest.setBranchId(request.getBranchId());
        createRequest.setReferenceType(request.getReferenceType());
        createRequest.setReferenceId(request.getReferenceId());
        createRequest.setCurrency(GENERATED_CURRENCY);
        createRequest.setDescription(
                request.getDescription() != null ? request.getDescription() : template.getName());
        createRequest.setLines(lines);

        JournalEntryResponse draft = create(createRequest);
        return post(draft.getId());
    }

    @Override
    public JournalEntryResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<JournalEntryResponse> getAll(Long financialPeriodId) {
        List<JournalEntry> entries = financialPeriodId != null
                ? journalEntryRepository.findByFinancialPeriodId(financialPeriodId)
                : journalEntryRepository.findAll();
        return entries.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public JournalEntryResponse post(Long id) {
        JournalEntry entry = findOrThrow(id);
        if (entry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only DRAFT journal entries can be posted");
        }
        if (entry.getFinancialPeriod().getStatus() != FinancialPeriodStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Financial period " + entry.getFinancialPeriod().getPeriodName() + " is closed");
        }
        assertBalanced(entry);
        entry.setStatus(JournalEntryStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());
        entry.setPostedBy(currentUsername());
        entry = journalEntryRepository.save(entry);
        generalLedgerService.applyPostedEntry(entry);
        journalAuditLogService.record(entry, JournalAuditAction.POSTED, "Posted to " + entry.getFinancialPeriod().getPeriodName());
        return toResponse(entry);
    }

    @Override
    @Transactional
    public JournalEntryResponse reverse(Long id) {
        JournalEntry original = findOrThrow(id);
        if (original.getStatus() != JournalEntryStatus.POSTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only POSTED journal entries can be reversed");
        }
        if (original.getFinancialPeriod().getStatus() != FinancialPeriodStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Financial period " + original.getFinancialPeriod().getPeriodName() + " is closed");
        }

        String username = currentUsername();
        final JournalEntry reversalDraft = JournalEntry.builder()
                .transactionType(original.getTransactionType())
                .transactionDate(original.getTransactionDate())
                .branchId(original.getBranchId())
                .financialPeriod(original.getFinancialPeriod())
                .referenceType("JOURNAL_ENTRY_REVERSAL")
                .referenceId(original.getEntryNo())
                .currency(original.getCurrency())
                .description("Reversal of " + original.getEntryNo())
                .status(JournalEntryStatus.POSTED)
                .postedAt(LocalDateTime.now())
                .postedBy(username)
                .build();
        original.getLines().forEach(line -> reversalDraft.getLines().add(JournalEntryLine.builder()
                .journalEntry(reversalDraft)
                .lineNo(line.getLineNo())
                .glAccount(line.getGlAccount())
                .entrySide(line.getEntrySide() == EntrySide.DEBIT ? EntrySide.CREDIT : EntrySide.DEBIT)
                .amount(line.getAmount())
                .description("Reversal of " + original.getEntryNo())
                .build()));

        JournalEntry reversal = journalEntryRepository.save(reversalDraft);
        reversal.setEntryNo(generateEntryNo(reversal.getId()));
        reversal = journalEntryRepository.save(reversal);
        generalLedgerService.applyPostedEntry(reversal);

        original.setStatus(JournalEntryStatus.REVERSED);
        journalEntryRepository.save(original);

        journalAuditLogService.record(original, JournalAuditAction.REVERSED, "Reversed by " + reversal.getEntryNo());
        journalAuditLogService.record(reversal, JournalAuditAction.POSTED, "Reversal of " + original.getEntryNo());

        return toResponse(reversal);
    }

    private void applyLines(JournalEntry entry, List<JournalEntryLineRequest> lineRequests) {
        lineRequests.stream()
                .map(lr -> JournalEntryLine.builder()
                        .journalEntry(entry)
                        .lineNo(lr.getLineNo())
                        .glAccount(findPostableAccountOrThrow(lr.getGlAccountId()))
                        .entrySide(lr.getEntrySide())
                        .amount(lr.getAmount())
                        .description(lr.getDescription())
                        .build())
                .forEach(entry.getLines()::add);
    }

    private void assertBalanced(JournalEntry entry) {
        BigDecimal debitTotal = sumBySide(entry, EntrySide.DEBIT);
        BigDecimal creditTotal = sumBySide(entry, EntrySide.CREDIT);
        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Journal entry is not balanced: debit=" + debitTotal + " credit=" + creditTotal);
        }
    }

    private BigDecimal sumBySide(JournalEntry entry, EntrySide side) {
        return entry.getLines().stream()
                .filter(l -> l.getEntrySide() == side)
                .map(JournalEntryLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateEntryNo(Long id) {
        return "JE-" + String.format("%08d", id);
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private GlAccount findPostableAccountOrThrow(Long glAccountId) {
        GlAccount account = glAccountRepository.findById(glAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("GL account", glAccountId));
        if (!account.isAllowPosting()) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "GL account " + account.getAccountNo() + " does not allow direct posting");
        }
        return account;
    }

    private JournalEntry findOrThrow(Long id) {
        return journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry", id));
    }

    private JournalEntryResponse toResponse(JournalEntry entry) {
        List<JournalEntryLineResponse> lines = entry.getLines().stream()
                .map(l -> JournalEntryLineResponse.builder()
                        .id(l.getId())
                        .lineNo(l.getLineNo())
                        .glAccountId(l.getGlAccount().getId())
                        .glAccountNo(l.getGlAccount().getAccountNo())
                        .entrySide(l.getEntrySide())
                        .amount(l.getAmount())
                        .description(l.getDescription())
                        .build())
                .toList();
        return JournalEntryResponse.builder()
                .id(entry.getId())
                .entryNo(entry.getEntryNo())
                .transactionType(entry.getTransactionType())
                .transactionDate(entry.getTransactionDate())
                .branchId(entry.getBranchId())
                .financialPeriodId(entry.getFinancialPeriod().getId())
                .financialPeriodName(entry.getFinancialPeriod().getPeriodName())
                .referenceType(entry.getReferenceType())
                .referenceId(entry.getReferenceId())
                .currency(entry.getCurrency())
                .description(entry.getDescription())
                .status(entry.getStatus())
                .postedAt(entry.getPostedAt())
                .postedBy(entry.getPostedBy())
                .lines(lines)
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
