package com.example.payment.repository;

import com.example.payment.entity.PaymentChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentChannelRepository extends JpaRepository<PaymentChannel, Long> {

    boolean existsByCode(String code);

    Optional<PaymentChannel> findByCode(String code);

    Page<PaymentChannel> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
