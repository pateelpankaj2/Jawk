package com.mpay.service;

import com.mpay.dto.MyAccountResponse;
import com.mpay.dto.UPIDetailsDTO;
import com.mpay.dto.UserRequest;
import com.mpay.enums.PaymentMethod;
import com.mpay.enums.WalletType;
import com.mpay.model.Merchant;
import com.mpay.model.Roles;
import com.mpay.model.UPIDetail;
import com.mpay.model.UserProfile;
import com.mpay.model.Wallet;
import com.mpay.repository.MerchantRepository;
import com.mpay.repository.RoleRepository;
import com.mpay.repository.UPIDetailRepository;
import com.mpay.repository.UserRepository;
import com.mpay.repository.WalletRepository;
import com.mpay.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Transactional
@Slf4j
public class UserProfileService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UPIDetailRepository upiDetailRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	UPIDetailRepository upiDetailsRepository;
	
	@Autowired
	MerchantRepository merchantRepository;

	@Autowired
	WalletRepository walletRepository;

	public UserProfile saveOrUpdateUser(UserProfile user) {
		user = userRepository.save(user);
		return user;
	}

	public Optional<UserProfile> getUserProfile(Long userProfileId) {
		return userRepository.findById(userProfileId);
	}

	public UserProfile deleteUserProfile(Long userProfileId) {
		Optional<UserProfile> user = userRepository.findById(userProfileId);
		if (user.isPresent()) {
			user.get().setIsUserActive(false);
			user.get().setUsername(user.get().getUsername()
					+ new SimpleDateFormat("MMddyyyy").format(new Timestamp(System.currentTimeMillis())));
			return user.get();
		}
		return null;
	}

	public UserProfile findByUserName(String username) {
		return userRepository.findByUsername(username);
	}

	public List<UserRequest> getAllUsers() {
		List<UserRequest> userList = new ArrayList<>();
		List<UserProfile> userProfiles = userRepository.findAll();
		if (CollectionUtils.isNotEmpty(userProfiles)) {
			userProfiles.stream().forEach(userProfile -> {
				UserRequest user = new UserRequest();
				user.setId(userProfile.getUserId());
				String roleName = userProfile.getRole().getName();
				user.setRole(userProfile.getRole().getName());
				user.setRoleId(userProfile.getRole().getRoleId());
				user.setEmailAddress(userProfile.getEmail());
				user.setFirstName(userProfile.getFirstName());
				user.setLastName(userProfile.getLastName());
				user.setContactNumber(userProfile.getContactNumber());

				if (roleName.equals(Constants.MERCHANT_ADMIN) && userProfile.getMerchant() != null) {
					user.setMerchantId(userProfile.getMerchant().getMerchantId());
				}

				if (roleName.equals(Constants.MEMBER)) {
					List<UPIDetail> upiDetails = userProfile.getUPIDetails();
					List<UPIDetailsDTO> detailsDTOs = new ArrayList<>();
					if (CollectionUtils.isNotEmpty(upiDetails)) {
						upiDetails.stream().forEach(upiDetail -> {
							UPIDetailsDTO detailsDTO = new UPIDetailsDTO();
							detailsDTO.setId(upiDetail.getUpiDetailsId());
							detailsDTO.setUpiId(upiDetail.getUpi());
							if (upiDetail.getUpiScanner() != null) {
								String scannerImg = Base64.getEncoder().encodeToString(upiDetail.getUpiScanner());
								detailsDTO.setScannerImg(scannerImg);
							}
							detailsDTOs.add(detailsDTO);
						});
					}
					user.setUpiInfo(detailsDTOs);
				}
				userList.add(user);
			});
		}
		return userList;
	}

	public Map<String, Object> createProfile(UserRequest userRequest) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		UserProfile checkUser = userRepository.findByUsername(userRequest.getEmailAddress());
		if (checkUser != null) {
			String message = "User already exists with this username: " + userRequest.getEmailAddress();
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", message);
			log.error(message);
			return resultMap;
		}

		UserProfile user = new UserProfile();
		user.setUsername(userRequest.getEmailAddress());
		user.setEmail(userRequest.getEmailAddress());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setContactNumber(userRequest.getContactNumber());
		user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		user.setGender(userRequest.getGender());
		user.setIsUserActive(true);
		String roleName = null;
		if (userRequest.getRoleId() != null) {
			Optional<Roles> role = roleRepository.findById(userRequest.getRoleId());
			if (role.isPresent()) {
				user.setRole(role.get());
				roleName = role.get().getName();
			}
		} else {
			Roles role = roleRepository.findByName(Constants.MERCHANT_SUBACCOUNT);
			user.setRole(role);
			if (userRequest.getMerchantId() != null) {
				UserProfile superUser = userRepository.getSuperUserByMerchantId(userRequest.getMerchantId());
				Merchant merchant = superUser.getMerchant();
				user.setMerchant(merchant);
				user.setSuperUserId(superUser);
			} else {
				throw new RuntimeException("MerchantId is required");
			}
		}

		if (Constants.MERCHANT_ADMIN.equals(roleName) && userRequest.getMerchantId() != null) {
			Optional<Merchant> checkMerchant = merchantRepository.findById(userRequest.getMerchantId());
			if (checkMerchant.isPresent()) {
				user.setMerchant(checkMerchant.get());
			}
		}

		if (Constants.MEMBER.equals(roleName)) {
			user.setDailyPayoutLimitAmount(userRequest.getDailyPayoutLimitAmount());
			user.setSinglePayoutMaxAmount(userRequest.getSinglePayoutMaxAmount());
			user.setSinglePayoutMinAmount(userRequest.getSinglePayoutMinAmount());
			if (userRequest.getSettlementPassword() != null) {
				user.setSettlementPassword(passwordEncoder.encode(userRequest.getSettlementPassword()));
			}
		}

		userRepository.save(user);

		if (Constants.MEMBER.equals(roleName)) {
			saveOrUpdateUPIDetails(userRequest.getUpiInfo(), user);

			// create wallet for merchant
			Wallet wallet = new Wallet();
			wallet.setWalletType(WalletType.MEMBER);
			wallet.setCommissionAmount(BigDecimal.valueOf(0));
			wallet.setBalanceAmount(BigDecimal.valueOf(0));
			wallet.setMember(user);
			walletRepository.save(wallet);
		}

		log.debug("Created User profile successfully with the email: {}", userRequest.getEmailAddress());
		resultMap.put("result", "success");
		return resultMap;
	}

	public void saveOrUpdateUPIDetails(List<UPIDetailsDTO> upiInfo, UserProfile user) {

		List<UPIDetail> list = user.getUPIDetails();
		List<UPIDetail> toDelete = new ArrayList<>();

		if (list != null) {
			for (UPIDetail upiDetail : list) {
				if (upiInfo.size() == 0) {
					toDelete.add(upiDetail);
				} else {
					boolean exist = false;
					for (UPIDetailsDTO detailsDTO : upiInfo) {
						if (detailsDTO.getId() != null) {
							if (detailsDTO.getId() == upiDetail.getUpiDetailsId()) {
								exist = true;
								break;
							}
						}
					}
					if (!exist) {
						toDelete.add(upiDetail);
					}
				}
			}
		}

		for (UPIDetail upiDetail : toDelete) {
			upiInfo.remove(upiDetail);
			upiDetail.setIsDeleted(true);
			upiDetailRepository.save(upiDetail);
		}

		for (UPIDetailsDTO upiDetailsDTO : upiInfo) {
			UPIDetail upiDetail = new UPIDetail();
			if (upiDetailsDTO.getId() != null) {
				upiDetail = upiDetailRepository.findById(upiDetailsDTO.getId()).get();
			}
			upiDetail.setAccountName(upiDetailsDTO.getAccountName());
			upiDetail.setIsDefault(upiDetailsDTO.isDefaultPayment());
			upiDetail.setMobileNumber(upiDetailsDTO.getMobileNumber());

			if (StringUtils.equalsIgnoreCase(upiDetailsDTO.getPaymentType(), PaymentMethod.UPI.toString())) {
				upiDetail.setPaymentType(PaymentMethod.UPI);
				upiDetail.setUpiType(upiDetailsDTO.getUpiType());
				upiDetail.setUpi(upiDetailsDTO.getUpiId());
				if (upiDetailsDTO.getScannerImg() != null) {
					byte[] decodeImg = Base64.getDecoder().decode(upiDetailsDTO.getScannerImg());
					upiDetail.setUpiScanner(decodeImg);
				}
				upiDetail.setUpiType(upiDetailsDTO.getUpiType());
			} else if (StringUtils.equalsIgnoreCase(upiDetailsDTO.getPaymentType(), PaymentMethod.EWALLET.toString())) {
				upiDetail.setPaymentType(PaymentMethod.EWALLET);
				upiDetail.setEwallet(upiDetailsDTO.getEwallet());
			} else if (StringUtils.equalsIgnoreCase(upiDetailsDTO.getPaymentType(), PaymentMethod.NETBANKING.toString())) {
				upiDetail.setPaymentType(PaymentMethod.NETBANKING);
				upiDetail.setBankName(upiDetailsDTO.getBankName());
				upiDetail.setAccountNumber(upiDetailsDTO.getAccountNumber());
				upiDetail.setIfscCode(upiDetailsDTO.getIfscCode());
			}
			upiDetail.setUserId(user);
			upiDetailRepository.save(upiDetail);
		}
	}

	public Map<String, Object> updateMemberAcceptOrderStatus(long userId, UserRequest userRequest) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Optional<UserProfile> findUserById = userRepository.findById(userId);
		if (findUserById.isPresent()) {
			UserProfile userProfile = findUserById.get();
			userProfile.setMemberAcceptOrder(userRequest.isMemberAcceptOrder());
			resultMap.put("result", "success");
			return resultMap;
		} else {
			log.error("User profile not found with Id: {}", userId);
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", "User profile not found with Id: " + userId);
			return resultMap;
		}
	}

	public Map<String, Object> updateProfile(long userId, UserRequest userRequest) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Optional<UserProfile> findUserById = userRepository.findById(userId);
		if (findUserById.isPresent()) {
			UserProfile userProfile = findUserById.get();

			String roleName = null;
			userProfile.setUsername(userRequest.getEmailAddress());
			userProfile.setFirstName(userRequest.getFirstName());
			userProfile.setLastName(userRequest.getLastName());
			userProfile.setEmail(userRequest.getEmailAddress());
			userProfile.setContactNumber(userRequest.getContactNumber());
			userProfile.setGender(userRequest.getGender());

			if (userRequest.getRoleId() != null) {
				Optional<Roles> role = roleRepository.findById(userRequest.getRoleId());
				if (role.isPresent()) {
					userProfile.setRole(role.get());
					roleName = role.get().getName();
				}
			}

			if (Constants.MERCHANT_ADMIN.equals(roleName) && userRequest.getMerchantId() != null) {
				Optional<Merchant> checkMerchant = merchantRepository.findById(userRequest.getMerchantId());
				if (checkMerchant.isPresent()) {
					userProfile.setMerchant(checkMerchant.get());
				}
			}

			if (Constants.MEMBER.equals(roleName)) {
				userProfile.setDailyPayoutLimitAmount(userRequest.getDailyPayoutLimitAmount());
				userProfile.setSinglePayoutMaxAmount(userRequest.getSinglePayoutMaxAmount());
				userProfile.setSinglePayoutMinAmount(userRequest.getSinglePayoutMinAmount());
			}

			log.debug("Updated {} user profile successfully.", userProfile.getUsername());
			userRepository.save(userProfile);

			if (Constants.MEMBER.equals(roleName)) {
				saveOrUpdateUPIDetails(userRequest.getUpiInfo(), userProfile);
			}

			resultMap.put("result", "success");
			return resultMap;
		} else {
			log.error("User profile not found with Id: {}", userId);
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", "User profile not found with Id: " + userId);
			return resultMap;
		}
	}

	public Map<String, Object> deleteProfile(Long id) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Optional<UserProfile> findUserById = userRepository.findById(id);
		if (findUserById.isPresent()) {
			UserProfile user = findUserById.get();
			userRepository.delete(user);
			log.debug("Deleted user profile id {} successfully.", id);
			resultMap.put("result", "success");
			return resultMap;
		} else {
			log.error("User profile not found with Id: {}", id);
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", "User profile not found with Id: " + id);
			return resultMap;
		}
	}

	public HashMap<String, Object> changePassword(long userId, UserRequest userRequest) {
		HashMap<String, Object> result = new HashMap<>();
		try {
			Optional<UserProfile> users = userRepository.findById(userId);
			if (users.isPresent()) {
				UserProfile user = users.get();
				boolean passwordMatched = passwordEncoder.matches(userRequest.getOldPassword(), user.getPassword());
				if (passwordMatched) {
					user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
					userRepository.save(user);
					result.put("result", "success");
					result.put("message", "Password changed successfully");
					log.info("Password changed for userId: {}", userId);
				} else {
					result.put("result", "failed");
					result.put("errorMessage", "Old password does not match");
				}
			}
		} catch(Exception e){
			result.put("result", "failed");
			result.put("message", "Failed to change the password: " + e.getMessage());
			log.error("Failed to change the password for user {}: {}", userId, e.getMessage());
		}
		return result;
	}

	public List<Object> getAllMembers(String role) {

		List<Object> userList = new ArrayList<>();
		List<Object[]> objs = userRepository.getUsersByRole(role);
		for (Object[] obj : objs) {
			Map<String, Object> usersMap = new HashMap<String, Object>();
			Long userId = ((Long) obj[0]);
			String username = (String) obj[1];
			String email = (String) obj[2];
			usersMap.put("id", userId);
			usersMap.put("name", username);
			usersMap.put("email", email);
			userList.add(usersMap);
		}
		return userList;
	}

	public Map<String, Object> getUserUPI(Long userId) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<UPIDetail> uipDetailByUserId = upiDetailsRepository.getUipDetailByUserId(userId);
		for (UPIDetail upiDetail : uipDetailByUserId) {

			resultMap.put("upi", upiDetail.getUpi());
			UserProfile user = upiDetail.getUserId();
			if (user != null) {
				resultMap.put("firstname", user.getFirstName());
				resultMap.put("lastname", user.getLastName());
				resultMap.put("email", user.getEmail());
			}
			break;
		}
		return resultMap;
	}

	public List<UserRequest> getSubAccounts(Long id) {

		List<UserProfile> subAccountsList = userRepository.getSubAccounts(id);
		List<UserRequest> list = new ArrayList<UserRequest>();
		if (CollectionUtils.isNotEmpty(subAccountsList)) {
			subAccountsList.stream().forEach(subAccount -> {
				UserRequest user = new UserRequest();
				user.setId(subAccount.getUserId());
				user.setRole(subAccount.getRole().getName());
				user.setRoleId(subAccount.getRole().getRoleId());
				user.setEmailAddress(subAccount.getEmail());
				user.setFirstName(subAccount.getFirstName());
				user.setLastName(subAccount.getLastName());
				user.setContactNumber(subAccount.getContactNumber());
				user.setFullName(subAccount.getFirstName() + " " + subAccount.getLastName());
				user.setDateCreated(subAccount.getDateCreated().getTime());
				user.setStatus(subAccount.getIsUserActive());
				list.add(user);
			});
		}

		return list;
	}
	
	public MyAccountResponse getMyAccountDetails(Long id, String type) {

		Optional<UserProfile> checkUser = userRepository.findById(id);
		MyAccountResponse myAccount = new MyAccountResponse();
		if (checkUser.isPresent()) {
			UserProfile userProfile = checkUser.get();
			myAccount.setFullName(userProfile.getFirstName() + " " + userProfile.getLastName());
			myAccount.setContactNumber(userProfile.getContactNumber());
			myAccount.setId(userProfile.getUserId());

			if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN)
					|| type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT)) {
				if (userProfile.getMerchant() != null) {
					if (userProfile.getMerchant() != null) {
						myAccount.setBalance(walletRepository
								.getBalanceAmountByMerchantId(userProfile.getMerchant().getMerchantId()));
					}

				}
			}

			if (type.equalsIgnoreCase(Constants.MEMBER)) {
				myAccount.setBalance(walletRepository.getBalanceAmountByMemberId(id));
			}

			myAccount.setEmailAddress(userProfile.getEmail());
			myAccount.setDateCreated(userProfile.getDateCreated().getTime());
		}

		return myAccount;
	}

	public HashMap<String, Object> changeSettlementPassword(long userId, UserRequest userRequest) {
		HashMap<String, Object> result = new HashMap<>();
		try {
			Optional<UserProfile> users = userRepository.findById(userId);
			if (users.isPresent()) {
				UserProfile user = users.get();
				boolean passwordMatched = passwordEncoder.matches(userRequest.getOldPassword(), user.getPassword());
				if (passwordMatched) {
					user.setSettlementPassword(passwordEncoder.encode(userRequest.getPassword()));
					userRepository.save(user);
					result.put("result", "success");
					result.put("message", "Settlement Password changed successfully");
					log.info("Settlement Password changed for userId: {}", userId);
				} else {
					result.put("result", "failed");
					result.put("errorMessage", "Old settlement password does not match");
				}
			}
		} catch (Exception e) {
			result.put("result", "failed");
			result.put("message", "Failed to change the settlement password: " + e.getMessage());
			log.error("Failed to change the settlement password for user {}: {}", userId, e.getMessage());
		}
		return result;
	}


}
