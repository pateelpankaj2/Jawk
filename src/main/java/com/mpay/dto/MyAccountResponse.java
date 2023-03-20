package com.mpay.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class MyAccountResponse {

	private Long id;
	private String fullName;
	private String emailAddress;
	private String contactNumber;
	private Long dateCreated;
	private BigDecimal balance;
}
