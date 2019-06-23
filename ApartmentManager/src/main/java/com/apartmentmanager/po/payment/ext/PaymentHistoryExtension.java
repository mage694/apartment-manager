package com.apartmentmanager.po.payment.ext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PaymentHistoryExtension implements Serializable {
    @Id
    @JsonIgnore
    private Integer paymentHistoryId;
    private List<? extends PremiumPayment> premiumPayments;
    private BigDecimal totalPrice;
    private BigDecimal receipts;
}
