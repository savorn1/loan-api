package com.example.customer.dto;

import com.example.customer.entity.AddressType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerAddressRequest {

    @NotNull
    private AddressType addressType;

    private String country;
    private String province;
    private String district;
    private String commune;
    private String village;
    private String street;
    private String postalCode;

    // Defaults to false in CustomerServiceImpl when omitted. Setting this true
    // clears the flag on the customer's other addresses. Boxed Boolean (not
    // primitive) so Jackson reads/writes the "isPrimary" JSON key as-is —
    // see the same note on CustomerAddressResponse.
    private Boolean isPrimary;
}
