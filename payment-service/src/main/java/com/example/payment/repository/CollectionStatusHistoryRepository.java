package com.example.payment.repository;

import com.example.payment.entity.CollectionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionStatusHistoryRepository extends JpaRepository<CollectionStatusHistory, Long> {

    List<CollectionStatusHistory> findByCollectionCaseIdOrderByCreatedAtDesc(Long collectionCaseId);
}
