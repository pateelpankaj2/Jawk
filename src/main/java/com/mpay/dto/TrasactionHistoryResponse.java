package com.mpay.dto;

import java.math.BigDecimal;

import com.mpay.enums.OrderType;

import lombok.Data;

@Data
public class TrasactionHistoryResponse {

	private Long id;
	private BigDecimal preBalance;
	private BigDecimal currentBalance;
	private BigDecimal amount;
	private String details;
	private BigDecimal income;
	private Long dateCreated;
	private OrderType orderType;
}
