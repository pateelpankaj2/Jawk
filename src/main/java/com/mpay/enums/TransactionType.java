package com.mpay.enums;

public enum TransactionType {
    DEBIT("debit"),
    CREDIT("credit"),
    WITHDRAW("withdraw"),
    DEPOSIT("deposit");

    private String value;
    private TransactionType(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }

}
