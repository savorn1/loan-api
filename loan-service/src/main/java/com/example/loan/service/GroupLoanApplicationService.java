package com.example.loan.service;

import com.example.loan.common.PageResponse;
import com.example.loan.dto.GroupLoanApplicationApprovalRequest;
import com.example.loan.dto.GroupLoanApplicationDocumentRequest;
import com.example.loan.dto.GroupLoanApplicationDocumentResponse;
import com.example.loan.dto.GroupLoanApplicationRequest;
import com.example.loan.dto.GroupLoanApplicationResponse;

import java.util.List;

public interface GroupLoanApplicationService {

    GroupLoanApplicationResponse create(GroupLoanApplicationRequest request);

    GroupLoanApplicationResponse getById(Long id);

    PageResponse<GroupLoanApplicationResponse> getAll(int page, int size, String sortBy, String sortOrder);

    List<GroupLoanApplicationResponse> getByGroup(Long groupId);

    GroupLoanApplicationResponse startReview(Long id);

    GroupLoanApplicationResponse cancel(Long id);

    GroupLoanApplicationResponse addApproval(Long id, GroupLoanApplicationApprovalRequest request);

    GroupLoanApplicationDocumentResponse addDocument(Long id, GroupLoanApplicationDocumentRequest request);

    GroupLoanApplicationDocumentResponse verifyDocument(Long id, Long documentId);

    GroupLoanApplicationDocumentResponse rejectDocument(Long id, Long documentId);

    void deleteDocument(Long id, Long documentId);
}
