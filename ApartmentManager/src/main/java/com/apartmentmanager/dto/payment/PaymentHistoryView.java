package com.apartmentmanager.dto.payment;

import com.apartmentmanager.constant.PaymentType;
import com.apartmentmanager.po.payment.ext.PremiumPayment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PaymentHistoryView {
    private Integer id;
    private LocalDate fromDate;
    private LocalDate toDate;
    private PaymentType paymentType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String primaryCustomerName;
    private Integer apartmentId;
    private String apartmentName;
    private List<? extends PremiumPayment> premiums;
    private BigDecimal totalPrice;
    private BigDecimal receipts;
    private boolean canBeReverted;
}
