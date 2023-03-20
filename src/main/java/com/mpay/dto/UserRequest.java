package com.mpay.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
public class UserRequest {
	private Long id;
	private String firstName;
	private String lastName;
	private String gender;
	private BigDecimal singlePayoutMinAmount;
	private BigDecimal singlePayoutMaxAmount;
	private BigDecimal dailyPayoutLimitAmount;
	private String fullName;
	private String emailAddress;
	private String password;
	private String oldPassword;
	private String role;
	private Long roleId;
	private Long merchantId;
	private String merchantName;
	private String contactNumber;
	private List<UPIDetailsDTO> upiInfo;
	private Boolean isSubaccount;
	private Long dateCreated;
	private Boolean status;
	private String settlementPassword;
	private String oldSettlementPassword;
	private boolean memberAcceptOrder;

}
