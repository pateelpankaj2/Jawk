package com.mpay.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "MERCHANT_SETTLEMENT")
@Data
@EqualsAndHashCode(callSuper = false)
public class MerchantSettlement extends TrackChangeEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SETTLEMENT_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    @JoinColumn(name = "MERCHANT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Merchant merchant;

    @JoinColumn(name = "MEMBER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserProfile member;

    @JoinColumn(name = "UPI_DETAIL_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private UPIDetail upiDetail;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    //@Column(name = "PAYOUT_FEE")
    //private Float payoutFee;
}
