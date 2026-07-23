package com.example.loan.service;

import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplicationApprovalRequest;
import com.example.loan.dto.ApplicationApprovalResponse;
import com.example.loan.dto.ApplicationDocumentRequest;
import com.example.loan.dto.ApplicationDocumentResponse;
import com.example.loan.dto.ApplicationNoteRequest;
import com.example.loan.dto.ApplicationNoteResponse;
import com.example.loan.dto.ApplicationRequest;
import com.example.loan.dto.ApplicationResponse;

import java.util.List;

public interface ApplicationService {

    ApplicationResponse create(ApplicationRequest request);

    ApplicationResponse getById(Long id);

    PageResponse<ApplicationResponse> getAll(int page, int size, String sortBy, String sortOrder);

    List<ApplicationResponse> getByCustomer(Long customerId);

    ApplicationResponse update(Long id, ApplicationRequest request);

    ApplicationResponse startReview(Long id);

    ApplicationResponse withdraw(Long id);

    void delete(Long id);

    ApplicationDocumentResponse addDocument(Long id, ApplicationDocumentRequest request);

    ApplicationDocumentResponse verifyDocument(Long id, Long documentId);

    ApplicationDocumentResponse rejectDocument(Long id, Long documentId);

    void deleteDocument(Long id, Long documentId);

    ApplicationNoteResponse addNote(Long id, ApplicationNoteRequest request);

    ApplicationResponse addApproval(Long id, ApplicationApprovalRequest request);
}
