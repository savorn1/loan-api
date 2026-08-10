package com.example.payment.service.impl;

import com.example.payment.client.LoanClient;
import com.example.payment.dto.CollectionBucket;
import com.example.payment.dto.CollectionTrendPointResponse;
import com.example.payment.dto.CollectorProductivityRow;
import com.example.payment.dto.LargeTransactionRow;
import com.example.payment.dto.ParBucketSummary;
import com.example.payment.dto.ParSummaryResponse;
import com.example.payment.dto.PortfolioSummaryResponse;
import com.example.payment.dto.ProvisioningStageRow;
import com.example.payment.dto.ProvisioningSummaryResponse;
import com.example.payment.entity.CollectionCase;
import com.example.payment.entity.CollectionCaseStatus;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.CollectionCaseRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PaymentRepository paymentRepository;
    private final CollectionCaseRepository collectionCaseRepository;
    private final LoanClient loanClient;

    private record LoanOverdue(CollectionBucket bucket, BigDecimal amount) {}

    // Unlike CollectionServiceImpl.getWorkqueue, this doesn't resolve loan/customer
    // details per loan — a bucket summary only needs the DPD bucket and overdue
    // amount, so it's a single query plus one Feign call total, not N+1.
    @Override
    public ParSummaryResponse getParSummary() {
        LocalDate today = LocalDate.now();
        Map<Long, List<Payment>> overdueByLoan = paymentRepository.findByStatus(PaymentStatus.OVERDUE).stream()
                .collect(Collectors.groupingBy(Payment::getLoanId));

        List<LoanOverdue> perLoan = overdueByLoan.values().stream()
                .map(payments -> {
                    long maxDpd = payments.stream()
                            .mapToLong(p -> ChronoUnit.DAYS.between(p.getDueDate(), today))
                            .max().orElse(0);
                    BigDecimal total = payments.stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new LoanOverdue(CollectionBucket.classify(maxDpd), total);
                })
                .toList();

        Map<CollectionBucket, List<LoanOverdue>> byBucket = perLoan.stream()
                .collect(Collectors.groupingBy(LoanOverdue::bucket));

        List<ParBucketSummary> buckets = Arrays.stream(CollectionBucket.values())
                .map(bucket -> {
                    List<LoanOverdue> inBucket = byBucket.getOrDefault(bucket, List.of());
                    BigDecimal amount = inBucket.stream()
                            .map(LoanOverdue::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return ParBucketSummary.builder()
                            .bucket(bucket)
                            .loanCount(inBucket.size())
                            .overdueAmount(amount)
                            .build();
                })
                .toList();

        BigDecimal totalOverdue = buckets.stream()
                .map(ParBucketSummary::getOverdueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PortfolioSummaryResponse portfolio = loanClient.getPortfolioSummary().getData();
        BigDecimal totalOutstanding = portfolio != null && portfolio.getTotalOutstandingBalance() != null
                ? portfolio.getTotalOutstandingBalance() : BigDecimal.ZERO;
        long activeLoanCount = portfolio != null ? portfolio.getActiveLoanCount() : 0;

        BigDecimal parPercent = totalOutstanding.compareTo(BigDecimal.ZERO) > 0
                ? totalOverdue.multiply(BigDecimal.valueOf(100)).divide(totalOutstanding, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ParSummaryResponse.builder()
                .buckets(buckets)
                .totalOverdueAmount(totalOverdue)
                .totalOutstandingBalance(totalOutstanding)
                .activeLoanCount(activeLoanCount)
                .portfolioAtRiskPercent(parPercent)
                .build();
    }

    @Override
    public List<CollectionTrendPointResponse> getCollectionTrend(int months) {
        LocalDate since = LocalDate.now().minusMonths(months);
        return paymentRepository.aggregateCollectionTrend(since);
    }

    // Simplified IFRS 9-style ECL staging: buckets loans by DPD (the same classification
    // getParSummary uses) into three stages, then applies a flat provision rate per
    // stage. This is an approximation for reporting purposes, not a regulator-certified
    // ECL model — a real model would weight by product/collateral/macro factors and use
    // each loan's actual outstanding balance rather than its overdue installment amount
    // as the provisioning base for Stage 2/3 (payment-service doesn't have loan balances;
    // only loan-service does, via the same portfolio Feign call getParSummary already uses).
    @Override
    public ProvisioningSummaryResponse getProvisioningSummary() {
        LocalDate today = LocalDate.now();
        Map<Long, List<Payment>> overdueByLoan = paymentRepository.findByStatus(PaymentStatus.OVERDUE).stream()
                .collect(Collectors.groupingBy(Payment::getLoanId));

        List<LoanOverdue> perLoan = overdueByLoan.values().stream()
                .map(payments -> {
                    long maxDpd = payments.stream()
                            .mapToLong(p -> ChronoUnit.DAYS.between(p.getDueDate(), today))
                            .max().orElse(0);
                    BigDecimal total = payments.stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new LoanOverdue(CollectionBucket.classify(maxDpd), total);
                })
                .toList();

        BigDecimal stage2Base = sumForBuckets(perLoan, CollectionBucket.DPD_1_30, CollectionBucket.DPD_31_60, CollectionBucket.DPD_61_90);
        long stage2Count = countForBuckets(perLoan, CollectionBucket.DPD_1_30, CollectionBucket.DPD_31_60, CollectionBucket.DPD_61_90);
        BigDecimal stage3Base = sumForBuckets(perLoan, CollectionBucket.DPD_90_PLUS);
        long stage3Count = countForBuckets(perLoan, CollectionBucket.DPD_90_PLUS);

        PortfolioSummaryResponse portfolio = loanClient.getPortfolioSummary().getData();
        BigDecimal totalOutstanding = portfolio != null && portfolio.getTotalOutstandingBalance() != null
                ? portfolio.getTotalOutstandingBalance() : BigDecimal.ZERO;
        long activeLoanCount = portfolio != null ? portfolio.getActiveLoanCount() : 0;

        BigDecimal stage1Base = totalOutstanding.subtract(stage2Base).subtract(stage3Base);
        if (stage1Base.compareTo(BigDecimal.ZERO) < 0) stage1Base = BigDecimal.ZERO;
        long stage1Count = Math.max(activeLoanCount - stage2Count - stage3Count, 0);

        ProvisioningStageRow stage1 = provisioningRow("STAGE_1", "Performing (12-month ECL)", stage1Count, stage1Base, BigDecimal.valueOf(1));
        ProvisioningStageRow stage2 = provisioningRow("STAGE_2", "Underperforming (DPD 1-90, lifetime ECL)", stage2Count, stage2Base, BigDecimal.valueOf(20));
        ProvisioningStageRow stage3 = provisioningRow("STAGE_3", "Credit-impaired (DPD 90+)", stage3Count, stage3Base, BigDecimal.valueOf(100));

        BigDecimal totalProvision = stage1.getProvisionAmount().add(stage2.getProvisionAmount()).add(stage3.getProvisionAmount());

        return ProvisioningSummaryResponse.builder()
                .stages(List.of(stage1, stage2, stage3))
                .totalOutstandingBalance(totalOutstanding)
                .totalProvisionAmount(totalProvision)
                .build();
    }

    private BigDecimal sumForBuckets(List<LoanOverdue> perLoan, CollectionBucket... buckets) {
        List<CollectionBucket> set = List.of(buckets);
        return perLoan.stream()
                .filter(lo -> set.contains(lo.bucket()))
                .map(LoanOverdue::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countForBuckets(List<LoanOverdue> perLoan, CollectionBucket... buckets) {
        List<CollectionBucket> set = List.of(buckets);
        return perLoan.stream().filter(lo -> set.contains(lo.bucket())).count();
    }

    private ProvisioningStageRow provisioningRow(String stage, String label, long loanCount, BigDecimal base, BigDecimal ratePercent) {
        BigDecimal amount = base.multiply(ratePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return ProvisioningStageRow.builder()
                .stage(stage)
                .label(label)
                .loanCount(loanCount)
                .outstandingBalance(base)
                .provisionRatePercent(ratePercent)
                .provisionAmount(amount)
                .build();
    }

    @Override
    public List<LargeTransactionRow> getLargeTransactions(BigDecimal threshold, int months) {
        LocalDate since = LocalDate.now().minusMonths(months);
        return paymentRepository
                .findByStatusAndAmountGreaterThanEqualAndPaidAtGreaterThanEqualOrderByAmountDesc(PaymentStatus.PAID, threshold, since)
                .stream()
                .map(p -> LargeTransactionRow.builder()
                        .loanId(p.getLoanId())
                        .amount(p.getAmount())
                        .paidAt(p.getPaidAt())
                        .installmentNumber(p.getInstallmentNumber())
                        .build())
                .toList();
    }

    // Attributes each collected payment to whichever collector's case owns that loan
    // (CollectionCase.loanId -> assignedToUserId), then aggregates per collector — a
    // loan with no case yet (never overdue, or a case never opened) simply doesn't
    // contribute to any collector's total, same as the workqueue's "unassigned" handling.
    @Override
    public List<CollectorProductivityRow> getCollectorProductivity(int months) {
        LocalDate since = LocalDate.now().minusMonths(months);

        List<CollectionCase> cases = collectionCaseRepository.findAll();
        Map<Long, Long> assigneeByLoanId = cases.stream()
                .filter(c -> c.getAssignedToUserId() != null)
                .collect(Collectors.toMap(CollectionCase::getLoanId, CollectionCase::getAssignedToUserId, (a, b) -> a));

        List<Payment> paidPayments = paymentRepository.findByStatusAndPaidAtGreaterThanEqual(PaymentStatus.PAID, since);
        Map<Long, List<Payment>> paidByCollector = paidPayments.stream()
                .filter(p -> assigneeByLoanId.containsKey(p.getLoanId()))
                .collect(Collectors.groupingBy(p -> assigneeByLoanId.get(p.getLoanId())));

        Map<Long, List<CollectionCase>> casesByCollector = cases.stream()
                .filter(c -> c.getAssignedToUserId() != null)
                .collect(Collectors.groupingBy(CollectionCase::getAssignedToUserId));

        return casesByCollector.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<CollectionCase> userCases = entry.getValue();
                    List<Payment> userPayments = paidByCollector.getOrDefault(userId, List.of());
                    long openCount = userCases.stream()
                            .filter(c -> c.getStatus() == CollectionCaseStatus.OPEN || c.getStatus() == CollectionCaseStatus.IN_PROGRESS)
                            .count();
                    long resolvedCount = userCases.stream().filter(c -> c.getStatus() == CollectionCaseStatus.RESOLVED).count();
                    BigDecimal totalCollected = userPayments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return CollectorProductivityRow.builder()
                            .assignedToUserId(userId)
                            .caseCount(userCases.size())
                            .openCaseCount(openCount)
                            .resolvedCaseCount(resolvedCount)
                            .paymentCount(userPayments.size())
                            .totalCollected(totalCollected)
                            .build();
                })
                .sorted((a, b) -> b.getTotalCollected().compareTo(a.getTotalCollected()))
                .toList();
    }
}
