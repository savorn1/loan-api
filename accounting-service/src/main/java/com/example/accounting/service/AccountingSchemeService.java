package com.example.accounting.service;

import com.example.accounting.dto.AccountingSchemeRequest;
import com.example.accounting.dto.AccountingSchemeResponse;

import java.util.List;

public interface AccountingSchemeService {

    AccountingSchemeResponse create(AccountingSchemeRequest request);

    AccountingSchemeResponse getById(Long id);

    List<AccountingSchemeResponse> getAll();

    AccountingSchemeResponse update(Long id, AccountingSchemeRequest request);

    void delete(Long id);
}
