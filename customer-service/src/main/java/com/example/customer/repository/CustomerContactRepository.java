package com.example.customer.repository;

import com.example.customer.entity.CustomerContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerContactRepository extends JpaRepository<CustomerContact, Long> {

    List<CustomerContact> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
}
