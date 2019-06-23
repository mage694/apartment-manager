package com.apartmentmanager.dto.customer;

import com.apartmentmanager.constant.ContactType;
import lombok.Data;

@Data
public class ContactInfoView {
    private ContactType contactType;
    private String detail;
}
