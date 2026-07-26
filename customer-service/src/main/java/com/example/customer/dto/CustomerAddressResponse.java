package com.example.customer.dto;

import com.example.customer.entity.AddressType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerAddressResponse {

    private Long id;
    private Long customerId;
    private AddressType addressType;
    private String country;
    private String province;
    private String district;
    private String commune;
    private String village;
    private String street;
    private String postalCode;
    // Boxed (not primitive) so Lombok emits getIsPrimary() rather than the
    // special isPrimary() boolean-getter form — Jackson strips a leading "is"
    // from is-prefixed getters, which would otherwise serialize this as
    // "primary" instead of "isPrimary".
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
