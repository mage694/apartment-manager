package com.apartmentmanager.dto.customer;

import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerPayment implements Serializable {
    @NotNull(message = "支付数量不能为空")
    @Range(min = 1, message = "支付数量需大于1")
    private Integer quantity;

    private List<ApartmentPremiumParams> premiums;

    @NotNull(message = "实收款不能为空")
    private BigDecimal receipts;
}
