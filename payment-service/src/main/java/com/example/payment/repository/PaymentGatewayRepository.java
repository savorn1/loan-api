package com.example.payment.repository;

import com.example.payment.entity.PaymentGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {

    boolean existsByCode(String code);

    Optional<PaymentGateway> findByCode(String code);

    Page<PaymentGateway> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
