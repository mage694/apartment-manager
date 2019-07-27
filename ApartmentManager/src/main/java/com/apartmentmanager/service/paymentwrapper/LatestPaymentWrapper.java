package com.apartmentmanager.service.paymentwrapper;

import com.apartmentmanager.dao.PaymentHistoryDao;
import com.apartmentmanager.dao.ext.PaymentHistoryExtensionDao;
import com.apartmentmanager.dto.apartment.ApartmentPaymentSummary;
import com.apartmentmanager.dto.customer.CustomerPayment;
import com.apartmentmanager.po.payment.PaymentHistory;
import com.apartmentmanager.po.payment.ext.PremiumPayment;
import com.apartmentmanager.service.datecalculator.ExpiredDateCalculator;
import com.apartmentmanager.service.premiumprocessor.PremiumProcessorDelegate;
import com.apartmentmanager.service.premiumresolver.ApartmentPremiumParamsResolverForPayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Order(2)
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
    @Autowired
    private ApartmentPremiumParamsResolverForPayment resolverForPayment;

    @Override
    public void wrap(Integer apartmentId, Integer customerId, CustomerPayment payment, ApartmentPaymentSummary summary) {
        PaymentHistory latestPayment = paymentHistoryDao.findTopByApartmentIdAndCustomerIdOrderByCreatedTimeDesc(apartmentId, customerId).orElseThrow(IllegalStateException::new);
        summary.setFromDate(latestPayment.getToDate());
        summary.setToDate(dateCalculator.calculate(summary.getFromDate(), summary.getPaymentType(), payment.getQuantity()));

        //calculate premiums
        paymentHistoryExtensionDao.findById(latestPayment.getId()).ifPresent(ext -> {
            summary.setUnsettledPrice(ext.getTotalPrice().subtract(ext.getReceipts()));
            List<? extends PremiumPayment> premiumPayments = ext.getPremiumPayments();
            summary.setPremiumPayments(premiumPayments);
            resolverForPayment.resolve(payment.getPremiums(), ext.getPremiumPayments());
            BigDecimal premiumTotal = premiumProcessorDelegate.processAndAccumulate(premiumPayments);
            summary.setTotalPrice(BigDecimal.valueOf(payment.getQuantity()).multiply(summary.getConcertedPrice()).add(premiumTotal).add(summary.getUnsettledPrice()));
        });
    }
}
