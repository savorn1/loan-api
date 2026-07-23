package com.example.accounting.service;

import com.example.accounting.dto.JournalTemplateRequest;
import com.example.accounting.dto.JournalTemplateResponse;

import java.util.List;

public interface JournalTemplateService {

    JournalTemplateResponse create(JournalTemplateRequest request);

    JournalTemplateResponse getById(Long id);

    List<JournalTemplateResponse> getAll();

    JournalTemplateResponse update(Long id, JournalTemplateRequest request);

    void delete(Long id);
}
