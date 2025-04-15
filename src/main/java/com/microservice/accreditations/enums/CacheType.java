package com.microservice.accreditations.enums;

public enum CacheType {
    POINT_OF_SALE("point-of-sale"),
    ACCREDITATIONS("accreditations");

    private final String value;

    CacheType(String value) {
        this.value = value;
    }

    public String getValues() {
        return value;
    }
}
