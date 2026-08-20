package com.example.loan.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class AmortizationCalculator {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final int SCALE = 2;

    private AmortizationCalculator() {
    }

    public record Installment(
            int installmentNumber,
            LocalDate dueDate,
            BigDecimal principalComponent,
            BigDecimal interestComponent,
            BigDecimal amount) {
    }

    public static BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return annualRatePercent.divide(BigDecimal.valueOf(100), MC).divide(BigDecimal.valueOf(12), MC);
    }

    public static BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int termMonths) {
        BigDecimal r = monthlyRate(annualRatePercent);
        if (r.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusRPowN = BigDecimal.ONE.add(r).pow(termMonths, MC);
        BigDecimal numerator = principal.multiply(r, MC).multiply(onePlusRPowN, MC);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
    }

    // A single repayment at maturity — principal plus simple (non-compounding)
    // interest, Actual/365. Used for DAY-unit terms, which don't fit the
    // monthly reducing-balance model the rest of this class assumes.
    public static Installment generateBulletInstallment(BigDecimal principal, BigDecimal annualRatePercent,
                                                          int days, LocalDate disbursementDate) {
        BigDecimal interest = principal.multiply(annualRatePercent, MC)
                .divide(BigDecimal.valueOf(100), MC)
                .multiply(BigDecimal.valueOf(days), MC)
                .divide(BigDecimal.valueOf(365), SCALE, RoundingMode.HALF_UP);
        BigDecimal amount = principal.add(interest);
        return new Installment(1, disbursementDate.plusDays(days), principal, interest, amount);
    }

    public static List<Installment> generateSchedule(BigDecimal principal, BigDecimal annualRatePercent,
                                                       int termMonths, LocalDate disbursementDate) {
        BigDecimal emi = calculateEmi(principal, annualRatePercent, termMonths);
        BigDecimal r = monthlyRate(annualRatePercent);
        BigDecimal remaining = principal;
        List<Installment> schedule = new ArrayList<>();

        for (int i = 1; i <= termMonths; i++) {
            BigDecimal interest = r.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)
                    : remaining.multiply(r, MC).setScale(SCALE, RoundingMode.HALF_UP);

            BigDecimal principalComponent;
            BigDecimal amount;
            if (i == termMonths) {
                principalComponent = remaining;
                amount = principalComponent.add(interest);
            } else {
                principalComponent = emi.subtract(interest);
                amount = emi;
            }
            remaining = remaining.subtract(principalComponent);
            schedule.add(new Installment(i, disbursementDate.plusMonths(i), principalComponent, interest, amount));
        }
        return schedule;
    }
}
