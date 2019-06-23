package com.apartmentmanager.dto.apartment;

import com.apartmentmanager.constant.PaymentType;
import com.apartmentmanager.po.payment.ext.PremiumPayment;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ApartmentPaymentSummary {
    private String title;
    private BigDecimal concertedPrice;
    private Integer quantity;
    private PaymentType paymentType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal unsettledPrice;
    private List<? extends PremiumPayment> premiumPayments;
    private BigDecimal totalPrice;
}
