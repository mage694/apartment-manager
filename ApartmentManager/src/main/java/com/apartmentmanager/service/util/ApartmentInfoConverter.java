package com.apartmentmanager.service.util;

import com.apartmentmanager.constant.PaymentType;
import com.apartmentmanager.po.apartment.ApartmentInfo;
import com.apartmentmanager.po.apartment.ApartmentPrice;
import com.apartmentmanager.dto.apartment.ApartmentInfoView;
import org.springframework.cglib.beans.BeanCopier;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public final class ApartmentInfoConverter {
    public static ApartmentInfoView toView(ApartmentInfo apartmentInfo) {
        ApartmentInfoView view =  ApartmentInfoView.builder().build();
        BeanCopier copier = BeanCopier.create(ApartmentInfo.class, ApartmentInfoView.class, false);
        copier.copy(apartmentInfo, view, null);
        view.setApartmentId(apartmentInfo.getId());

        Map<PaymentType, BigDecimal> prices = apartmentInfo.getPrices().stream()
                .filter(p -> p.getPrice() != null)
                .collect(Collectors.toMap(ApartmentPrice::getPaymentType, ApartmentPrice::getPrice));
        view.setPrices(prices);
        return view;
    }
}
