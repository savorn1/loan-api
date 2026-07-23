package com.example.loan.repository;

import com.example.loan.entity.ApplicationNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, Long> {

    List<ApplicationNote> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);
}
