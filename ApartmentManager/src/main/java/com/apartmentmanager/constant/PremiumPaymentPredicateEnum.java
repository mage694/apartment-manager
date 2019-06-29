package com.apartmentmanager.constant;

import com.apartmentmanager.po.payment.ext.PremiumPayment;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByDate;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByMeasurement;

import java.util.function.Predicate;
import java.util.stream.Stream;

public enum PremiumPaymentPredicateEnum {
    PREMIUM_PAYMENT_BY_MEASUREMENT(pp -> pp instanceof PremiumPaymentByMeasurement),
    PREMIUM_PAYMENT_BY_DATE(pp -> pp instanceof PremiumPaymentByDate);

    private Predicate<PremiumPayment> predicate;

    private PremiumPaymentPredicateEnum(Predicate<PremiumPayment> predicate) {
        this.predicate = predicate;
    }

    public static PremiumPaymentPredicateEnum get(PremiumPayment pp) {
        return Stream.of(PremiumPaymentPredicateEnum.values()).filter(p -> p.predicate.test(pp))
                .findAny().orElseThrow(IllegalArgumentException::new);
    }

}
