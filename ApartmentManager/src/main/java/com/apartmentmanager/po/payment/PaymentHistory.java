package com.apartmentmanager.po.payment;

import com.apartmentmanager.constant.PaymentType;
import com.apartmentmanager.po.apartment.ApartmentInfo;
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
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartmentId")
    private ApartmentInfo apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId")
    private CustomerInfo customer;

    private LocalDateTime createdTime;

    private LocalDate fromDate;

    private LocalDate toDate;

    private BigDecimal unitPrice;

    @Column(nullable = false, length = 1)
    @Convert(converter = PaymentType.PersistentConverter.class)
    private PaymentType paymentType;

    private Integer quantity;
}
