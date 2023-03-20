package com.mpay.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mpay.dto.SignUpRequest;
import com.mpay.service.SignUpService;

@RestController
@RequestMapping(value = "/api")
public class SignUpController {

	@Autowired
	SignUpService signUpService;

	@PostMapping("/sign-up")
	public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {

		Map<String, Object> signUp = signUpService.signUp(signUpRequest);
		if (signUp.containsKey("errorMessage")) {
			return new ResponseEntity<>(signUp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(signUp, HttpStatus.OK);
	}
}
