package com.apartmentmanager.dto.apartment;

import com.apartmentmanager.constant.ApartmentStatus;
import com.apartmentmanager.constant.PaymentType;
import com.apartmentmanager.po.apartment.ext.ApartmentPremium;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.apartmentmanager.remote.FileRemoteClient.FileInfo;

@Data
@Builder
public class ApartmentInfoView implements Serializable {
    private Integer apartmentId;
    private String name;
    private ApartmentStatus status;
    private String description;
    private Map<PaymentType, BigDecimal> prices;
    private PaymentType chosenPaymentType;
    private BigDecimal concertedPrice;
    private BigDecimal deposit;
    private Integer primaryCustomerId;
    private String primaryCustomerName;
    private LatestPaymentView latestPayment;
    private List<? extends ApartmentPremium> premiums; //will be populated only if latest payment does not exist
    private List<FileInfo> files;
}
