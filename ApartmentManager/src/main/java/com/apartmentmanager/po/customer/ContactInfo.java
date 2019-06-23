package com.apartmentmanager.po.customer;

import com.apartmentmanager.constant.ContactType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class ContactInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 1)
    @Convert(converter = ContactType.PersistentConverter.class)
    private ContactType contactType;

    private String detail;

    @ManyToOne
    @JoinColumn(name = "customerId")
    @JsonIgnore
    private CustomerInfo customer;
}
