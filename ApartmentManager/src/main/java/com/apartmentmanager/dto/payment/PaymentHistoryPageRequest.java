package com.apartmentmanager.dto.payment;

import com.apartmentmanager.dto.apartment.ApartmentFilter;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;

@Data
@Builder
public class PaymentHistoryPageRequest {
    PaymentHistoryFilter filter;
    PageRequest pageRequest;
}
