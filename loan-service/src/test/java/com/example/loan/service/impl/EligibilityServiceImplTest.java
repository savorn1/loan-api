package com.example.loan.service.impl;

import com.example.loan.client.CustomerClient;
import com.example.loan.client.LoanProductClient;
import com.example.loan.common.ApiResponse;
import com.example.loan.dto.CustomerEmploymentResponse;
import com.example.loan.dto.CustomerIncomeResponse;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.EligibilityCheckResponse;
import com.example.loan.dto.EligibilityResult;
import com.example.loan.dto.LoanProductRuleResponse;
import com.example.loan.dto.RuleField;
import com.example.loan.dto.RuleOperator;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanStatus;
import com.example.loan.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EligibilityServiceImplTest {

    @Mock private LoanProductClient loanProductClient;
    @Mock private CustomerClient customerClient;
    @Mock private LoanRepository loanRepository;

    private EligibilityServiceImpl service;
    private final UUID productId = UUID.randomUUID();
    private final Long customerId = 9L;

    @BeforeEach
    void setUp() {
        service = new EligibilityServiceImpl(loanProductClient, customerClient, loanRepository);
        lenient().when(loanRepository.findByCustomerId(customerId)).thenReturn(List.of());
        lenient().when(customerClient.getById(customerId)).thenReturn(ApiResponse.success(new CustomerResponse()));
        lenient().when(customerClient.getEmployments(customerId)).thenReturn(ApiResponse.success(List.of()));
        lenient().when(customerClient.getIncomes(customerId)).thenReturn(ApiResponse.success(List.of()));
    }

    private LoanProductRuleResponse rule(RuleField field, RuleOperator operator, String value, String value2) {
        LoanProductRuleResponse r = new LoanProductRuleResponse();
        r.setRuleTemplateCode(field.name() + "_RULE");
        r.setRuleTemplateName(field.name() + " rule");
        r.setField(field);
        r.setOperator(operator);
        r.setValue(value);
        r.setValue2(value2);
        r.setStatus("ACTIVE");
        return r;
    }

    @Test
    void checkEligibility_returnsEmptyWhenNoLoanProductAssigned() {
        assertThat(service.checkEligibility(customerId, null)).isEmpty();
    }

    @Test
    void checkEligibility_flagsCreditScoreAndDebtToIncomeAsNotEvaluable() {
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(
                rule(RuleField.CREDIT_SCORE, RuleOperator.GREATER_THAN_OR_EQUAL, "650", null),
                rule(RuleField.DEBT_TO_INCOME_RATIO, RuleOperator.LESS_THAN_OR_EQUAL, "40", null)
        )));
        CustomerResponse customer = new CustomerResponse();
        customer.setDateOfBirth(LocalDate.now().minusYears(30));
        when(customerClient.getById(customerId)).thenReturn(ApiResponse.success(customer));

        List<EligibilityCheckResponse> results = service.checkEligibility(customerId, productId);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getResult() == EligibilityResult.NOT_EVALUABLE);
        assertThat(results).allMatch(r -> r.getReason() != null);
    }

    @Test
    void checkEligibility_skipsInactiveRules() {
        LoanProductRuleResponse inactive = rule(RuleField.AGE, RuleOperator.GREATER_THAN_OR_EQUAL, "18", null);
        inactive.setStatus("INACTIVE");
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(inactive)));

        assertThat(service.checkEligibility(customerId, productId)).isEmpty();
    }

    @Test
    void checkEligibility_ageRule_passesWhenWithinBetweenRange() {
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(
                rule(RuleField.AGE, RuleOperator.BETWEEN, "21", "60")
        )));
        CustomerResponse customer = new CustomerResponse();
        customer.setDateOfBirth(LocalDate.now().minusYears(30));
        when(customerClient.getById(customerId)).thenReturn(ApiResponse.success(customer));

        EligibilityCheckResponse result = service.checkEligibility(customerId, productId).get(0);

        assertThat(result.getResult()).isEqualTo(EligibilityResult.PASS);
        assertThat(result.getActualValue()).isEqualTo("30");
    }

    @Test
    void checkEligibility_ageRule_failsWhenOutsideBetweenRange() {
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(
                rule(RuleField.AGE, RuleOperator.BETWEEN, "21", "60")
        )));
        CustomerResponse customer = new CustomerResponse();
        customer.setDateOfBirth(LocalDate.now().minusYears(17));
        when(customerClient.getById(customerId)).thenReturn(ApiResponse.success(customer));

        assertThat(service.checkEligibility(customerId, productId).get(0).getResult())
                .isEqualTo(EligibilityResult.FAIL);
    }

    @Test
    void checkEligibility_ageRule_notEvaluableWithoutDateOfBirth() {
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(
                rule(RuleField.AGE, RuleOperator.BETWEEN, "21", "60")
        )));
        when(customerClient.getById(customerId)).thenReturn(ApiResponse.success(new CustomerResponse()));

        assertThat(service.checkEligibility(customerId, productId).get(0).getResult())
                .isEqualTo(EligibilityResult.NOT_EVALUABLE);
    }

    @Test
    void checkEligibility_existingLoanCount_onlyCountsPendingApprovedAndActive() {
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(
                rule(RuleField.EXISTING_LOAN_COUNT, RuleOperator.LESS_THAN_OR_EQUAL, "2", null)
        )));
        when(loanRepository.findByCustomerId(customerId)).thenReturn(List.of(
                Loan.builder().status(LoanStatus.PENDING).build(),
                Loan.builder().status(LoanStatus.ACTIVE).build(),
                Loan.builder().status(LoanStatus.CLOSED).build(),
                Loan.builder().status(LoanStatus.REJECTED).build()
        ));

        EligibilityCheckResponse result = service.checkEligibility(customerId, productId).get(0);

        // 2 count (PENDING + ACTIVE) — CLOSED/REJECTED excluded — so <= 2 passes.
        assertThat(result.getActualValue()).isEqualTo("2");
        assertThat(result.getResult()).isEqualTo(EligibilityResult.PASS);
    }

    @Test
    void checkEligibility_monthlyIncome_normalizesAnnualToMonthly() {
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(
                rule(RuleField.MONTHLY_INCOME, RuleOperator.GREATER_THAN_OR_EQUAL, "1000", null)
        )));
        CustomerIncomeResponse income = new CustomerIncomeResponse();
        income.setAmount(new BigDecimal("12000"));
        income.setFrequency("ANNUALLY");
        when(customerClient.getIncomes(customerId)).thenReturn(ApiResponse.success(List.of(income)));

        EligibilityCheckResponse result = service.checkEligibility(customerId, productId).get(0);

        assertThat(new BigDecimal(result.getActualValue())).isEqualByComparingTo("1000.00");
        assertThat(result.getResult()).isEqualTo(EligibilityResult.PASS);
    }

    @Test
    void checkEligibility_employmentStatus_inOperatorMatchesCaseInsensitively() {
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(
                rule(RuleField.EMPLOYMENT_STATUS, RuleOperator.IN, "FULL_TIME,SELF_EMPLOYED", null)
        )));
        CustomerEmploymentResponse employment = new CustomerEmploymentResponse();
        employment.setEmploymentType("full_time");
        employment.setStatus("ACTIVE");
        when(customerClient.getEmployments(customerId)).thenReturn(ApiResponse.success(List.of(employment)));

        assertThat(service.checkEligibility(customerId, productId).get(0).getResult())
                .isEqualTo(EligibilityResult.PASS);
    }

    @Test
    void checkEligibility_employmentStatus_notEvaluableWithNoActiveEmployment() {
        when(loanProductClient.getRules(productId)).thenReturn(ApiResponse.success(List.of(
                rule(RuleField.EMPLOYMENT_STATUS, RuleOperator.IN, "FULL_TIME", null)
        )));

        assertThat(service.checkEligibility(customerId, productId).get(0).getResult())
                .isEqualTo(EligibilityResult.NOT_EVALUABLE);
    }
}
