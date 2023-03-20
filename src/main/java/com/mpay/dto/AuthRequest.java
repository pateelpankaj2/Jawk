package com.mpay.dto;

import lombok.Data;

@Data
public class AuthRequest {

    private String code;
    private String authentication;
    private String grantType;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private String redirectUrls;
    private String scope;

}
