package com.example.loan.service.impl;

import com.example.loan.dto.ApplicationStatusCount;
import com.example.loan.dto.ApprovalFunnelResponse;
import com.example.loan.dto.BorrowerConcentrationRow;
import com.example.loan.dto.BranchConcentrationRow;
import com.example.loan.dto.BranchOutstandingRow;
import com.example.loan.dto.CohortStatusRow;
import com.example.loan.dto.ConcentrationRiskResponse;
import com.example.loan.dto.CustomerOutstandingRow;
import com.example.loan.dto.DataQualityExceptionRow;
import com.example.loan.dto.DisbursementTrendPointResponse;
import com.example.loan.dto.LoanPricingRow;
import com.example.loan.dto.LoanStatusBreakdownResponse;
import com.example.loan.dto.PortfolioSummaryResponse;
import com.example.loan.dto.PricingBandRow;
import com.example.loan.dto.PricingSummaryResponse;
import com.example.loan.dto.StatusAuditEntryResponse;
import com.example.loan.dto.VintageCohortResponse;
import com.example.loan.entity.Application;
import com.example.loan.entity.ApplicationStatus;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanStatus;
import com.example.loan.entity.LoanStatusHistory;
import com.example.loan.repository.ApplicationRepository;
import com.example.loan.repository.LoanRepository;
import com.example.loan.repository.LoanStatusHistoryRepository;
import com.example.loan.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final LoanRepository loanRepository;
    private final ApplicationRepository applicationRepository;
    private final LoanStatusHistoryRepository loanStatusHistoryRepository;

    @Override
    public PortfolioSummaryResponse getPortfolioSummary() {
        PortfolioSummaryResponse summary = loanRepository.aggregatePortfolioSummary();
        // sum(...) comes back null (not zero) when there are no ACTIVE loans —
        // normalize so callers never have to null-check the totals.
        if (summary.getTotalPrincipal() == null) summary.setTotalPrincipal(BigDecimal.ZERO);
        if (summary.getTotalOutstandingBalance() == null) summary.setTotalOutstandingBalance(BigDecimal.ZERO);
        return summary;
    }

    @Override
    public List<DisbursementTrendPointResponse> getDisbursementTrend(int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        return loanRepository.aggregateDisbursementTrend(since);
    }

    @Override
    public List<LoanStatusBreakdownResponse> getStatusBreakdown() {
        return loanRepository.aggregateStatusBreakdown();
    }

    // Pivots the (cohortMonth, status) rows the repository returns into one row per
    // month with ACTIVE/CLOSED broken out as separate columns — a vintage table reads
    // left-to-right by cohort, not grouped by status.
    @Override
    public List<VintageCohortResponse> getVintageAnalysis(int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        List<CohortStatusRow> rows = loanRepository.aggregateVintageCohorts(since);

        Map<String, List<CohortStatusRow>> byMonth = rows.stream()
                .collect(Collectors.groupingBy(CohortStatusRow::getCohortMonth, LinkedHashMap::new, Collectors.toList()));

        return byMonth.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<CohortStatusRow> monthRows = entry.getValue();
                    long totalCount = monthRows.stream().mapToLong(CohortStatusRow::getLoanCount).sum();
                    BigDecimal totalPrincipal = monthRows.stream()
                            .map(CohortStatusRow::getTotalPrincipal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long activeCount = sumForStatus(monthRows, LoanStatus.ACTIVE, CohortStatusRow::getLoanCount).longValue();
                    BigDecimal activePrincipal = sumPrincipalForStatus(monthRows, LoanStatus.ACTIVE);
                    long closedCount = sumForStatus(monthRows, LoanStatus.CLOSED, CohortStatusRow::getLoanCount).longValue();
                    BigDecimal closedPrincipal = sumPrincipalForStatus(monthRows, LoanStatus.CLOSED);
                    return VintageCohortResponse.builder()
                            .cohortMonth(month)
                            .loanCount(totalCount)
                            .totalPrincipal(totalPrincipal)
                            .activeCount(activeCount)
                            .activePrincipal(activePrincipal)
                            .closedCount(closedCount)
                            .closedPrincipal(closedPrincipal)
                            .build();
                })
                .toList();
    }

    private Number sumForStatus(List<CohortStatusRow> rows, LoanStatus status, java.util.function.ToLongFunction<CohortStatusRow> extractor) {
        return rows.stream().filter(r -> r.getStatus() == status).mapToLong(extractor).sum();
    }

    private BigDecimal sumPrincipalForStatus(List<CohortStatusRow> rows, LoanStatus status) {
        return rows.stream()
                .filter(r -> r.getStatus() == status)
                .map(CohortStatusRow::getTotalPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public ConcentrationRiskResponse getConcentrationRisk(int topN) {
        BigDecimal totalOutstanding = getPortfolioSummary().getTotalOutstandingBalance();

        List<BranchConcentrationRow> byBranch = loanRepository.aggregateOutstandingByBranch().stream()
                .map(row -> BranchConcentrationRow.builder()
                        .branchId(row.getBranchId())
                        .loanCount(row.getLoanCount())
                        .outstandingBalance(row.getOutstandingBalance())
                        .percentOfPortfolio(percentOf(row.getOutstandingBalance(), totalOutstanding))
                        .build())
                .toList();

        List<BorrowerConcentrationRow> topBorrowers = loanRepository.aggregateOutstandingByCustomer().stream()
                .limit(Math.max(topN, 0))
                .map(row -> BorrowerConcentrationRow.builder()
                        .customerId(row.getCustomerId())
                        .loanCount(row.getLoanCount())
                        .outstandingBalance(row.getOutstandingBalance())
                        .percentOfPortfolio(percentOf(row.getOutstandingBalance(), totalOutstanding))
                        .build())
                .toList();

        return ConcentrationRiskResponse.builder()
                .totalOutstandingBalance(totalOutstanding)
                .byBranch(byBranch)
                .topBorrowers(topBorrowers)
                .build();
    }

    private BigDecimal percentOf(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 2, RoundingMode.HALF_UP);
    }

    // Fetches the window's applications once and does all grouping/averaging in Java —
    // same approach payment-service's ReportServiceImpl.getParSummary takes for
    // per-loan classification, since there's no rejection-reason column to aggregate
    // in SQL and the decision-time average needs row-by-row date math anyway.
    @Override
    public ApprovalFunnelResponse getApprovalFunnel(int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        List<Application> applications = applicationRepository.findBySubmittedAtAfter(since);

        Map<ApplicationStatus, Long> countByStatus = applications.stream()
                .collect(Collectors.groupingBy(Application::getStatus, Collectors.counting()));

        List<ApplicationStatusCount> breakdown = Arrays.stream(ApplicationStatus.values())
                .map(status -> ApplicationStatusCount.builder()
                        .status(status)
                        .count(countByStatus.getOrDefault(status, 0L))
                        .build())
                .toList();

        long totalSubmitted = applications.size();
        long totalApproved = countByStatus.getOrDefault(ApplicationStatus.APPROVED, 0L);
        long totalRejected = countByStatus.getOrDefault(ApplicationStatus.REJECTED, 0L);

        BigDecimal approvalRate = totalSubmitted > 0
                ? BigDecimal.valueOf(totalApproved).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalSubmitted), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Long> decisionDays = applications.stream()
                .filter(a -> a.getDecidedAt() != null)
                .map(a -> ChronoUnit.DAYS.between(a.getSubmittedAt(), a.getDecidedAt()))
                .toList();
        BigDecimal avgDecisionDays = decisionDays.isEmpty()
                ? null
                : BigDecimal.valueOf(decisionDays.stream().mapToLong(Long::longValue).average().orElse(0))
                        .setScale(1, RoundingMode.HALF_UP);

        return ApprovalFunnelResponse.builder()
                .totalSubmitted(totalSubmitted)
                .totalApproved(totalApproved)
                .totalRejected(totalRejected)
                .approvalRatePercent(approvalRate)
                .avgDecisionDays(avgDecisionDays)
                .statusBreakdown(breakdown)
                .build();
    }

    @Override
    public List<StatusAuditEntryResponse> getStatusAuditTrail(LocalDate dateFrom, LocalDate dateTo, LoanStatus status, int limit) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        List<LoanStatusHistory> history = loanStatusHistoryRepository.findAuditTrail(from, to, status);

        return history.stream()
                .limit(Math.max(limit, 0))
                .map(h -> StatusAuditEntryResponse.builder()
                        .loanId(h.getLoan().getId())
                        .loanNo(h.getLoan().getLoanNo())
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .note(h.getNote())
                        .changedBy(h.getChangedBy())
                        .changedAt(h.getChangedAt())
                        .build())
                .toList();
    }

    private static final BigDecimal RATE_BAND_1 = BigDecimal.valueOf(10);
    private static final BigDecimal RATE_BAND_2 = BigDecimal.valueOf(15);
    private static final BigDecimal RATE_BAND_3 = BigDecimal.valueOf(20);

    @Override
    public PricingSummaryResponse getPricingSummary() {
        List<LoanPricingRow> loans = loanRepository.findPricingProjection();

        Map<String, List<LoanPricingRow>> byRateBand = loans.stream()
                .collect(Collectors.groupingBy(l -> rateBandOf(l.getInterestRate()), LinkedHashMap::new, Collectors.toList()));
        Map<String, List<LoanPricingRow>> byTermBand = loans.stream()
                .collect(Collectors.groupingBy(l -> termBandOf(l.getTermMonths()), LinkedHashMap::new, Collectors.toList()));

        return PricingSummaryResponse.builder()
                .byRateBand(RATE_BAND_ORDER.stream()
                        .filter(byRateBand::containsKey)
                        .map(band -> toPricingBandRow(band, byRateBand.get(band)))
                        .toList())
                .byTermBand(TERM_BAND_ORDER.stream()
                        .filter(byTermBand::containsKey)
                        .map(band -> toPricingBandRow(band, byTermBand.get(band)))
                        .toList())
                .build();
    }

    private static final List<String> RATE_BAND_ORDER = List.of("< 10%", "10% - 15%", "15% - 20%", "20%+");
    private static final List<String> TERM_BAND_ORDER = List.of("<= 6 months", "7 - 12 months", "13 - 24 months", "25+ months");

    private String rateBandOf(BigDecimal rate) {
        if (rate.compareTo(RATE_BAND_1) < 0) return "< 10%";
        if (rate.compareTo(RATE_BAND_2) < 0) return "10% - 15%";
        if (rate.compareTo(RATE_BAND_3) < 0) return "15% - 20%";
        return "20%+";
    }

    private String termBandOf(Integer termMonths) {
        if (termMonths <= 6) return "<= 6 months";
        if (termMonths <= 12) return "7 - 12 months";
        if (termMonths <= 24) return "13 - 24 months";
        return "25+ months";
    }

    private PricingBandRow toPricingBandRow(String band, List<LoanPricingRow> rows) {
        BigDecimal totalPrincipal = rows.stream().map(LoanPricingRow::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgRate = rows.stream().map(LoanPricingRow::getInterestRate).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);
        return PricingBandRow.builder()
                .band(band)
                .loanCount(rows.size())
                .totalPrincipal(totalPrincipal)
                .avgInterestRate(avgRate)
                .build();
    }

    // Full-table scans over Loan/Application — acceptable at this portfolio scale (same
    // assumption findPricingProjection already makes); a large-scale version would need
    // to push each rule down into its own repository query instead.
    @Override
    public List<DataQualityExceptionRow> getDataQualityExceptions(int staleDays) {
        LocalDateTime now = LocalDateTime.now();
        List<DataQualityExceptionRow> exceptions = new java.util.ArrayList<>();

        for (Loan loan : loanRepository.findAll()) {
            String identifier = loan.getLoanNo() != null ? loan.getLoanNo() : "#" + loan.getId();

            if (loan.getLoanNo() == null) {
                exceptions.add(loanException(loan, identifier, "MISSING_LOAN_NUMBER",
                        "Loan has no loan number assigned"));
            }
            if (loan.getBranchId() == null) {
                exceptions.add(loanException(loan, identifier, "MISSING_BRANCH",
                        "Loan has no branch assigned"));
            }
            if ((loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.CLOSED)
                    && loan.getDisbursedAt() == null) {
                exceptions.add(loanException(loan, identifier, "ACTIVE_WITHOUT_DISBURSEMENT_DATE",
                        "Loan is " + loan.getStatus() + " but has no disbursement date"));
            }
            if (loan.getStatus() == LoanStatus.ACTIVE && loan.getOutstandingBalance() == null) {
                exceptions.add(loanException(loan, identifier, "ACTIVE_WITHOUT_BALANCE",
                        "Active loan has no outstanding balance recorded"));
            }
            if ((loan.getStatus() == LoanStatus.PENDING || loan.getStatus() == LoanStatus.APPROVED)
                    && loan.getCreatedAt() != null
                    && ChronoUnit.DAYS.between(loan.getCreatedAt(), now) > staleDays) {
                exceptions.add(loanException(loan, identifier, "STALE_LOAN_NOT_PROGRESSING",
                        "Loan has been " + loan.getStatus() + " for more than " + staleDays + " days"));
            }
        }

        for (Application application : applicationRepository.findAll()) {
            String identifier = application.getApplicationNo() != null
                    ? application.getApplicationNo() : "#" + application.getId();

            if (application.getApplicationNo() == null) {
                exceptions.add(applicationException(application, identifier, "MISSING_APPLICATION_NUMBER",
                        "Application has no application number assigned"));
            }
            if ((application.getStatus() == ApplicationStatus.APPROVED || application.getStatus() == ApplicationStatus.REJECTED)
                    && application.getDecidedAt() == null) {
                exceptions.add(applicationException(application, identifier, "DECIDED_WITHOUT_TIMESTAMP",
                        "Application is " + application.getStatus() + " but has no decision timestamp"));
            }
            if ((application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.UNDER_REVIEW)
                    && application.getSubmittedAt() != null
                    && ChronoUnit.DAYS.between(application.getSubmittedAt(), now) > staleDays) {
                exceptions.add(applicationException(application, identifier, "STALE_APPLICATION_NOT_DECIDED",
                        "Application has been " + application.getStatus() + " for more than " + staleDays + " days"));
            }
        }

        return exceptions.stream()
                .sorted(java.util.Comparator.comparing(DataQualityExceptionRow::getRecordCreatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed())
                .toList();
    }

    private DataQualityExceptionRow loanException(Loan loan, String identifier, String issueType, String description) {
        return DataQualityExceptionRow.builder()
                .entityType("LOAN")
                .entityId(loan.getId())
                .identifier(identifier)
                .issueType(issueType)
                .description(description)
                .recordCreatedAt(loan.getCreatedAt())
                .build();
    }

    private DataQualityExceptionRow applicationException(Application application, String identifier, String issueType, String description) {
        return DataQualityExceptionRow.builder()
                .entityType("APPLICATION")
                .entityId(application.getId())
                .identifier(identifier)
                .issueType(issueType)
                .description(description)
                .recordCreatedAt(application.getCreatedAt())
                .build();
    }
}
