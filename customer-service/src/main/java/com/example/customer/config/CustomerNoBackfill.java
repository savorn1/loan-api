package com.example.customer.config;

import com.example.customer.entity.Customer;
import com.example.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

// One-time backfill for customers left with a null customerNo — rows created before this
// column existed (see CustomerServiceImpl.create()'s two-phase-save comment). Idempotent:
// only rows still null get touched, so this is a no-op on every startup after the first.
// Unlike Application.applicationNo / Loan.loanNo in loan-service, Customer.customerNo isn't
// updatable=false, so a normal save() here is enough — no bulk update needed. Mirrors
// loan-service's ApplicationNoBackfill / GroupCodeBackfill.
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerNoBackfill implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Customer> missing = customerRepository.findByCustomerNoIsNull();
        if (missing.isEmpty()) {
            return;
        }
        for (Customer customer : missing) {
            String datePart = customer.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            customer.setCustomerNo("CUS-" + datePart + "-" + String.format("%06d", customer.getId()));
        }
        customerRepository.saveAll(missing);
        log.info("Backfilled customerNo for {} customer(s)", missing.size());
    }
}
