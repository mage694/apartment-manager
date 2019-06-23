package com.apartmentmanager.dao.ext;

import com.apartmentmanager.po.payment.ext.PaymentHistoryExtension;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentHistoryExtensionDao extends MongoRepository<PaymentHistoryExtension, Integer> {
}
