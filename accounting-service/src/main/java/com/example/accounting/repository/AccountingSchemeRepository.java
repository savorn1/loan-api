package com.example.accounting.repository;

import com.example.accounting.entity.AccountingScheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountingSchemeRepository extends JpaRepository<AccountingScheme, Long> {

    boolean existsByJournalTemplateIdAndAccountRoleAndCurrency(Long journalTemplateId, String accountRole, String currency);

    Optional<AccountingScheme> findByJournalTemplateIdAndAccountRoleAndCurrency(Long journalTemplateId, String accountRole, String currency);

    List<AccountingScheme> findByJournalTemplateId(Long journalTemplateId);
}
