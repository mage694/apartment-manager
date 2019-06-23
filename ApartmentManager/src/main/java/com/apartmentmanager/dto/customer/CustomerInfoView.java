package com.apartmentmanager.dto.customer;

import com.apartmentmanager.constant.CustomerStatus;
import com.apartmentmanager.constant.Gender;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.apartmentmanager.remote.FileRemoteClient.FileInfo;

@Data
@Builder
public class CustomerInfoView {
    private Integer id;
    private String idNum;
    private String name;
    private LocalDate birthday;
    private Gender gender;
    private Integer apartmentId;
    private String apartmentName;
    private Boolean isPrimary;
    private CustomerStatus status;
    private Set<ContactInfoView> contacts;
    private List<FileInfo> files;
    private LocalDate enrollDate;
}
