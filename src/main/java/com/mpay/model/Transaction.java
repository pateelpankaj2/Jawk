package com.mpay.model;

import com.mpay.enums.TransactionType;
import com.mpay.enums.WalletType;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "TRANSACTION")
@Getter
@Setter
public class Transaction extends TrackChangeEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "TRANSACTION_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long transactionId;

	@Column(name = "TRANSACTION_TYPE")
	@Enumerated(value = EnumType.STRING)
	private TransactionType transactionType;

	@Column(name = "TRANSACTION_METHOD")
	private String transactionMethod;

	@Column(name = "COMMISSION_AMOUNT")
	private BigDecimal commissionAmount;

	@Column(name = "FEE")
	private BigDecimal fee;

	@Column(name = "BALANCE_BEFORE")
	private BigDecimal balanceBefore;

	@Column(name = "BALANCE_AFTER")
	private BigDecimal balanceAfter;

	@JoinColumn(name = "MEMBER_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private UserProfile member;

	@JoinColumn(name = "MERCHANT_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Merchant merchant;

	@Column(name = "WALLET_TYPE")
	@Enumerated(value = EnumType.STRING)
	private WalletType walletType;

	@JoinColumn(name = "ORDER_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Order order;
}
