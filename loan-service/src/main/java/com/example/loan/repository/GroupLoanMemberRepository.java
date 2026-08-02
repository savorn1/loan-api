package com.example.loan.repository;

import com.example.loan.entity.GroupLoanMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupLoanMemberRepository extends JpaRepository<GroupLoanMember, Long> {

    List<GroupLoanMember> findByApplicationIdOrderByIdAsc(Long applicationId);

    Optional<GroupLoanMember> findByApplicationIdAndCustomerId(Long applicationId, Long customerId);
}
