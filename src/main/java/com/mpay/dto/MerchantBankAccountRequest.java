package com.mpay.dto;

import lombok.Data;

@Data
public class MerchantBankAccountRequest {

	private Long id;
	private Long merchantId;
	private String merchantName;
	private String bankName;
	private String accountName;
	private String accountNumber;
	private String approvalStatus;
	private String paymentMethod;
	private String upiId;
	private String emailAddress;
	private String contactNumber;
	private String ifscCode;
	private Long dateCreated;
}
