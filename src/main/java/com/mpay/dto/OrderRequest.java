package com.mpay.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderRequest {
	private Long id;
	private String type;
	private String orderNumber;
	private BigDecimal amount;
	private BigDecimal paymentAmount;
	private String orderType;
	private String paymentMethod;
	private Long userId;
	private Long memberId;
	private Long merchantId;
	private String merchantName;
	private Long dateCreated;
	private String orderStatus;
	private String transactionNumber;
	private UserRequest user;
	private UserRequest member;
	private BigDecimal merchantFee;
	private BigDecimal merchantIncome;
	private String comment;
	private String rejectionComment;
	private String settlementPassword;
	private Long accountId;
	private String transactionReceipt;
	private Long completedDate;
	private String settlementType;
	private String paymentStatus;
	private String accountInfo;
}
