package com.apartmentmanager.service.premiumresolver;

import com.apartmentmanager.constant.PremiumPaymentPredicateEnum;
import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import com.apartmentmanager.po.apartment.ext.ApartmentPremium;
import com.apartmentmanager.po.apartment.ext.ApartmentPremiumByDate;
import com.apartmentmanager.po.apartment.ext.ApartmentPremiumByMeasurement;
import com.apartmentmanager.po.payment.ext.PremiumPayment;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByDate;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByMeasurement;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@Service
public class ApartmentPremiumParamsResolver {
    private Map<PremiumPaymentPredicateEnum, BiConsumer<PremiumPayment, ApartmentPremiumParams>> resolvers;

    public ApartmentPremiumParamsResolver() {
        resolvers = new HashMap<>();
        resolvers.put(PremiumPaymentPredicateEnum.PREMIUM_PAYMENT_BY_MEASUREMENT, (pp, p) -> ((PremiumPaymentByMeasurement) pp).setCurrentMeasurement(p.getCurrentMeasurement()));
        resolvers.put(PremiumPaymentPredicateEnum.PREMIUM_PAYMENT_BY_DATE, (pp, p) -> ((PremiumPaymentByDate) pp).setToDate(p.getExpiredDate()));
    }

    public ApartmentPremiumParamsResolver(Map<PremiumPaymentPredicateEnum, BiConsumer<PremiumPayment, ApartmentPremiumParams>> resolvers) {
        this.resolvers = resolvers;
    }

    public void resolve(List<ApartmentPremiumParams> premiumParams, List<? extends PremiumPayment> premiumPayments) {
        premiumParams.forEach(p -> premiumPayments.stream()
                .filter(pp -> p.getPremiumFlag().equals((pp.getPremiumFlag())))
                .forEach(pp -> resolvers.get(PremiumPaymentPredicateEnum.get(pp)).accept(pp, p)));
    }

    public ApartmentPremium toApartmentPremium(ApartmentPremiumParams p, BiConsumer<ApartmentPremiumParams, ApartmentPremium>... extraHandlers) {
        BeanCopier copier = BeanCopier.create(ApartmentPremiumParams.class, ApartmentPremium.class, false);
        if (p.getCurrentMeasurement() != null) {
            ApartmentPremiumByMeasurement premium = new ApartmentPremiumByMeasurement();
            copier.copy(p, premium, null);
            premium.setCurrentMeasurement(p.getCurrentMeasurement());
            Optional.ofNullable(extraHandlers).ifPresent(hs -> Stream.of(hs).forEach(h -> h.accept(p, premium)));
            return premium;
        } else {
            ApartmentPremiumByDate premium = new ApartmentPremiumByDate();
            copier.copy(p, premium, null);
            premium.setExpiredDate(p.getExpiredDate());
            Optional.ofNullable(extraHandlers).ifPresent(hs -> Stream.of(hs).forEach(h -> h.accept(p, premium)));
            return premium;
        }
    }

    public PremiumPayment toPremiumPayment(ApartmentPremiumParams p, BiConsumer<ApartmentPremiumParams, PremiumPayment>... extraHandlers) {
        BeanCopier copier = BeanCopier.create(ApartmentPremiumParams.class, PremiumPayment.class, false);
        if (p.getCurrentMeasurement() != null) {
            PremiumPaymentByMeasurement ppm = new PremiumPaymentByMeasurement();
            ppm.setCurrentMeasurement(p.getCurrentMeasurement());
            copier.copy(p, ppm, null);
            Optional.ofNullable(extraHandlers).ifPresent(hs -> Stream.of(hs).forEach(h -> h.accept(p, ppm)));
            return ppm;
        } else {
            PremiumPaymentByDate ppd = new PremiumPaymentByDate();
            ppd.setToDate(p.getExpiredDate());
            copier.copy(p, ppd, null);
            Optional.ofNullable(extraHandlers).ifPresent(hs -> Stream.of(hs).forEach(h -> h.accept(p, ppd)));
            return ppd;
        }
    }
}
