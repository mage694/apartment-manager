package com.apartmentmanager.service;

import com.apartmentmanager.service.paymentwrapper.IPaymentSummaryWrapper;
import com.apartmentmanager.dto.apartment.ApartmentPaymentSummary;
import com.apartmentmanager.dto.customer.CustomerPayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentSummaryCalculator {
    @Autowired
    private IPaymentSummaryWrapper paymentSummaryWrapper;

    public ApartmentPaymentSummary calculate(Integer apartmentId, Integer customerId, CustomerPayment payment) {
        ApartmentPaymentSummary summaryView = new ApartmentPaymentSummary();
        summaryView.setQuantity(payment.getQuantity());
        paymentSummaryWrapper.wrap(apartmentId, customerId, payment, summaryView);
        return summaryView;
    }
}
