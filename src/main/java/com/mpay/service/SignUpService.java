package com.mpay.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.mpay.dto.SignUpRequest;
import com.mpay.enums.WalletType;
import com.mpay.model.Roles;
import com.mpay.model.UserProfile;
import com.mpay.model.Wallet;
import com.mpay.repository.RoleRepository;
import com.mpay.repository.UserRepository;
import com.mpay.repository.WalletRepository;
import com.mpay.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SignUpService {

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	UserRepository userRepository;

	@Autowired
	WalletRepository walletRepository;

	@Autowired
	UserProfileService userProfileService;

	public Map<String, Object> signUp(SignUpRequest signUpRequest) {
		UserProfile profile = new UserProfile();

		Map<String, Object> resultMap = new HashMap<String, Object>();
		UserProfile checkUser = userRepository.findByUsername(signUpRequest.getEmail());
		if (checkUser != null) {
			String message = "User already exists with this email: " + signUpRequest.getEmail();
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", message);
			log.error(message);
			return resultMap;
		}
		try {
			profile.setMemberAcceptOrder(false);
			profile.setFirstName(signUpRequest.getFirstName());
			profile.setLastName(signUpRequest.getLastName());
			profile.setContactNumber(signUpRequest.getContactNumber());
			profile.setEmail(signUpRequest.getEmail());
			profile.setUsername(signUpRequest.getEmail());
			profile.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
			profile.setIsUserActive(true);
			Roles role = roleRepository.findByName(Constants.MEMBER);
			profile.setRole(role);

			UserProfile user = userRepository.save(profile);

			// Save UPI details for this member
			userProfileService.saveOrUpdateUPIDetails(signUpRequest.getUpiInfo(), profile);

			// Create wallet for member
			Wallet wallet = new Wallet();
			wallet.setWalletType(WalletType.MEMBER);
			wallet.setCommissionAmount(BigDecimal.valueOf(0));
			wallet.setBalanceAmount(BigDecimal.valueOf(0));
			wallet.setMember(user);
			walletRepository.save(wallet);
			log.debug("Sign-up user successfully with the email: {}", signUpRequest.getEmail());
			resultMap.put("result", "success");
			resultMap.put("message", "Sign-up user successfully with the email: " + signUpRequest.getEmail());
			return resultMap;
		} catch (Exception e) {
			log.error("Error occurred while sign-up user with the email: " + signUpRequest.getEmail(), e);
			return null;
		}

	}
}
