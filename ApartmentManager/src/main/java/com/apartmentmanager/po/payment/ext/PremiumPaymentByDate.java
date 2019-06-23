package com.apartmentmanager.po.payment.ext;

import com.apartmentmanager.constant.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PremiumPaymentByDate extends PremiumPayment {
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer quantity;
}
