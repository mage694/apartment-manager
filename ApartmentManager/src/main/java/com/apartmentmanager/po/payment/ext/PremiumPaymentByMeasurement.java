package com.apartmentmanager.po.payment.ext;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class PremiumPaymentByMeasurement extends PremiumPayment {
    private BigDecimal previousMeasurement;
    private BigDecimal currentMeasurement;
}
