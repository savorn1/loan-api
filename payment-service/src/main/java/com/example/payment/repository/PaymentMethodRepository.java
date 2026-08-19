package com.example.payment.repository;

import com.example.payment.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    boolean existsByCode(String code);

    Optional<PaymentMethod> findByCode(String code);

    Page<PaymentMethod> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
