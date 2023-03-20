package com.mpay.enums;

public enum PaymentMethod {
    UPI("upi"),
    EWALLET("ewallet"),
    NETBANKING("netbanking");

    private String value;
    private PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
