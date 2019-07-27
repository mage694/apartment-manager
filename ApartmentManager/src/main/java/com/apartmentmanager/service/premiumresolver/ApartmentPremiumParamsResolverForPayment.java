package com.apartmentmanager.service.premiumresolver;

import com.apartmentmanager.constant.PremiumPaymentPredicateEnum;
import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import com.apartmentmanager.po.payment.ext.PremiumPayment;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByDate;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByMeasurement;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.apartmentmanager.constant.PremiumPaymentPredicateEnum.PREMIUM_PAYMENT_BY_DATE;
import static com.apartmentmanager.constant.PremiumPaymentPredicateEnum.PREMIUM_PAYMENT_BY_MEASUREMENT;

@Service
public class ApartmentPremiumParamsResolverForPayment extends AbstractPremiumParamsResolver {
    @Override
    protected Map<PremiumPaymentPredicateEnum, BiConsumer<PremiumPayment, ApartmentPremiumParams>> resolvers() {
        Map<PremiumPaymentPredicateEnum, BiConsumer<PremiumPayment, ApartmentPremiumParams>> resolvers = new HashMap<>();
        resolvers.put(PREMIUM_PAYMENT_BY_MEASUREMENT, new MeasurementConsumer());
        resolvers.put(PREMIUM_PAYMENT_BY_DATE, new DateConsumer());
        return resolvers;
    }

    private class MeasurementConsumer implements BiConsumer<PremiumPayment, ApartmentPremiumParams> {
        @Override
        public void accept(PremiumPayment premiumPayment, ApartmentPremiumParams currentPremium) {
            PremiumPaymentByMeasurement ppm = (PremiumPaymentByMeasurement) premiumPayment;
            ppm.setCreatedTime(LocalDateTime.now());
            ppm.setPremiumFlag(currentPremium.getPremiumFlag());
            ppm.setPreviousMeasurement(ppm.getCurrentMeasurement());
            ppm.setCurrentMeasurement(currentPremium.getCurrentMeasurement());
            ppm.setPremiumType(currentPremium.getPremiumType());
            ppm.setPaymentType(currentPremium.getPaymentType());
            ppm.setUnitPrice(currentPremium.getUnitPrice());
        }
    }

    private class DateConsumer implements BiConsumer<PremiumPayment, ApartmentPremiumParams> {
        @Override
        public void accept(PremiumPayment premiumPayment, ApartmentPremiumParams currentPremium) {
            PremiumPaymentByDate ppd = (PremiumPaymentByDate) premiumPayment;
            ppd.setCreatedTime(LocalDateTime.now());
            ppd.setPremiumFlag(currentPremium.getPremiumFlag());
            ppd.setFromDate(ppd.getToDate());
            ppd.setToDate(currentPremium.getExpiredDate());
            ppd.setPremiumType(currentPremium.getPremiumType());
            ppd.setPaymentType(currentPremium.getPaymentType());
        }
    }
}
