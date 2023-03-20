package com.mpay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionResource {
    private Long userId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String contactNumber;
    private Long roleId;
    private String roleName;
    private Long merchantId;
    private String merchantName;
}
