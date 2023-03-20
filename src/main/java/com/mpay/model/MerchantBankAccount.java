package com.mpay.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "MERCHANT_BANK_ACCOUNT")
public class MerchantBankAccount extends TrackChangeEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "MERCHANT_BANK_ACCOUNT_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long merchantBankAccountId;

	@Column(name = "BANK_NAME")
	private String bankName;

	@Column(name = "ACCOUNT_NAME")
	private String accountName;

	@Column(name = "ACCOUNT_NUMBER")
	private String accountNumber;

	@Column(name = "APPROVAL_STATUS")
	private String approvalStatus;

	@Column(name = "PAYMENT_METHOD")
	private String paymentMethod;

	@Column(name = "UPI_ID")
	private String upiId;

	@Column(name = "EMAIL_ADDRESS")
	private String emailAddress;

	@Column(name = "CONTACT_NUMBER")
	private String contactNumber;

	@Column(name = "IFSC_CODE")
	private String ifscCode;

	@JoinColumn(name = "MERCHANT_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Merchant merchant;

}
