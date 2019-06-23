package com.apartmentmanager.dao.ext;

import com.apartmentmanager.po.payment.ext.PremiumPayment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PremiumPaymentDao extends MongoRepository<PremiumPayment, Integer> {
}
