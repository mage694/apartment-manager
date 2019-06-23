package com.apartmentmanager.dao;

import com.apartmentmanager.constant.PaymentType;
import com.apartmentmanager.po.apartment.ApartmentInfo;
import com.apartmentmanager.po.apartment.ApartmentPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartmentPriceDao extends JpaRepository<ApartmentPrice, Integer> {

    ApartmentPrice findByApartmentAndPaymentType(ApartmentInfo apartmentId, PaymentType paymentType);

    List<ApartmentPrice> findByApartmentIdIn(List<Integer> apartmentIds);

    List<ApartmentPrice> findByApartmentId(Integer apartmentIds);
}
