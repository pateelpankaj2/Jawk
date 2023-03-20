package com.mpay.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MerchantRequest {

	private String webhookURL;
	private String webhookUsername;
	private String webhookPassword;

	private Long merchantId;
	private String name;
	private String firstName;
	private String lastName ;
	private String loginEmail;
	private String loginPassword;
	private String website;
	private String apiUsername;
	private String apiPassword;
	private String contactNumber;
	private String settlementPassword;
	private Boolean merchantManualSettlement;
	private BigDecimal manualSettlementMaxAmount;
	private BigDecimal topUpRate;
	private Boolean merchantStatus;
	private BigDecimal firstDepositRate;
	private BigDecimal withdrawalRate;
	private BigDecimal payInRate;
	private BigDecimal payOutRate;
}
