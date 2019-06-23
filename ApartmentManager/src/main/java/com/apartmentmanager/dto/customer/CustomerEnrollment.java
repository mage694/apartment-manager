package com.apartmentmanager.dto.customer;

import com.apartmentmanager.constant.Gender;
import com.apartmentmanager.dto.apartment.ApartmentPremiumParams;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class CustomerEnrollment implements Serializable {
    @NotNull(message = "价格不能为空")
    private BigDecimal concertedPrice;

    @NotNull(message = "入住日期不能为空")
    private LocalDate enrollDate;

    @NotBlank(message = "支付方式不能为空")
    @Size(max = 1, message = "支付方式不合法")
    private String paymentType;

    @NotNull(message = "支付数量不能为空")
    private Integer quantity;

    @NotNull(message = "实收款不能为空")
    private BigDecimal receipts;

    @NotNull(message = "押金不能为空")
    private BigDecimal deposit;

    @NotNull(message = "租客不能为空")
    @Size(min = 1, message = "至少需要一名租客")
    private List<Customer> customers;

    private List<ApartmentPremiumParams> premiums;

    @Data
    public static class Customer implements Serializable {

        @NotBlank(message = "身份证号不能为空")
        @Pattern(regexp = "^\\d+(x|X)?", message = "身份证不合法")
        private String idNum;

        @NotBlank(message = "姓名不能为空")
        @Size(min = 2, message = "姓名长度需大于2")
        private String name;

        @Past(message = "生日不合法")
        private LocalDate birthday;

        @NotNull(message = "性别不能为空")
        private Gender gender;
        private Map<String, String> contacts;
        private Boolean isPrimary;
        private List<String> fileIds;
    }
}
