package com.apartmentmanager.po.enrollment;

import com.apartmentmanager.po.apartment.ApartmentInfo;
import com.apartmentmanager.po.apartment.ApartmentPrice;
import com.apartmentmanager.po.customer.CustomerInfo;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class EnrollmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal deposit;

    private LocalDate enrollDate;

    private LocalDate expiredDate;

    private LocalDateTime createTime;

    @ManyToOne
    @JoinColumn(name = "priceId", nullable = false)
    private ApartmentPrice originalPrice;

    private BigDecimal concertedPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartmentId")
    private ApartmentInfo apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId")
    private CustomerInfo customer;
}
