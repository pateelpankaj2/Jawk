package com.mpay.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SignUpRequest {

	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String username;
	private String password;
	private String contactNumber;
	private List<UPIDetailsDTO> upiInfo;
}
