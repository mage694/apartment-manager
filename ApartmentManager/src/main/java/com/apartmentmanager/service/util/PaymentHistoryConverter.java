package com.apartmentmanager.service.util;

import com.apartmentmanager.po.payment.PaymentHistory;
import com.apartmentmanager.dto.apartment.LatestPaymentView;

public final class PaymentHistoryConverter {
    public static LatestPaymentView toLatestPaymentView(PaymentHistory paymentHistory) {
        return LatestPaymentView.builder().id(paymentHistory.getId())
                .toDate(paymentHistory.getToDate()).build();

    }

}
