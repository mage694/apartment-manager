package com.apartmentmanager.dto.apartment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ApartmentForm implements Serializable {
    @NotBlank(message = "名称不能为空")
    private String name;

    private BigDecimal pricePerDay;
    private BigDecimal pricePerWeek;
    private BigDecimal pricePerMonth;
    private BigDecimal deposit;
    private List<ApartmentPremiumParams> premiums;
    private List<String> fileIds;
    private String description;
}
