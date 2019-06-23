package com.apartmentmanager.po.payment.ext;

import com.apartmentmanager.constant.ApartmentPremiumType;
import com.apartmentmanager.constant.PaymentType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PremiumPayment implements Serializable {
    private String premiumFlag;
    private BigDecimal unitPrice;
    private BigDecimal premiumTotal;
    private LocalDateTime createdTime;
    private PaymentType paymentType;
    private ApartmentPremiumType premiumType;
}
