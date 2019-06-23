package com.apartmentmanager.constant;

import javax.persistence.AttributeConverter;
import java.util.stream.Stream;

public enum  ApartmentStatus {
    IDLE("I"), OCCUPIED("O"), REMOVED("D"), EXPIRED("E");


    private String code;

    ApartmentStatus(String code) {
        this.code = code;
    }

    public static ApartmentStatus getFromCode(String code) {
        return Stream.of(ApartmentStatus.values()).filter(t -> t.code.equals(code) || t.name().equals(code))
                .findAny().orElseThrow(IllegalArgumentException::new);
    }

    public String  code() {
        return code;
    }

    public static final class PersistentConverter implements AttributeConverter<ApartmentStatus, String> {

        @Override
        public String convertToDatabaseColumn(ApartmentStatus attribute) {
            return attribute.code;
        }

        @Override
        public ApartmentStatus convertToEntityAttribute(String dbData) {
            return getFromCode(dbData);
        }
    }
}
