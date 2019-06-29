package com.apartmentmanager.service;

import com.apartmentmanager.dao.ext.ApartmentExtensionDao;
import com.apartmentmanager.dto.apartment.ApartmentInfoView;
import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import com.apartmentmanager.po.apartment.ext.ApartmentInfoExtension;
import com.apartmentmanager.po.apartment.ext.ApartmentPremium;
import com.apartmentmanager.service.premiumresolver.ApartmentPremiumParamsResolver;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApartmentExtensionService {
    @Getter
    @Autowired
    private ApartmentExtensionDao apartmentExtensionDao;

    @Autowired
    private ApartmentPremiumParamsResolver apartmentPremiumParamsResolver;

    @Transactional
    public void saveApartmentExtension(Integer apartmentId, List<ApartmentPremiumParams> premiums) {
        ApartmentInfoExtension extension = apartmentExtensionDao.findById(apartmentId).orElseGet(ApartmentInfoExtension::new);
        extension.setApartmentId(apartmentId);
        List<? extends ApartmentPremium> premiumList = premiums.stream().map(apartmentPremiumParamsResolver::toApartmentPremium).collect(Collectors.toList());
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
