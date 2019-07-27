package com.apartmentmanager.service.paymentwrapper;

import com.apartmentmanager.dao.EnrollmentHistoryDao;
import com.apartmentmanager.dto.apartment.ApartmentPaymentSummary;
import com.apartmentmanager.dto.customer.CustomerPayment;
import com.apartmentmanager.po.enrollment.EnrollmentHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Order(1)
@Service("enrollmentInfoWrapper")
public class EnrollmentInfoWrapper implements IPaymentSummaryWrapper {
    @Autowired
    private EnrollmentHistoryDao enrollmentHistoryDao;

    @Override
    public void wrap(Integer apartmentId, Integer customerId, CustomerPayment payment, ApartmentPaymentSummary summary) {
        EnrollmentHistory enrollmentHistory = enrollmentHistoryDao.findByApartmentIdAndCustomerId(apartmentId, customerId);
        summary.setPaymentType(enrollmentHistory.getOriginalPrice().getPaymentType());
        summary.setConcertedPrice(enrollmentHistory.getConcertedPrice());
    }
}
