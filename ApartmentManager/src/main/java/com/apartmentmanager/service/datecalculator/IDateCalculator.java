package com.apartmentmanager.service.datecalculator;

import com.apartmentmanager.constant.PaymentType;

import java.time.LocalDate;

public interface IDateCalculator {
    LocalDate calculate(LocalDate fromDate, PaymentType paymentType, Integer quantity);
}
