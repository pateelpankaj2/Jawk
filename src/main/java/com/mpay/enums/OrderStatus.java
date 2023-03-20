package com.mpay.enums;

public enum OrderStatus {
    ASSIGNED("assigned"),
    COMPLETED("completed"),
	PENDING("pending"),
    PENDING_REVIEW("pending review"),
    REJECTED("rejected");

    private String value;
    private OrderStatus(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
