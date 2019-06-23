package com.apartmentmanager.constant;

import java.util.stream.Stream;

public enum ApartmentPremiumType {
    ELECTRICITY("E", "电费"), WATER("W", "水费"), MANAGEMENT("M", "物业费"), GAS("G", "煤气费");

    private String code;
    private String desc;


    ApartmentPremiumType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ApartmentPremiumType fromCode(String code) {
        return Stream.of(ApartmentPremiumType.values()).filter(t -> t.code.equals(code) || t.name().equals(code)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
