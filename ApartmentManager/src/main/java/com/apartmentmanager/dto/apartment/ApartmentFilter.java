package com.apartmentmanager.dto.apartment;

import com.apartmentmanager.constant.ApartmentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApartmentFilter {
    private String userId;
    private String name;
    private ApartmentStatus status;
}
