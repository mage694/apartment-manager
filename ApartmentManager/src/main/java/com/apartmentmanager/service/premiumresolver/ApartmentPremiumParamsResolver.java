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
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.apartmentmanager.constant.PremiumPaymentPredicateEnum.PREMIUM_PAYMENT_BY_DATE;
import static com.apartmentmanager.constant.PremiumPaymentPredicateEnum.PREMIUM_PAYMENT_BY_MEASUREMENT;

@Service
public class ApartmentPremiumParamsResolver extends AbstractPremiumParamsResolver {

    @Override
    protected Map<PremiumPaymentPredicateEnum, BiConsumer<PremiumPayment, ApartmentPremiumParams>> resolvers() {
        Map<PremiumPaymentPredicateEnum, BiConsumer<PremiumPayment, ApartmentPremiumParams>> resolvers = new HashMap<>();
        resolvers.put(PREMIUM_PAYMENT_BY_MEASUREMENT, new SimplePremiumByMeasurementConsumer());
        resolvers.put(PREMIUM_PAYMENT_BY_DATE, new SimplePremiumByDateConsumer());
        return resolvers;
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

    private class SimplePremiumByMeasurementConsumer implements BiConsumer<PremiumPayment, ApartmentPremiumParams> {
        @Override
        public void accept(PremiumPayment premiumPayment, ApartmentPremiumParams premiumParams) {
            ((PremiumPaymentByMeasurement) premiumPayment).setCurrentMeasurement(premiumParams.getCurrentMeasurement());
        }
    }

    private class SimplePremiumByDateConsumer implements BiConsumer<PremiumPayment, ApartmentPremiumParams> {
        @Override
        public void accept(PremiumPayment premiumPayment, ApartmentPremiumParams premiumParams) {
            ((PremiumPaymentByDate) premiumPayment).setToDate(premiumParams.getExpiredDate());
        }
    }
}
