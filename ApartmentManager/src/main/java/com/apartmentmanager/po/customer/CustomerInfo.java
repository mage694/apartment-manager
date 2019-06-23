package com.apartmentmanager.po.customer;

import com.apartmentmanager.constant.CustomerStatus;
import com.apartmentmanager.constant.Gender;
import com.apartmentmanager.po.apartment.ApartmentInfo;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Entity
public class CustomerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20)
    private String idNum;

    private String name;

    private LocalDate birthday;

    @Enumerated
    private Gender gender;

    @Column(length = 1, nullable = false)
    @Convert(converter = CustomerStatus.PersistentConverter.class)
    private CustomerStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartmentId")
    private ApartmentInfo apartment;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer")
    private Set<ContactInfo> contacts;

    private Boolean isPrimary;
}
