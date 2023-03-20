package com.mpay.dto;


import lombok.Data;

@Data
public class UPIDetailsDTO {
	private Long id;
	private String paymentType;
	private boolean defaultPayment;
	private String mobileNumber;
	private String accountName;

	// UPI fields
	private String upiId;
	private String scannerImg;
	private String upiType;

	// netbanking fields
	private String bankName;
	private String accountNumber;
	private String ifscCode;

	// ewallet fields
	private String ewallet;
}
