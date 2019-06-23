package com.apartmentmanager.dto.customer;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;

@Data
@Builder
public class CustomerPageRequest {
    CustomerFilter filter;
    PageRequest pageRequest;
}
