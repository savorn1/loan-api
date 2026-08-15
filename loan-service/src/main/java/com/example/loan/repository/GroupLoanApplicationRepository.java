package com.example.loan.repository;

import com.example.loan.entity.GroupLoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupLoanApplicationRepository extends JpaRepository<GroupLoanApplication, Long> {

    List<GroupLoanApplication> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    List<GroupLoanApplication> findByApplicationNoIsNull();

    // applicationNo is @Column(updatable = false) so a normal entity save() silently
    // excludes it from the generated UPDATE — a bulk JPQL update bypasses that mapping
    // restriction, which is the only way to write it after the row already exists.
    @Modifying
    @Query("UPDATE GroupLoanApplication a SET a.applicationNo = :applicationNo WHERE a.id = :id")
    void updateApplicationNo(@Param("id") Long id, @Param("applicationNo") String applicationNo);
}
