package com.apartmentmanager.constant;

import javax.persistence.AttributeConverter;
import java.util.stream.Stream;

public enum PaymentType {
    PER_DAY("D"), PER_MONTH("M"), PER_WEEK("W"), PER_MEASUREMENT("S");

    private String code;

    PaymentType(String code) {
        this.code = code;
    }

    public static PaymentType getFromCode(String code) {
        return Stream.of(PaymentType.values()).filter(t -> t.code.equals(code) || t.name().equals(code))
                .findAny().orElse(null);
    }

    public String code() {
        return code;
    }

    public static final class PersistentConverter implements AttributeConverter<PaymentType, String> {

        @Override
        public PaymentType convertToEntityAttribute(String dbData) {
            return getFromCode(dbData);
        }

        @Override
        public String convertToDatabaseColumn(PaymentType attribute) {
            return attribute.code;
        }
    }
}
