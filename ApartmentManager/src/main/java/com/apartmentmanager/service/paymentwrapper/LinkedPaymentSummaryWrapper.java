package com.apartmentmanager.service.paymentwrapper;

import com.apartmentmanager.dto.apartment.ApartmentPaymentSummary;
import com.apartmentmanager.dto.customer.CustomerPayment;
import com.apartmentmanager.spring.BeanProvider;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;

@Primary
@Service("linkedPaymentSummaryWrapper")
public class LinkedPaymentSummaryWrapper implements IPaymentSummaryWrapper {
    @Autowired
    private BeanProvider beanProvider;

    @Override
    public void wrap(Integer apartmentId, Integer customerId, CustomerPayment payment, ApartmentPaymentSummary summary) {
        beanProvider.getBeans(IPaymentSummaryWrapper.class, this)
                .forEach(w -> w.wrap(apartmentId, customerId, payment, summary));
    }
}
