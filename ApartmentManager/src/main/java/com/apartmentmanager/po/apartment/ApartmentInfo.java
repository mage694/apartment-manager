package com.apartmentmanager.po.apartment;

import com.apartmentmanager.constant.ApartmentStatus;
import com.apartmentmanager.po.customer.CustomerInfo;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
//@Table(indexes = {@Index(name = "user_id_index", columnList = "userId")})
public class ApartmentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "apartment")
    private Set<ApartmentPrice> prices;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "apartment")
    private Set<CustomerInfo> customers;

    @Convert(converter = ApartmentStatus.PersistentConverter.class)
    @Column(length = 1, nullable = false)
    private ApartmentStatus status;

    private LocalDateTime createdTime;

    private String description;

    @Column(length = 32)
    private String userId;

}
