package com.apartmentmanager.service;

import com.apartmentmanager.dao.ext.ApartmentExtensionDao;
import com.apartmentmanager.po.apartment.ext.ApartmentInfoExtension;
import com.apartmentmanager.po.apartment.ext.ApartmentPremium;
import com.apartmentmanager.po.apartment.ext.ApartmentPremiumByDate;
import com.apartmentmanager.po.apartment.ext.ApartmentPremiumByMeasurement;
import com.apartmentmanager.dto.apartment.ApartmentInfoView;
import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApartmentExtensionService {
    @Getter
    @Autowired
    private ApartmentExtensionDao apartmentExtensionDao;

    @Transactional
    public void saveApartmentExtension(Integer apartmentId, List<ApartmentPremiumParams> premiums) {
        ApartmentInfoExtension extension = apartmentExtensionDao.findById(apartmentId).orElseGet(ApartmentInfoExtension::new);
        extension.setApartmentId(apartmentId);

        BeanCopier copier = BeanCopier.create(ApartmentPremiumParams.class, ApartmentPremium.class, false);
        List<? extends ApartmentPremium> premiumList= premiums.stream().map(p -> {
            if (p.getCurrentMeasurement() != null) {
                ApartmentPremiumByMeasurement premium = new ApartmentPremiumByMeasurement();
                copier.copy(p, premium, null);
                premium.setCurrentMeasurement(p.getCurrentMeasurement());
                return premium;
            } else {
                ApartmentPremiumByDate premium = new ApartmentPremiumByDate();
                copier.copy(p, premium, null);
                premium.setExpiredDate(p.getExpiredDate());
                return premium;
            }
        }).collect(Collectors.toList());
        extension.setPremiums(premiumList);
        apartmentExtensionDao.save(extension);
    }

    public void bindApartmentPremiums(List<ApartmentInfoView> apartmentInfoViews) {
        if (apartmentInfoViews.isEmpty()) {
            return;
        }

        List<Integer> apartmentIds = apartmentInfoViews.stream()
                .map(ApartmentInfoView::getApartmentId).collect(Collectors.toList());
        Iterable<ApartmentInfoExtension> extensions = apartmentExtensionDao.findAllById(apartmentIds);

        extensions.forEach(ext -> apartmentInfoViews.stream()
                .filter(a -> a.getApartmentId().equals(ext.getApartmentId()))
                .findAny()
                .ifPresent(apartment -> apartment.setPremiums(ext.getPremiums())));
    }
}
