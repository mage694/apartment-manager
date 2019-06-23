package com.apartmentmanager.constant;

import javax.persistence.AttributeConverter;
import java.util.stream.Stream;

public enum ContactType  {
    EMAIL("E"), PHONE("P");

    private String code;

    ContactType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ContactType getFromCode(String code) {
        return Stream.of(ContactType.values()).filter(t -> code.equals(t.code) || code.equals(t.name()))
                .findAny().orElseThrow(IllegalArgumentException::new);
    }

    public static final class PersistentConverter implements AttributeConverter<ContactType, String> {

        @Override
        public String convertToDatabaseColumn(ContactType attribute) {
            return attribute.code;
        }

        @Override
        public ContactType convertToEntityAttribute(String dbData) {
            return getFromCode(dbData);
        }
    }
}