package com.example.accounting.service.impl;

import com.example.accounting.dto.AccountingSchemeRequest;
import com.example.accounting.dto.AccountingSchemeResponse;
import com.example.accounting.entity.AccountingScheme;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.entity.JournalTemplate;
import com.example.accounting.exception.AppException;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.AccountingSchemeRepository;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.repository.JournalTemplateRepository;
import com.example.accounting.service.AccountingSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountingSchemeServiceImpl implements AccountingSchemeService {

    private final AccountingSchemeRepository accountingSchemeRepository;
    private final JournalTemplateRepository journalTemplateRepository;
    private final GlAccountRepository glAccountRepository;

    @Override
    public AccountingSchemeResponse create(AccountingSchemeRequest request) {
        JournalTemplate template = findTemplateOrThrow(request.getJournalTemplateId());
        validateRoleBelongsToTemplate(template, request.getAccountRole());
        if (accountingSchemeRepository.existsByJournalTemplateIdAndAccountRoleAndCurrency(
                request.getJournalTemplateId(), request.getAccountRole(), request.getCurrency())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A scheme already binds role " + request.getAccountRole() + " for currency " + request.getCurrency());
        }
        GlAccount glAccount = findAccountOrThrow(request.getGlAccountId());
        AccountingScheme scheme = AccountingScheme.builder()
                .journalTemplate(template)
                .accountRole(request.getAccountRole())
                .glAccount(glAccount)
                .currency(request.getCurrency())
                .status(request.getStatus())
                .build();
        return toResponse(accountingSchemeRepository.save(scheme));
    }

    @Override
    public AccountingSchemeResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<AccountingSchemeResponse> getAll() {
        return accountingSchemeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public AccountingSchemeResponse update(Long id, AccountingSchemeRequest request) {
        AccountingScheme scheme = findOrThrow(id);
        JournalTemplate template = findTemplateOrThrow(request.getJournalTemplateId());
        validateRoleBelongsToTemplate(template, request.getAccountRole());
        boolean bindingChanged = !scheme.getJournalTemplate().getId().equals(request.getJournalTemplateId())
                || !scheme.getAccountRole().equals(request.getAccountRole())
                || !scheme.getCurrency().equals(request.getCurrency());
        if (bindingChanged && accountingSchemeRepository.existsByJournalTemplateIdAndAccountRoleAndCurrency(
                request.getJournalTemplateId(), request.getAccountRole(), request.getCurrency())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A scheme already binds role " + request.getAccountRole() + " for currency " + request.getCurrency());
        }
        scheme.setJournalTemplate(template);
        scheme.setAccountRole(request.getAccountRole());
        scheme.setGlAccount(findAccountOrThrow(request.getGlAccountId()));
        scheme.setCurrency(request.getCurrency());
        scheme.setStatus(request.getStatus());
        return toResponse(accountingSchemeRepository.save(scheme));
    }

    @Override
    public void delete(Long id) {
        accountingSchemeRepository.delete(findOrThrow(id));
    }

    private void validateRoleBelongsToTemplate(JournalTemplate template, String accountRole) {
        boolean roleExists = template.getLines().stream().anyMatch(l -> l.getAccountRole().equals(accountRole));
        if (!roleExists) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Account role " + accountRole + " is not defined on journal template " + template.getCode());
        }
    }

    private AccountingScheme findOrThrow(Long id) {
        return accountingSchemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accounting scheme", id));
    }

    private JournalTemplate findTemplateOrThrow(Long id) {
        return journalTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal template", id));
    }

    private GlAccount findAccountOrThrow(Long id) {
        return glAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GL account", id));
    }

    private AccountingSchemeResponse toResponse(AccountingScheme scheme) {
        return AccountingSchemeResponse.builder()
                .id(scheme.getId())
                .journalTemplateId(scheme.getJournalTemplate().getId())
                .accountRole(scheme.getAccountRole())
                .glAccountId(scheme.getGlAccount().getId())
                .glAccountNo(scheme.getGlAccount().getAccountNo())
                .currency(scheme.getCurrency())
                .status(scheme.getStatus())
                .createdAt(scheme.getCreatedAt())
                .updatedAt(scheme.getUpdatedAt())
                .build();
    }
}
