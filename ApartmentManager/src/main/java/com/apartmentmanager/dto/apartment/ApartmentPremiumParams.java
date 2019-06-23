package com.apartmentmanager.dto.apartment;

import com.apartmentmanager.constant.ApartmentPremiumType;
import com.apartmentmanager.constant.PaymentType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ApartmentPremiumParams implements Serializable {
    @NotBlank(message = "额外费用标识不能为空")
    private String premiumFlag;

    @NotNull(message = "支付方式不能为空")
    private PaymentType paymentType;

    @NotNull(message = "额外费用方式不能为空")
    private ApartmentPremiumType premiumType;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    private BigDecimal currentMeasurement;
    private LocalDate expiredDate;
    private Boolean selected = Boolean.TRUE;    //Default to select all premiums

    public void setPaymentType(String code) {
        this.paymentType = PaymentType.getFromCode(code);
    }

    public void setPremiumType(String code) {
        this.premiumType = ApartmentPremiumType.fromCode(code);
    }
}
