package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.LoanProductRequest;
import com.example.loanproduct.dto.LoanProductResponse;
import com.example.loanproduct.entity.LoanProduct;
import com.example.loanproduct.entity.LoanProductDocument;
import com.example.loanproduct.entity.LoanProductFeeScheme;
import com.example.loanproduct.entity.LoanProductInterestScheme;
import com.example.loanproduct.entity.LoanProductRule;
import com.example.loanproduct.entity.LoanProductStatus;
import com.example.loanproduct.entity.LoanProductTerm;
import com.example.loanproduct.exception.AppException;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.LoanProductDocumentRepository;
import com.example.loanproduct.repository.LoanProductFeeSchemeRepository;
import com.example.loanproduct.repository.LoanProductInterestSchemeRepository;
import com.example.loanproduct.repository.LoanProductRepository;
import com.example.loanproduct.repository.LoanProductRuleRepository;
import com.example.loanproduct.repository.LoanProductTermRepository;
import com.example.loanproduct.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanProductServiceImpl implements LoanProductService {

    private final LoanProductRepository loanProductRepository;
    private final LoanProductInterestSchemeRepository interestSchemeMappingRepository;
    private final LoanProductFeeSchemeRepository feeSchemeMappingRepository;
    private final LoanProductTermRepository termRepository;
    private final LoanProductRuleRepository ruleRepository;
    private final LoanProductDocumentRepository documentRepository;

    @Override
    public LoanProductResponse create(LoanProductRequest request) {
        String username = currentUsername();
        LoanProduct product = LoanProduct.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .loanType(request.getLoanType())
                .currency(request.getCurrency())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .minTerm(request.getMinTerm())
                .maxTerm(request.getMaxTerm())
                .status(LoanProductStatus.DRAFT)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .version(1)
                .createdBy(username)
                .updatedBy(username)
                .build();
        return toResponse(loanProductRepository.save(product));
    }

    @Override
    public LoanProductResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<LoanProductResponse> getAll() {
        return loanProductRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public LoanProductResponse update(UUID id, LoanProductRequest request) {
        LoanProduct product = findOrThrow(id);
        if (product.getStatus() != LoanProductStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only DRAFT products can be updated");
        }
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setLoanType(request.getLoanType());
        product.setCurrency(request.getCurrency());
        product.setMinAmount(request.getMinAmount());
        product.setMaxAmount(request.getMaxAmount());
        product.setMinTerm(request.getMinTerm());
        product.setMaxTerm(request.getMaxTerm());
        product.setEffectiveFrom(request.getEffectiveFrom());
        product.setEffectiveTo(request.getEffectiveTo());
        product.setUpdatedBy(currentUsername());
        return toResponse(loanProductRepository.save(product));
    }

    @Override
    public void delete(UUID id) {
        loanProductRepository.delete(findOrThrow(id));
    }

    @Override
    @Transactional
    public LoanProductResponse publish(UUID id) {
        LoanProduct product = findOrThrow(id);
        if (product.getStatus() != LoanProductStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only DRAFT products can be published");
        }
        List<String> missing = new ArrayList<>();
        if (!interestSchemeMappingRepository.existsByLoanProductId(id)) {
            missing.add("an interest scheme");
        }
        if (!termRepository.existsByLoanProductId(id)) {
            missing.add("a term");
        }
        if (!missing.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot publish: missing " + String.join(", ", missing));
        }
        product.setStatus(LoanProductStatus.PUBLISHED);
        product.setUpdatedBy(currentUsername());
        return toResponse(loanProductRepository.save(product));
    }

    @Override
    @Transactional
    public LoanProductResponse newVersion(UUID id) {
        LoanProduct source = findOrThrow(id);
        if (source.getStatus() != LoanProductStatus.PUBLISHED && source.getStatus() != LoanProductStatus.INACTIVE) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only PUBLISHED or INACTIVE products can be revised into a new version");
        }
        String username = currentUsername();
        LoanProduct newProduct = LoanProduct.builder()
                .code(source.getCode())
                .name(source.getName())
                .description(source.getDescription())
                .loanType(source.getLoanType())
                .currency(source.getCurrency())
                .minAmount(source.getMinAmount())
                .maxAmount(source.getMaxAmount())
                .minTerm(source.getMinTerm())
                .maxTerm(source.getMaxTerm())
                .effectiveFrom(source.getEffectiveFrom())
                .effectiveTo(source.getEffectiveTo())
                .version(source.getVersion() + 1)
                .status(LoanProductStatus.DRAFT)
                .createdBy(username)
                .updatedBy(username)
                .build();
        newProduct = loanProductRepository.save(newProduct);
        cloneMappings(source.getId(), newProduct);
        return toResponse(newProduct);
    }

    @Override
    @Transactional
    public LoanProductResponse retire(UUID id) {
        LoanProduct product = findOrThrow(id);
        if (product.getStatus() != LoanProductStatus.PUBLISHED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only PUBLISHED products can be retired");
        }
        product.setStatus(LoanProductStatus.INACTIVE);
        product.setUpdatedBy(currentUsername());
        return toResponse(loanProductRepository.save(product));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private void cloneMappings(UUID sourceProductId, LoanProduct newProduct) {
        List<LoanProductInterestScheme> interestSchemes = interestSchemeMappingRepository.findByLoanProductId(sourceProductId).stream()
                .map(m -> LoanProductInterestScheme.builder()
                        .loanProduct(newProduct)
                        .interestScheme(m.getInterestScheme())
                        .priority(m.getPriority())
                        .effectiveFrom(m.getEffectiveFrom())
                        .effectiveTo(m.getEffectiveTo())
                        .isDefault(m.getIsDefault())
                        .status(m.getStatus())
                        .build())
                .toList();
        interestSchemeMappingRepository.saveAll(interestSchemes);

        List<LoanProductFeeScheme> feeSchemes = feeSchemeMappingRepository.findByLoanProductId(sourceProductId).stream()
                .map(m -> LoanProductFeeScheme.builder()
                        .loanProduct(newProduct)
                        .feeScheme(m.getFeeScheme())
                        .isMandatory(m.getIsMandatory())
                        .priority(m.getPriority())
                        .effectiveFrom(m.getEffectiveFrom())
                        .effectiveTo(m.getEffectiveTo())
                        .status(m.getStatus())
                        .build())
                .toList();
        feeSchemeMappingRepository.saveAll(feeSchemes);

        List<LoanProductTerm> terms = termRepository.findByLoanProductId(sourceProductId).stream()
                .map(t -> LoanProductTerm.builder()
                        .loanProduct(newProduct)
                        .termTemplate(t.getTermTemplate())
                        .isDefault(t.getIsDefault())
                        .status(t.getStatus())
                        .build())
                .toList();
        termRepository.saveAll(terms);

        List<LoanProductRule> rules = ruleRepository.findByLoanProductId(sourceProductId).stream()
                .map(r -> LoanProductRule.builder()
                        .loanProduct(newProduct)
                        .ruleTemplate(r.getRuleTemplate())
                        .status(r.getStatus())
                        .build())
                .toList();
        ruleRepository.saveAll(rules);

        List<LoanProductDocument> documents = documentRepository.findByLoanProductId(sourceProductId).stream()
                .map(d -> LoanProductDocument.builder()
                        .loanProduct(newProduct)
                        .documentTemplate(d.getDocumentTemplate())
                        .required(d.getRequired())
                        .status(d.getStatus())
                        .build())
                .toList();
        documentRepository.saveAll(documents);
    }

    private LoanProduct findOrThrow(UUID id) {
        return loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product", id));
    }

    private LoanProductResponse toResponse(LoanProduct product) {
        return LoanProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .version(product.getVersion())
                .name(product.getName())
                .description(product.getDescription())
                .loanType(product.getLoanType())
                .currency(product.getCurrency())
                .minAmount(product.getMinAmount())
                .maxAmount(product.getMaxAmount())
                .minTerm(product.getMinTerm())
                .maxTerm(product.getMaxTerm())
                .status(product.getStatus())
                .effectiveFrom(product.getEffectiveFrom())
                .effectiveTo(product.getEffectiveTo())
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .updatedBy(product.getUpdatedBy())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
