package com.mpay.enums;

public enum OrderType {
    PAYIN("payin"),
    PAYOUT("payout"),
    WITHDRAW("withdraw"),
    DEPOSIT("deposit");

    private String value;
    private OrderType(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
