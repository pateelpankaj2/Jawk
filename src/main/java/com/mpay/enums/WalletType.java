package com.mpay.enums;

public enum WalletType {

	MEMBER("member"), 
	MERCHANT("merchant"),
	PAYMENT_SYSTEM("payment_system");

	private String value;

	private WalletType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
