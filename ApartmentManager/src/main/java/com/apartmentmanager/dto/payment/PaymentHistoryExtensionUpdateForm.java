package com.apartmentmanager.dto.payment;

import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentHistoryExtensionUpdateForm implements Serializable {
    private BigDecimal totalPrice;
    private BigDecimal receipts;
    private List<ApartmentPremiumParams> premiums;
}
