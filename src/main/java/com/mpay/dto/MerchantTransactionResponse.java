package com.mpay.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantTransactionResponse {

	private Long id;
	private String merchant;
	private String orderNumber;
	private BigDecimal amount;
	private BigDecimal commission;
	private BigDecimal balanceBefore;
	private BigDecimal balanceAfter;
	private Date createdDate;
	private String orderType;
	private UserRequest user;
	private UserRequest member;
	private BigDecimal merchantFee;
	private BigDecimal merchantIncome;
	private String paymentMethod;
	private Long userId;
	private String userName;
}
