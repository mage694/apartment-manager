package com.apartmentmanager.service.premiumprocessor;

import com.apartmentmanager.po.payment.ext.PremiumPayment;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByDate;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByMeasurement;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PremiumProcessorDelegate {
    @Resource(name = "premiumProcessorByMeasurement")
    private IPremiumProcessor premiumProcessorByMeasurement;
    @Resource(name = "premiumProcessorByDate")
    private IPremiumProcessor premiumProcessorByDate;

    public BigDecimal processAndAccumulate(List<? extends PremiumPayment> premiumPayments) {
        BigDecimal base = new BigDecimal(0);
        base.setScale(1, RoundingMode.HALF_UP);
        premiumPayments.stream().forEach(payment -> {
            if (payment instanceof PremiumPaymentByMeasurement) {
                premiumProcessorByMeasurement.process(payment);
            } else if (payment instanceof PremiumPaymentByDate) {
                premiumProcessorByDate.process(payment);
            }
        });
        return premiumPayments.stream().map(PremiumPayment::getPremiumTotal).reduce(base, BigDecimal::add);
    }

}
