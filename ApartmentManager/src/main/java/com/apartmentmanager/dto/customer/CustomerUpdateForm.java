package com.apartmentmanager.dto.customer;

import com.apartmentmanager.constant.Gender;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class CustomerUpdateForm implements Serializable {
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

    private List<String> fileIds;
    private Map<String, String> contacts;
}
