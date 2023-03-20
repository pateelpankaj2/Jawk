package com.mpay.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table (name = "MERCHANT")
@EqualsAndHashCode(callSuper=false)
//@Where(clause = "IS_DELETED IS NULL OR IS_DELETED = false")
@SQLDelete(sql = "UPDATE MERCHANT SET is_deleted = true, date_modified = now() WHERE MERCHANT_ID = ?") // Soft delete
public class Merchant extends TrackChangeEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "MERCHANT_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long merchantId;

	@Column(name = "NAME")
	private String name;

	@Column(name = "WEBSITE")
	private String website;

	@Column(name = "API_USERNAME")
	private String apiUsername;

	@Column(name = "CONTACT_NUMBER")
	private String contactNumber;

	@Column(name = "API_PASSWORD")
	private String apiPassword;

	@Column(name = "WEBHOOK_URL")
	private String webhookURL;

	@Column(name = "WEBHOOK_USERNAME")
	private String webhookUsername;

	@Column(name = "WEBHOOK_PASSWORD")
	private String webhookPassword;

	@Column(name = "MERCHANT_STATUS")
	private Boolean merchantStatus;

	@Column(name = "MERCHANT_MANUAL_SETTLEMENT")
	private Boolean merchantManualSettlement;

	@Column(name = "MANUAL_SETTLEMENT_MAXIMUM_AMOUNT")
	private BigDecimal manualSettlementMaxAmount;

	@Column(name = "TOP_UP_RATE")
	private BigDecimal topUpRate;

	@Column(name = "FIRST_DEPOSIT_RATE")
	private BigDecimal firstDepositRate;

	@Column(name = "WITHDRAWAL_RATE")
	private BigDecimal withdrawalRate;

	@Column(name = "PAYIN_RATE")
	private BigDecimal payInRate;

	@Column(name = "PAYOUT_RATE")
	private BigDecimal payOutRate;
}
