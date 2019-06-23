package com.apartmentmanager.constant;

import lombok.Getter;

import java.util.stream.Stream;

public enum  ModuleEnum {
    APARTMENT("apartment"), CUSTOMER("customer");

    @Getter
    private String moduleName;

    ModuleEnum(String moduleName) {
        this.moduleName = moduleName;
    }

    public static ModuleEnum getByName(String moduleName) {
        return Stream.of(ModuleEnum.values()).filter(e -> e.moduleName.equals(moduleName))
                .findAny().orElse(null);
    }
}
