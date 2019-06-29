package com.apartmentmanager.service.premiumresolver;

import com.apartmentmanager.constant.PremiumPaymentPredicateEnum;
import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import com.apartmentmanager.po.payment.ext.PremiumPayment;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractPremiumParamsResolver {

    protected Map<PremiumPaymentPredicateEnum, BiConsumer<PremiumPayment, ApartmentPremiumParams>> resolvers;

    public AbstractPremiumParamsResolver() {
        resolvers = resolvers();
    }

    protected abstract Map<PremiumPaymentPredicateEnum, BiConsumer<PremiumPayment, ApartmentPremiumParams>> resolvers();

    public void resolve(List<ApartmentPremiumParams> premiumParams, List<? extends PremiumPayment> premiumPayments) {
        premiumParams.forEach(p -> premiumPayments.stream()
                .filter(pp -> p.getPremiumFlag().equals((pp.getPremiumFlag())))
                .forEach(pp -> resolvers.get(PremiumPaymentPredicateEnum.get(pp)).accept(pp, p)));
    }
}
