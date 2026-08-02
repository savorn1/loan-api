package com.example.loan.repository;

import com.example.loan.entity.GroupLoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupLoanApplicationRepository extends JpaRepository<GroupLoanApplication, Long> {

    List<GroupLoanApplication> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
