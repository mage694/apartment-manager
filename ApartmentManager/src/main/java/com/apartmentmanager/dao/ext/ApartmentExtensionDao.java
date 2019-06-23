package com.apartmentmanager.dao.ext;

import com.apartmentmanager.po.apartment.ext.ApartmentInfoExtension;
import com.apartmentmanager.po.apartment.ext.ApartmentPremium;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApartmentExtensionDao extends MongoRepository<ApartmentInfoExtension, Integer> {
}
