package com.apartmentmanager.dto.apartment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasicRequestWrapper<T> {
    private String userId;
    private T param;
}
