package com.example.loan.repository;

import com.example.loan.entity.ApplicationApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationApprovalRepository extends JpaRepository<ApplicationApproval, Long> {

    List<ApplicationApproval> findByApplicationIdOrderByDecidedAtAsc(Long applicationId);
}
