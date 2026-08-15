package com.example.loan.repository;

import com.example.loan.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCustomerId(Long customerId);

    List<Application> findBySubmittedAtAfter(LocalDateTime since);

    List<Application> findByApplicationNoIsNull();

    // applicationNo is @Column(updatable = false) so a normal entity save() silently
    // excludes it from the generated UPDATE — a bulk JPQL update bypasses that mapping
    // restriction, which is the only way to write it after the row already exists.
    @Modifying
    @Query("UPDATE Application a SET a.applicationNo = :applicationNo WHERE a.id = :id")
    void updateApplicationNo(@Param("id") Long id, @Param("applicationNo") String applicationNo);
}
