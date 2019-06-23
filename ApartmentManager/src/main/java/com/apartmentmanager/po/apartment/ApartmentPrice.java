package com.apartmentmanager.po.apartment;

import com.apartmentmanager.constant.PaymentType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ApartmentPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Convert(converter = PaymentType.PersistentConverter.class)
    @Column(length = 1, nullable = false)
    private PaymentType paymentType;

    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartmentId")
    private ApartmentInfo apartment;

    public ApartmentPrice(ApartmentInfo apartment, PaymentType paymentType, BigDecimal price) {
        this.apartment = apartment;
        this.paymentType = paymentType;
        this.price = price;
    }
}
