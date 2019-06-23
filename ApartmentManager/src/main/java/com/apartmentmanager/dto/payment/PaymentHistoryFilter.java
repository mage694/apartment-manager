package com.apartmentmanager.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentHistoryFilter {
    private String name;
}
