package com.apartmentmanager.service.premiumprocessor;

import com.apartmentmanager.po.payment.ext.PremiumPaymentByMeasurement;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("premiumProcessorByMeasurement")
public class PremiumProcessorByMeasurement implements IPremiumProcessor<PremiumPaymentByMeasurement> {
    @Override
    public void process(PremiumPaymentByMeasurement premiumPayment) {
        BigDecimal premiumTotal =  premiumPayment.getCurrentMeasurement()
                .subtract(premiumPayment.getPreviousMeasurement())
                .multiply(premiumPayment.getUnitPrice());
        premiumPayment.setPremiumTotal(premiumTotal);
    }
}

