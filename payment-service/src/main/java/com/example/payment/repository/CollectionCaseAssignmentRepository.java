package com.example.payment.repository;

import com.example.payment.entity.CollectionCaseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionCaseAssignmentRepository extends JpaRepository<CollectionCaseAssignment, Long> {

    List<CollectionCaseAssignment> findByCollectionCaseIdOrderByCreatedAtDesc(Long collectionCaseId);
}
