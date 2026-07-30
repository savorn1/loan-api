package com.example.payment.dto;

// Days-past-due severity bucket, derived at query time from a loan's most
// overdue installment — there's no persisted DPD column to bucket on directly.
public enum CollectionBucket {
    DPD_1_30,
    DPD_31_60,
    DPD_61_90,
    DPD_90_PLUS;

    public static CollectionBucket classify(long maxDpd) {
        if (maxDpd <= 30) return DPD_1_30;
        if (maxDpd <= 60) return DPD_31_60;
        if (maxDpd <= 90) return DPD_61_90;
        return DPD_90_PLUS;
    }
}
