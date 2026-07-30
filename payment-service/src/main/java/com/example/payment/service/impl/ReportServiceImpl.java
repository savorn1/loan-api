package com.example.payment.service.impl;

import com.example.payment.client.LoanClient;
import com.example.payment.dto.CollectionBucket;
import com.example.payment.dto.CollectionTrendPointResponse;
import com.example.payment.dto.ParBucketSummary;
import com.example.payment.dto.ParSummaryResponse;
import com.example.payment.dto.PortfolioSummaryResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
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
}
