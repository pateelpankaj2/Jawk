package com.mpay.controller;

import com.mpay.dto.MyAccountResponse;
import com.mpay.dto.UserRequest;
import com.mpay.exceptions.AccessControlException;
import com.mpay.model.UserProfile;
import com.mpay.repository.UserRepository;
import com.mpay.service.CommonService;
import com.mpay.service.RoleService;
import com.mpay.service.UserProfileService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/users")
public class UserController {

    @Autowired
    UserProfileService userProfileService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CommonService commonService;

    @Autowired
    RoleService roleservice;

    @GetMapping("/{id}")
	public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
		UserProfile user = userRepository.findById(id).get();
		UserRequest userRequest = new UserRequest();
		commonService.setUserDetails(userRequest, user, false);
		return new ResponseEntity<>(userRequest, HttpStatus.OK);
	}

    @GetMapping
    public ResponseEntity<?> getAllUsers() throws AccessControlException {
        //UserContext.assertIsAdmin();
        return new ResponseEntity<>(userProfileService.getAllUsers(), HttpStatus.OK);
    }

	@GetMapping("/get-roles")
	public ResponseEntity<?> getAllRoles() {
		return new ResponseEntity<>(roleservice.getAllRoles(), HttpStatus.OK);
	}

	@PostMapping("/create-profile")
	public ResponseEntity<?> CreateProfile(@RequestBody UserRequest userRequest) {

		Map<String, Object> resultMap = userProfileService.createProfile(userRequest);
		if (resultMap.containsKey("errorMessage")) {
			return new ResponseEntity<>(resultMap, HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}

	@PutMapping(value = "/update-member_accept_order_status/{id}")
	public ResponseEntity<Object> updateMemberAcceptOrderStatus(@PathVariable("id") Long id, @RequestBody UserRequest userRequest) {
		Map<String, Object> resultMap = userProfileService.updateMemberAcceptOrderStatus(id, userRequest);
		if (resultMap.containsKey("errorMessage")) {
			return new ResponseEntity<>(resultMap, HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}

	@PutMapping(value = "/update-profile/{id}")
	public ResponseEntity<Object> updateProfile(@PathVariable("id") Long id, @RequestBody UserRequest userRequest) {
		Map<String, Object> resultMap = userProfileService.updateProfile(id, userRequest);
		if (resultMap.containsKey("errorMessage")) {
			return new ResponseEntity<>(resultMap, HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}

	@DeleteMapping(value = "/delete-profile/{id}")
	public ResponseEntity<Object> deleteMember(@PathVariable("id") Long id) {
		Map<String, Object> resultMap = userProfileService.deleteProfile(id);
		if (resultMap.containsKey("errorMessage")) {
			return new ResponseEntity<>(resultMap, HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}

	@GetMapping("/min-details")
	public ResponseEntity<?> getAllMember(@RequestParam("type") String rolename) {
		List<Object> allMembers = userProfileService.getAllMembers(rolename);
		return new ResponseEntity<>(allMembers, HttpStatus.OK);
	}

	@PostMapping("/change-password/{id}")
	public ResponseEntity<?> changePassword(@PathVariable("id") Long id, @RequestBody UserRequest userRequest) {
		Map<String, Object> result = userProfileService.changePassword(id, userRequest);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/upi-details")
	public ResponseEntity<?> getUserUPI(@RequestParam("id") Long userId) {
		Map<String, Object> userUPI = userProfileService.getUserUPI(userId);
		return new ResponseEntity<>(userUPI, HttpStatus.OK);
	}

	@GetMapping("/get-sub-accounts")
	public ResponseEntity<?> getSubAccount(@RequestParam("id") Long id) {
		List<UserRequest> subAccounts = userProfileService.getSubAccounts(id);
		return new ResponseEntity<>(subAccounts, HttpStatus.OK);
	}

	@GetMapping("/my-account")
	public ResponseEntity<?> getMyAccountDetails(@RequestParam("id") Long id, @RequestParam("type") String type) {
		MyAccountResponse myAccount = userProfileService.getMyAccountDetails(id, type);
		return new ResponseEntity<>(myAccount, HttpStatus.OK);
	}

	@PostMapping("/change-settlement-password/{id}")
	public ResponseEntity<?> changeSettlementPassword(@PathVariable("id") Long id, @RequestBody UserRequest userRequest) {
		Map<String, Object> result = userProfileService.changeSettlementPassword(id, userRequest);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
