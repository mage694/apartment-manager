package com.apartmentmanager.dto.apartment;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;

@Data
@Builder
public class ApartmentPageRequest {
    ApartmentFilter filter;
    PageRequest pageRequest;
}
