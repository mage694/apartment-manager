package com.apartmentmanager.service.datecalculator;

import com.apartmentmanager.constant.PaymentType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Primary
@Service("expiredDateCalculator")
public class ExpiredDateCalculator implements IDateCalculator {

    @Override
    public LocalDate calculate(LocalDate fromDate, PaymentType paymentType, Integer quantity) {
        ChronoUnit unit;
        switch (paymentType) {
            case PER_DAY:
                unit = ChronoUnit.DAYS;
                break;
            case PER_WEEK:
                unit = ChronoUnit.WEEKS;
                break;
            case PER_MONTH:
            default:
                unit = ChronoUnit.MONTHS;
                break;
        }
        return fromDate.plus(quantity, unit);
    }
}
