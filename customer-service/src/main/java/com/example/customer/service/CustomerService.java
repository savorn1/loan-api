package com.example.customer.service;

import com.example.customer.common.PageResponse;
import com.example.customer.dto.CustomerRequest;
import com.example.customer.dto.CustomerResponse;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse getById(Long id);

    PageResponse<CustomerResponse> getAll(int page, int size, String sortBy, String sortOrder);

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);
}
