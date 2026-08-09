package com.example.accounting.repository;

import com.example.accounting.entity.ReconciliationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReconciliationSnapshotRepository extends JpaRepository<ReconciliationSnapshot, Long> {

    // Bounded to 90 (roughly a quarter of daily checks) rather than unbounded findAll — this
    // backs a trend chart, not an audit export.
    List<ReconciliationSnapshot> findTop90ByOrderByCreatedAtDesc();
}
