package com.apartmentmanager.service;

import com.apartmentmanager.dao.ApartmentPriceDao;
import com.apartmentmanager.dto.apartment.ApartmentInfoView;
import com.apartmentmanager.po.apartment.ApartmentPrice;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApartmentPriceService {
    @Getter
    @Autowired
    private ApartmentPriceDao apartmentPriceDao;

    public void bindPrices(List<ApartmentInfoView> apartmentInfoViews) {
        if (apartmentInfoViews.isEmpty()) {
            return;
        }

        List<Integer> apartmentIds = apartmentInfoViews.stream()
                .map(ApartmentInfoView::getApartmentId).collect(Collectors.toList());
        List<ApartmentPrice> prices = apartmentPriceDao.findByApartmentIdIn(apartmentIds);
        apartmentInfoViews.stream()
                .peek(a -> a.setPrices(new HashMap<>()))
                .forEach(
                        apartmentView -> prices.stream()
                                .filter(price -> price.getApartment().getId().equals(apartmentView.getApartmentId()))
                                .forEach(price -> apartmentView.getPrices().put(price.getPaymentType(), price.getPrice())));
    }
}
