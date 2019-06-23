package com.apartmentmanager.service.premiumprocessor;

import com.apartmentmanager.po.payment.ext.PremiumPayment;

import java.util.function.Consumer;

public interface IPremiumProcessor<T extends PremiumPayment> extends Consumer<T> {

    void process(T premiumPayment);

    @Override
    default void accept(T premiumPayment) {
        process(premiumPayment);
    }
}
