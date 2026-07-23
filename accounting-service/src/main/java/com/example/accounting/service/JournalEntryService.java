package com.example.accounting.service;

import com.example.accounting.dto.JournalEntryRequest;
import com.example.accounting.dto.JournalEntryResponse;

import java.util.List;

public interface JournalEntryService {

    JournalEntryResponse create(JournalEntryRequest request);

    JournalEntryResponse getById(Long id);

    List<JournalEntryResponse> getAll(Long financialPeriodId);

    JournalEntryResponse post(Long id);

    JournalEntryResponse reverse(Long id);
}
