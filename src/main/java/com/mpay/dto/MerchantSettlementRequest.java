package com.mpay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantSettlementRequest {
    private long merchantId;
    private String merchant;
    private long upiDetailId;
    private String member;
    private BigDecimal amount;
    private long memberId;

    UPIDetailsDTO upiDetails;
}
