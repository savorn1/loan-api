package com.example.accounting.service;

import com.example.accounting.dto.GlAccountRequest;
import com.example.accounting.dto.GlAccountResponse;

import java.util.List;

public interface GlAccountService {

    GlAccountResponse create(GlAccountRequest request);

    GlAccountResponse getById(Long id);

    List<GlAccountResponse> getAll();

    GlAccountResponse update(Long id, GlAccountRequest request);

    void delete(Long id);
}
