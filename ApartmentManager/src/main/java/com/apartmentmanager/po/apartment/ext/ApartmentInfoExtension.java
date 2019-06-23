package com.apartmentmanager.po.apartment.ext;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ApartmentInfoExtension implements Serializable {
    @Id
    @JsonIgnore
    private Integer apartmentId;

    private List<? extends ApartmentPremium> premiums;
}
