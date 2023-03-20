package com.mpay.model;

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
import com.mpay.enums.WalletType;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "WALLET")
@Getter
@Setter
public class Wallet extends TrackChangeEntity {

	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "WALLET_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @Column(name = "BALANCE_AMOUNT", nullable = false, columnDefinition="numeric(19,2) default 0.0")
    private BigDecimal balanceAmount;

    @Column(name = "COMMISSION_AMOUNT", nullable = false, columnDefinition="numeric(19,2) default 0.0")
    private BigDecimal commissionAmount;

    @Column(name = "DATE")
    private Date date;

	@JoinColumn(name = "MEMBER_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private UserProfile member;

    @JoinColumn(name = "MERCHANT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Merchant merchant;

    @Column(name ="WALLET_TYPE")
    @Enumerated(value = EnumType.STRING)
    private WalletType walletType;

}
