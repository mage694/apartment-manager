package com.apartmentmanager.po.apartment.ext;

import com.apartmentmanager.constant.ApartmentPremiumType;
import com.apartmentmanager.constant.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class ApartmentPremium implements Serializable {
    private String premiumFlag;
    private PaymentType paymentType;
    private ApartmentPremiumType premiumType;
    private BigDecimal unitPrice;
    private Boolean selected = Boolean.TRUE;    //Default to select all premiums
}
