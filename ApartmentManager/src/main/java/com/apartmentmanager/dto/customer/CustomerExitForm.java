package com.apartmentmanager.dto.customer;

import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerExitForm {
    private List<ApartmentPremiumParams> premiums;

    @NotNull(message = "押金不能为空")
    private BigDecimal deposit;

    private BigDecimal compensation;

    @NotNull(message = "退还费用不能为空")
    private BigDecimal refund;

}
