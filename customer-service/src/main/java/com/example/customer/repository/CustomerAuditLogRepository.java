package com.example.customer.repository;

import com.example.customer.entity.CustomerAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerAuditLogRepository extends JpaRepository<CustomerAuditLog, Long> {

    // Newest first — unlike the other customer sub-resources (chronological
    // ASC), an audit trail is read as "what happened most recently".
    List<CustomerAuditLog> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
