package com.example.loan.service.impl;

import com.example.loan.client.CustomerClient;
import com.example.loan.client.LoanProductClient;
import com.example.loan.dto.CustomerEmploymentResponse;
import com.example.loan.dto.CustomerIncomeResponse;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.EligibilityCheckResponse;
import com.example.loan.dto.EligibilityResult;
import com.example.loan.dto.LoanProductRuleResponse;
import com.example.loan.dto.RuleField;
import com.example.loan.dto.RuleOperator;
import com.example.loan.entity.LoanStatus;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.EligibilityService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Purely advisory: never blocks application creation or approval, and never throws
// for a rule it can't evaluate. Only three of the six RuleField values have real
// source data anywhere in the system today — CREDIT_SCORE and DEBT_TO_INCOME_RATIO
// don't exist on Customer or anywhere else, so those rules are always
// NOT_EVALUABLE rather than silently passing or guessing.
@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityServiceImpl implements EligibilityService {

    private static final Set<LoanStatus> COUNTS_AS_EXISTING = Set.of(
            LoanStatus.PENDING, LoanStatus.APPROVED, LoanStatus.ACTIVE);

    private final LoanProductClient loanProductClient;
    private final CustomerClient customerClient;
    private final LoanRepository loanRepository;

    // Trips on repeated failures/timeouts from customer-service or loan-product-service
    // (config: application.properties) instead of letting a struggling downstream
    // service back up every application-detail page load one slow call at a time.
    @Override
    @CircuitBreaker(name = "eligibilityCheck", fallbackMethod = "checkEligibilityFallback")
    public List<EligibilityCheckResponse> checkEligibility(Long customerId, UUID loanProductId) {
        if (loanProductId == null) {
            return List.of();
        }
        List<LoanProductRuleResponse> rules = loanProductClient.getRules(loanProductId).getData();
        if (rules == null || rules.isEmpty()) {
            return List.of();
        }

        CustomerResponse customer = fetchOrNull(() -> customerClient.getById(customerId).getData());
        List<CustomerEmploymentResponse> employments = fetchOrNull(() -> customerClient.getEmployments(customerId).getData());
        List<CustomerIncomeResponse> incomes = fetchOrNull(() -> customerClient.getIncomes(customerId).getData());
        long existingLoanCount = loanRepository.findByCustomerId(customerId).stream()
                .filter(l -> COUNTS_AS_EXISTING.contains(l.getStatus()))
                .count();

        List<EligibilityCheckResponse> results = new ArrayList<>();
        for (LoanProductRuleResponse rule : rules) {
            if (!"ACTIVE".equals(rule.getStatus())) {
                continue;
            }
            results.add(evaluate(rule, customer, employments, incomes, existingLoanCount));
        }
        return results;
    }

    // Signature must mirror checkEligibility's plus a trailing Throwable — resilience4j
    // wires this up by reflection, matched on name + parameter shape. Degrades to "no
    // rules to show" rather than surfacing an error on the application detail page,
    // consistent with this whole feature being advisory-only.
    @SuppressWarnings("unused")
    private List<EligibilityCheckResponse> checkEligibilityFallback(Long customerId, UUID loanProductId, Throwable t) {
        log.warn("Eligibility check unavailable for customer {} / product {}: {}", customerId, loanProductId, t.toString());
        return List.of();
    }

    private EligibilityCheckResponse evaluate(
            LoanProductRuleResponse rule, CustomerResponse customer,
            List<CustomerEmploymentResponse> employments, List<CustomerIncomeResponse> incomes,
            long existingLoanCount) {
        EligibilityCheckResponse.EligibilityCheckResponseBuilder builder = EligibilityCheckResponse.builder()
                .ruleCode(rule.getRuleTemplateCode())
                .ruleName(rule.getRuleTemplateName())
                .field(rule.getField())
                .operator(rule.getOperator())
                .expectedValue(rule.getValue2() != null ? rule.getValue() + " - " + rule.getValue2() : rule.getValue());

        return switch (rule.getField()) {
            case CREDIT_SCORE -> builder.result(EligibilityResult.NOT_EVALUABLE)
                    .reason("No credit score data exists anywhere in the system").build();
            case DEBT_TO_INCOME_RATIO -> builder.result(EligibilityResult.NOT_EVALUABLE)
                    .reason("No debt/liability data exists to compute a ratio from").build();
            case AGE -> evaluateAge(builder, customer, rule);
            case MONTHLY_INCOME -> evaluateNumericField(builder, normalizeMonthlyIncome(incomes), rule);
            case EXISTING_LOAN_COUNT -> evaluateNumericField(builder, BigDecimal.valueOf(existingLoanCount), rule);
            case EMPLOYMENT_STATUS -> evaluateEmploymentStatus(builder, employments, rule);
        };
    }

    private EligibilityCheckResponse evaluateAge(
            EligibilityCheckResponse.EligibilityCheckResponseBuilder builder,
            CustomerResponse customer, LoanProductRuleResponse rule) {
        if (customer == null || customer.getDateOfBirth() == null) {
            return builder.result(EligibilityResult.NOT_EVALUABLE)
                    .reason("No date of birth on file for this customer").build();
        }
        int age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();
        return evaluateNumericField(builder, BigDecimal.valueOf(age), rule);
    }

    private EligibilityCheckResponse evaluateEmploymentStatus(
            EligibilityCheckResponse.EligibilityCheckResponseBuilder builder,
            List<CustomerEmploymentResponse> employments, LoanProductRuleResponse rule) {
        String currentType = employments == null ? null : employments.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .map(CustomerEmploymentResponse::getEmploymentType)
                .filter(t -> t != null && !t.isBlank())
                .findFirst()
                .orElse(null);
        if (currentType == null) {
            return builder.result(EligibilityResult.NOT_EVALUABLE)
                    .reason("No active employment record on file for this customer").build();
        }
        boolean pass = switch (rule.getOperator()) {
            case EQUALS -> currentType.equalsIgnoreCase(rule.getValue());
            case NOT_EQUALS -> !currentType.equalsIgnoreCase(rule.getValue());
            case IN -> Arrays.stream(rule.getValue().split(","))
                    .map(String::trim)
                    .anyMatch(v -> v.equalsIgnoreCase(currentType));
            // GREATER_THAN/LESS_THAN/BETWEEN don't apply to a categorical field —
            // a rule template mis-paired with EMPLOYMENT_STATUS this way can never pass.
            default -> false;
        };
        return builder.actualValue(currentType)
                .result(pass ? EligibilityResult.PASS : EligibilityResult.FAIL)
                .build();
    }

    private EligibilityCheckResponse evaluateNumericField(
            EligibilityCheckResponse.EligibilityCheckResponseBuilder builder,
            BigDecimal actual, LoanProductRuleResponse rule) {
        boolean pass = evaluateNumeric(actual, rule.getOperator(), rule.getValue(), rule.getValue2());
        return builder.actualValue(actual.toPlainString())
                .result(pass ? EligibilityResult.PASS : EligibilityResult.FAIL)
                .build();
    }

    private boolean evaluateNumeric(BigDecimal actual, RuleOperator operator, String value, String value2) {
        return switch (operator) {
            case EQUALS -> actual.compareTo(new BigDecimal(value)) == 0;
            case NOT_EQUALS -> actual.compareTo(new BigDecimal(value)) != 0;
            case GREATER_THAN -> actual.compareTo(new BigDecimal(value)) > 0;
            case GREATER_THAN_OR_EQUAL -> actual.compareTo(new BigDecimal(value)) >= 0;
            case LESS_THAN -> actual.compareTo(new BigDecimal(value)) < 0;
            case LESS_THAN_OR_EQUAL -> actual.compareTo(new BigDecimal(value)) <= 0;
            case BETWEEN -> actual.compareTo(new BigDecimal(value)) >= 0
                    && actual.compareTo(new BigDecimal(value2)) <= 0;
            case IN -> Arrays.stream(value.split(","))
                    .map(String::trim)
                    .anyMatch(v -> actual.compareTo(new BigDecimal(v)) == 0);
        };
    }

    // Sums every income source, normalized to a monthly figure — a customer can have
    // more than one (e.g. salary + side income), same as CustomerIncome's design.
    private BigDecimal normalizeMonthlyIncome(List<CustomerIncomeResponse> incomes) {
        if (incomes == null || incomes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CustomerIncomeResponse income : incomes) {
            if (income.getAmount() == null) continue;
            BigDecimal monthlyFactor = switch (income.getFrequency() == null ? "" : income.getFrequency()) {
                case "DAILY" -> new BigDecimal("30");
                case "WEEKLY" -> new BigDecimal("4.33");
                case "BIWEEKLY" -> new BigDecimal("2.167");
                case "ANNUALLY" -> BigDecimal.ONE.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
                default -> BigDecimal.ONE; // MONTHLY, or an unrecognized value — treat as already monthly
            };
            total = total.add(income.getAmount().multiply(monthlyFactor));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // A missing sub-resource (e.g. no employments/incomes on file yet) surfaces as a
    // 404 from customer-service — that's a legitimate "no data", not a system failure,
    // so it degrades to null (-> NOT_EVALUABLE) instead of failing the whole check.
    private <T> T fetchOrNull(java.util.function.Supplier<T> fetch) {
        try {
            return fetch.get();
        } catch (FeignException e) {
            return null;
        }
    }
}
