package com.apartmentmanager.service.paymentwrapper;

import com.apartmentmanager.dao.PaymentHistoryDao;
import com.apartmentmanager.dao.ext.PaymentHistoryExtensionDao;
import com.apartmentmanager.po.payment.PaymentHistory;
import com.apartmentmanager.po.payment.ext.PremiumPayment;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByDate;
import com.apartmentmanager.po.payment.ext.PremiumPaymentByMeasurement;
import com.apartmentmanager.service.datecalculator.ExpiredDateCalculator;
import com.apartmentmanager.service.premiumprocessor.PremiumProcessorDelegate;
import com.apartmentmanager.dto.apartment.ApartmentPaymentSummary;
import com.apartmentmanager.dto.customer.CustomerPayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service("latestPaymentWrapper")
public class LatestPaymentWrapper implements IPaymentSummaryWrapper {
    @Autowired
    private PaymentHistoryDao paymentHistoryDao;
    @Autowired
    private ExpiredDateCalculator dateCalculator;
    @Autowired
    private PaymentHistoryExtensionDao paymentHistoryExtensionDao;
    @Autowired
    private PremiumProcessorDelegate premiumProcessorDelegate;

    @Override
    public void wrap(Integer apartmentId, Integer customerId, CustomerPayment payment, ApartmentPaymentSummary summary) {
        PaymentHistory latestPayment = paymentHistoryDao.findTopByApartmentIdAndCustomerIdOrderByCreatedTimeDesc(apartmentId, customerId).orElseThrow(IllegalStateException::new);
        summary.setFromDate(latestPayment.getToDate());
        summary.setToDate(dateCalculator.calculate(summary.getFromDate(), summary.getPaymentType(), payment.getQuantity()));

        //calculate premiums
        paymentHistoryExtensionDao.findById(latestPayment.getId()).ifPresent(ext -> {
            summary.setUnsettledPrice(ext.getTotalPrice().subtract(ext.getReceipts()));
            List<PremiumPayment> premiumPayments = new ArrayList<>(ext.getPremiumPayments().size());
            summary.setPremiumPayments(premiumPayments);
            payment.getPremiums().stream().forEach(currentPremium -> {
                ext.getPremiumPayments().stream()
                        .filter(p -> p.getPremiumFlag().equals(currentPremium.getPremiumFlag()))
                        .forEach(previousPremium -> {
                            if (previousPremium instanceof PremiumPaymentByMeasurement && currentPremium.getCurrentMeasurement() != null) {
                                PremiumPaymentByMeasurement previous = (PremiumPaymentByMeasurement) previousPremium;
                                PremiumPaymentByMeasurement next = new PremiumPaymentByMeasurement();
                                premiumPayments.add(next);
                                next.setCreatedTime(LocalDateTime.now());
                                next.setPremiumFlag(currentPremium.getPremiumFlag());
                                next.setPreviousMeasurement(previous.getCurrentMeasurement());
                                next.setCurrentMeasurement(currentPremium.getCurrentMeasurement());
                                next.setUnitPrice(currentPremium.getUnitPrice());
                            } else if (previousPremium instanceof PremiumPaymentByDate && currentPremium.getExpiredDate() != null) {
                                PremiumPaymentByDate previous = (PremiumPaymentByDate) previousPremium;
                                PremiumPaymentByDate next = new PremiumPaymentByDate();
                                premiumPayments.add(next);
                                next.setCreatedTime(LocalDateTime.now());
                                next.setPremiumFlag(currentPremium.getPremiumFlag());
                                next.setFromDate(previous.getToDate());
                                next.setToDate(currentPremium.getExpiredDate());
                            }
                        });

            });
            BigDecimal premiumTotal = premiumProcessorDelegate.processAndAccumulate(premiumPayments);
            summary.setTotalPrice(BigDecimal.valueOf(payment.getQuantity()).multiply(summary.getConcertedPrice()).add(premiumTotal).add(summary.getUnsettledPrice()));
        });
    }
}
