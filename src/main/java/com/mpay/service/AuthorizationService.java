package com.mpay.service;

import com.mpay.dto.AuthRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthorizationService {

    private String accessToken;

    public String fetchToken(AuthRequest authRequest) throws IOException {
        OkHttpClient client = buildHttpClient();
        Request request = buildPOSTRequest(authRequest);
        Response response = client.newCall(request).execute();
        String responseBody = response.body() != null ? response.body().string() : null;
        if (response.code() != 200) {
            throw new RuntimeException("Unauthorized " + responseBody);
        } else {
            extractToken(responseBody);
        }
        return responseBody;
    }


    OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private Request buildPOSTRequest(AuthRequest authRequest) {
        return new Request.Builder()
                .url(authRequest.getAuthorizationEndpoint())
                .post(getFormBody(authRequest))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
    }

    private RequestBody getFormBody(AuthRequest authRequest) {
        return new FormBody.Builder()
                .add("code", authRequest.getCode())
                .add("redirect_uri", authRequest.getRedirectUrls())
                .add("client_id", authRequest.getClientId())
                .add("client_secret", authRequest.getClientSecret())
                .add("scope", authRequest.getScope())
                .add("grant_type", authRequest.getGrantType())
                .build();
    }

    private void extractToken(String token) {
        JSONObject tokenInfo = new JSONObject(token);
        if (tokenInfo.has("access_token")) {
            accessToken = (String) tokenInfo.get("access_token");
        }
    }
}
