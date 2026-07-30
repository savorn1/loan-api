package com.example.payment.service;

import com.example.payment.dto.PaymentTransactionRequest;
import com.example.payment.dto.PaymentTransactionResponse;
import com.example.payment.entity.TransactionStatus;

import java.util.List;

public interface PaymentTransactionService {

    PaymentTransactionResponse create(PaymentTransactionRequest request);

    PaymentTransactionResponse getById(Long id);

    List<PaymentTransactionResponse> getAll();

    PaymentTransactionResponse updateStatus(Long id, TransactionStatus status);
}
