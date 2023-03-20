package com.mpay.model;

import com.mpay.enums.OrderStatus;
import com.mpay.enums.OrderType;
import com.mpay.enums.PaymentMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "ORDERS")
@Data
@EqualsAndHashCode(callSuper=false)
public class Order extends TrackChangeEntity {

	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "ORDER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(name = "ORDER_NUMBER")
    private String orderNumber;

    @Column(name = "ORDER_TYPE")
    @Enumerated(value = EnumType.STRING)
    private OrderType orderType;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @JoinColumn(name = "MERCHANT_ID")
	@ManyToOne(fetch = FetchType.LAZY)
    private Merchant merchant;

    @JoinColumn(name = "USER_ID")
	@ManyToOne(fetch = FetchType.LAZY)
    private UserProfile user;

    @JoinColumn(name = "MEMBER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserProfile member;

    @Column(name = "ORDER_STATUS")
    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "PAYMENT_METHOD")
    @Enumerated(value = EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "TRANSACTION_NUMBER")
    private String transactionNumber;

    @Column(name = "REJECTION_COMMENT")
    private String rejectionComment;

	@Column(name = "COMMENT")
	private String comment;

	@Column(name = "ADMIN_COMMENT")
	private String adminComment;

	@JoinColumn(name = "BANK_ACCOUNT_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private MerchantBankAccount merchantBankAccount;

	@Column(name = "RECEIPT_URL")
	private String receiptUrl;

	@Column(name = "DATE_COMPLETED")
	private Timestamp dateCompleted;

	@Column(name = "SETTLEMENT_TYPE")
	private String settlementType;

	@JoinColumn(name = "UPI_ACCOUNT_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private UPIDetail upiDetail;
}
