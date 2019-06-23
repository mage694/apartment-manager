package com.apartmentmanager.service.paymentwrapper;

import com.apartmentmanager.dto.apartment.ApartmentPaymentSummary;
import com.apartmentmanager.dto.customer.CustomerPayment;

public interface IPaymentSummaryWrapper {
    void wrap(Integer apartmentId, Integer customerId, CustomerPayment payment, ApartmentPaymentSummary summary);
}
