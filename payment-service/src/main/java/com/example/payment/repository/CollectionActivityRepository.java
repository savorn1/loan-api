package com.example.payment.repository;

import com.example.payment.entity.CollectionActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionActivityRepository extends JpaRepository<CollectionActivity, Long> {

    List<CollectionActivity> findByCollectionCaseIdOrderByCreatedAtDesc(Long collectionCaseId);
}
