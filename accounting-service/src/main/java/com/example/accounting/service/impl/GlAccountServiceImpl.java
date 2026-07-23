package com.example.accounting.service.impl;

import com.example.accounting.dto.GlAccountRequest;
import com.example.accounting.dto.GlAccountResponse;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.exception.AppException;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.service.GlAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GlAccountServiceImpl implements GlAccountService {

    private final GlAccountRepository glAccountRepository;

    @Override
    public GlAccountResponse create(GlAccountRequest request) {
        if (glAccountRepository.existsByAccountNo(request.getAccountNo())) {
            throw new AppException(HttpStatus.CONFLICT, "Account number already exists: " + request.getAccountNo());
        }
        GlAccount account = GlAccount.builder()
                .parent(resolveParent(request.getParentId()))
                .accountNo(request.getAccountNo())
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .normalBalance(request.getNormalBalance())
                .currency(request.getCurrency())
                .allowPosting(request.getAllowPosting())
                .status(request.getStatus())
                .build();
        return toResponse(glAccountRepository.save(account));
    }

    @Override
    public GlAccountResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<GlAccountResponse> getAll() {
        return glAccountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public GlAccountResponse update(Long id, GlAccountRequest request) {
        GlAccount account = findOrThrow(id);
        if (!account.getAccountNo().equals(request.getAccountNo())
                && glAccountRepository.existsByAccountNo(request.getAccountNo())) {
            throw new AppException(HttpStatus.CONFLICT, "Account number already exists: " + request.getAccountNo());
        }
        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "An account cannot be its own parent");
        }
        account.setParent(resolveParent(request.getParentId()));
        account.setAccountNo(request.getAccountNo());
        account.setAccountName(request.getAccountName());
        account.setAccountType(request.getAccountType());
        account.setNormalBalance(request.getNormalBalance());
        account.setCurrency(request.getCurrency());
        account.setAllowPosting(request.getAllowPosting());
        account.setStatus(request.getStatus());
        return toResponse(glAccountRepository.save(account));
    }

    @Override
    public void delete(Long id) {
        GlAccount account = findOrThrow(id);
        if (!glAccountRepository.findByParentId(id).isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete an account that has child accounts");
        }
        glAccountRepository.delete(account);
    }

    private GlAccount resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return findOrThrow(parentId);
    }

    private GlAccount findOrThrow(Long id) {
        return glAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GL account", id));
    }

    private GlAccountResponse toResponse(GlAccount account) {
        return GlAccountResponse.builder()
                .id(account.getId())
                .parentId(account.getParent() != null ? account.getParent().getId() : null)
                .accountNo(account.getAccountNo())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .normalBalance(account.getNormalBalance())
                .currency(account.getCurrency())
                .allowPosting(account.isAllowPosting())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
