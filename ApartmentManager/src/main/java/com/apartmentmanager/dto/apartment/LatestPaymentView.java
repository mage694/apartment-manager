package com.apartmentmanager.dto.apartment;

import com.apartmentmanager.po.payment.ext.PaymentHistoryExtension;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class LatestPaymentView implements Serializable {
    @JsonIgnore
    private Integer id;
    private LocalDate toDate;
    private PaymentHistoryExtension extension;
}
