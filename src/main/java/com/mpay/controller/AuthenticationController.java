package com.mpay.controller;

import com.mpay.dto.AuthRequest;
import com.mpay.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthenticationController {

    @Autowired
    AuthorizationService authorizationService;

    @PostMapping(value = "/get-token", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getAccessToken(@RequestBody AuthRequest authRequest) throws IOException {
        return ResponseEntity.ok().body(authorizationService.fetchToken(authRequest));
    }

}
