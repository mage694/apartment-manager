package com.apartmentmanager.dto.customer;

import com.apartmentmanager.constant.CustomerStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerFilter {
    private String name;
    private CustomerStatus status;
}
