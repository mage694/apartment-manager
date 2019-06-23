package com.apartmentmanager.dto.apartment;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApartmentUpdateForm extends ApartmentForm {
    private LocalDate toDate;
}
