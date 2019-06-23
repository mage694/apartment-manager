package com.apartmentmanager.po.apartment.ext;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ApartmentPremiumByMeasurement extends ApartmentPremium {
    private BigDecimal currentMeasurement;
}
