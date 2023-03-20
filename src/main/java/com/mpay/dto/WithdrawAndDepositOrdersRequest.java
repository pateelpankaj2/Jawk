package com.mpay.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WithdrawAndDepositOrdersRequest {
	
	private Long id;
	private String type;
	private String orderNumber;
	private BigDecimal amount;
	private String orderType;
	private Long dateCreated;
	private String orderStatus;
	private String transactionNumber;
	private BigDecimal fee;
	private String comment;
	private MerchantBankAccountRequest bankDetails;
}
