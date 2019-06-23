package com.apartmentmanager.constant;

import javax.persistence.AttributeConverter;
import java.util.stream.Stream;

public enum CustomerStatus {
    ENROLLED("I"), EXITED("O"), REMOVED("D");

    private String code;

    CustomerStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static CustomerStatus getFromCode(String code) {
        return Stream.of(CustomerStatus.values()).filter(s -> s.code.equals(code) || s.name().equals(code))
                .findAny().orElseThrow(IllegalArgumentException::new);
    }

    public static class PersistentConverter implements AttributeConverter<CustomerStatus, String> {
        @Override
        public CustomerStatus convertToEntityAttribute(String dbData) {
            return getFromCode(dbData);
        }

        @Override
        public String convertToDatabaseColumn(CustomerStatus attribute) {
            return attribute.code;
        }
    }

}
