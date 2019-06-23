package com.apartmentmanager.dto.apartment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ApartmentPaymentView implements Serializable {
    private Integer paymentId;
    private Integer apartmentId;
    private Integer customerId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime createdTime;
}
