package com.apartmentmanager.service.premiumprocessor;

import com.apartmentmanager.po.payment.ext.PremiumPaymentByDate;
import com.apartmentmanager.service.datecalculator.ExpiredDateCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("premiumProcessorByDate")
public class PremiumProcessorByDate implements IPremiumProcessor<PremiumPaymentByDate> {
    @Autowired
    private ExpiredDateCalculator dateCalculator;

    @Override
    public void process(PremiumPaymentByDate premiumPayment) {
        premiumPayment.setToDate(dateCalculator.calculate(premiumPayment.getFromDate(), premiumPayment.getPaymentType(), premiumPayment.getQuantity()));
        BigDecimal premiumTotal = BigDecimal.valueOf(premiumPayment.getQuantity()).multiply(premiumPayment.getUnitPrice());
        premiumPayment.setPremiumTotal(premiumTotal);
    }
}
