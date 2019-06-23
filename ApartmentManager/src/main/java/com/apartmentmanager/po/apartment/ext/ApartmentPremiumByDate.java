package com.apartmentmanager.po.apartment.ext;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ApartmentPremiumByDate extends ApartmentPremium {
    private LocalDate expiredDate;

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }
}
