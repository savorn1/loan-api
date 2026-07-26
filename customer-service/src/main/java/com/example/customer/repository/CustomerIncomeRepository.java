package com.example.customer.repository;

import com.example.customer.entity.CustomerIncome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerIncomeRepository extends JpaRepository<CustomerIncome, Long> {

    List<CustomerIncome> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
}
