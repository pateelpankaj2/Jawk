package com.mpay.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MemberTransactionResponse {

	private Long id;
	private Long memberId;
	private String memberName;
	private String orderNumber;
	private BigDecimal amount;
	private BigDecimal commission;
	private BigDecimal balanceBefore;
	private BigDecimal balanceAfter;
	private Date createdDate;
	private String orderType;
	private String merchantName;
	private Long userId;
	private String userName;
	private String paymentMethod;
	private BigDecimal memberIncome;
}
