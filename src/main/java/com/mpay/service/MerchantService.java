package com.mpay.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import com.mpay.dto.MerchantSettlementRequest;
import com.mpay.dto.UPIDetailsDTO;
import com.mpay.enums.TransactionType;
import com.mpay.model.MerchantSettlement;
import com.mpay.model.Transaction;
import com.mpay.model.UPIDetail;
import com.mpay.repository.MerchantSettlementRepository;
import com.mpay.repository.TransactionRepository;
import com.mpay.repository.UPIDetailRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.mpay.dto.MerchantBankAccountRequest;
import com.mpay.dto.MerchantRequest;
import com.mpay.enums.WalletType;
import com.mpay.model.Merchant;
import com.mpay.model.MerchantBankAccount;
import com.mpay.model.Roles;
import com.mpay.model.UserProfile;
import com.mpay.model.Wallet;
import com.mpay.repository.MerchantBankAccountRepository;
import com.mpay.repository.MerchantRepository;
import com.mpay.repository.RoleRepository;
import com.mpay.repository.UserRepository;
import com.mpay.repository.WalletRepository;
import com.mpay.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MerchantService {

	@Autowired
	CommonService commonService;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	UPIDetailRepository upiDetailRepository;

	@Autowired
	MerchantSettlementRepository merchantSettlementRepository;

	@Autowired
	MerchantRepository merchantRepository;

	@Autowired
	WalletRepository walletRepository;

	@Autowired
	MerchantBankAccountRepository merchantBankAccountRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private UserRepository userRepository;

	public Map<String, Object> saveOrUpdateMerchant(MerchantRequest merchantRequest, Long id) {

		boolean flag = false;
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Merchant merchant = null;
			if (id != null) {
				Optional<Merchant> checkMerchant = merchantRepository.findById(id);
				if (checkMerchant.isPresent()) {
					merchant = checkMerchant.get();
				} else {
					String message = "Merchant not found with the id: " + id;
					resultMap.put("result", "failed");
					resultMap.put("errorMessage", message);
					return resultMap;
					//throw new EntityExistsException("Merchant not found with the id: " + id);
				}
			} else {
				flag = true;
				merchant = new Merchant();
			}
			
			if(flag) {
				UserProfile checkUser = userRepository.findByUsername(merchantRequest.getLoginEmail());
				if (checkUser != null) {
					String message = "User already exists with this login email: " + merchantRequest.getLoginEmail();
					resultMap.put("result", "failed");
					resultMap.put("errorMessage", message);
					log.error(message);
					return resultMap;
				}
			}

			merchant.setApiUsername(merchantRequest.getApiUsername());
			merchant.setApiPassword(merchantRequest.getApiPassword());
			merchant.setContactNumber(merchantRequest.getContactNumber());
			//merchant.setName(merchantRequest.getFirstName() + " " + merchantRequest.getLastName());
			merchant.setName(merchantRequest.getName());
			merchant.setWebsite(merchantRequest.getWebsite());
			merchant.setWebhookURL(merchantRequest.getWebhookURL());
			merchant.setWebhookUsername(merchantRequest.getWebhookUsername());
			merchant.setWebhookPassword(merchantRequest.getWebhookPassword());
			merchant.setMerchantManualSettlement(merchantRequest.getMerchantManualSettlement());
			merchant.setManualSettlementMaxAmount(merchantRequest.getManualSettlementMaxAmount());
			merchant.setTopUpRate(merchantRequest.getTopUpRate());
			merchant.setMerchantStatus(merchantRequest.getMerchantStatus());
			merchant.setFirstDepositRate(merchantRequest.getFirstDepositRate());
			merchant.setWithdrawalRate(merchantRequest.getWithdrawalRate());
			merchant.setPayInRate(merchantRequest.getPayInRate());
			merchant.setPayOutRate(merchantRequest.getPayOutRate());

			Merchant saveMerchant = merchantRepository.save(merchant);
			if (flag) {
				// create wallet for merchant
				Wallet wallet = new Wallet();

				wallet.setWalletType(WalletType.MERCHANT);
				wallet.setCommissionAmount(BigDecimal.valueOf(0));
				wallet.setBalanceAmount(BigDecimal.valueOf(0));
				wallet.setMerchant(saveMerchant);
				walletRepository.save(wallet);
			}
			if (flag) {
				// Add Same in the user name
				UserProfile user = new UserProfile();
				user.setUsername(merchantRequest.getLoginEmail());
				user.setEmail(merchantRequest.getLoginEmail());
				String name = merchantRequest.getName();
				if (name != null) {
					String[] split = name.split(" ");
					if (split.length >= 1) {
						String firstname = split[0];
						user.setFirstName(firstname);
					}

					if (split.length == 2) {
						String lastname = split[1];
						user.setLastName(lastname);
					} else {
						user.setLastName("");
					}
				}

				user.setContactNumber(merchantRequest.getContactNumber());
				user.setPassword(passwordEncoder.encode(merchantRequest.getLoginPassword()));
				user.setSettlementPassword(passwordEncoder.encode(merchantRequest.getSettlementPassword()));
				user.setIsUserActive(true);
				Roles role = roleRepository.findByName(Constants.MERCHANT_ADMIN);
				user.setRole(role);
				user.setMerchant(saveMerchant);
				userRepository.save(user);

			}
			resultMap.put("result", "success");
			return resultMap;
		} catch (Exception e) {
			if (id != null) {
				log.debug("Failed to update merchant with Id: {} {}", id, e.getMessage(), e);
			} else {
				log.debug("Failed to save new merchant: {}", e.getMessage(), e);

			}
			return null;
		}
	}

	public List<MerchantRequest> getAllMarchart() {
		List<MerchantRequest> merchartList = new ArrayList<MerchantRequest>();
		List<Merchant> dbMerchantList = merchantRepository.findAll();
		if (CollectionUtils.isNotEmpty(dbMerchantList)) {
			dbMerchantList.stream().forEach(merchant -> {
				if (!merchant.getIsDeleted()) {
					MerchantRequest merchantRequest = new MerchantRequest();
					merchantRequest.setMerchantId(merchant.getMerchantId());
					merchantRequest.setName(merchant.getName());
					merchantRequest.setContactNumber(merchant.getContactNumber());
					merchantRequest.setWebsite(merchant.getWebsite());
					merchantRequest.setApiUsername(merchant.getApiUsername());
					merchantRequest.setWebhookURL(merchant.getWebhookURL());
					merchantRequest.setWebhookUsername(merchant.getWebhookUsername());
					merchantRequest.setWebhookPassword(merchant.getWebhookPassword());
					merchantRequest.setMerchantManualSettlement(merchant.getMerchantManualSettlement());
					merchantRequest.setManualSettlementMaxAmount(merchant.getManualSettlementMaxAmount());
					merchantRequest.setTopUpRate(merchant.getTopUpRate());
					merchantRequest.setMerchantStatus(merchant.getMerchantStatus());
					merchartList.add(merchantRequest);
				}
			});
		}
		log.debug("Fetched all marchants successfully.");
		return merchartList;
	}

	public Boolean deleteMerchant(Long id) {
		Optional<Merchant> checkMerchant = merchantRepository.findById(id);
		if (checkMerchant.isPresent()) {
			Merchant merchant = checkMerchant.get();
			merchantRepository.delete(merchant);
			log.debug("Deleted merchant id {} successfully.", id);
			return true;
		} else {
			log.error("Merchant not found with Id: {}", id);
			return false;
		}
	}

	public MerchantBankAccount createOrUpdateMerchantBankAccount(
			MerchantBankAccountRequest merchantBankAccountRequest) {

		MerchantBankAccount merchantBankAccount = new MerchantBankAccount();
		if (merchantBankAccountRequest.getId() != null) {
			Optional<MerchantBankAccount> checkMerchantBank = merchantBankAccountRepository
					.findById(merchantBankAccountRequest.getId());
			if (checkMerchantBank.isPresent()) {
				merchantBankAccount = checkMerchantBank.get();
			} else {
				String message = "Merchant account not found with the Id: " + merchantBankAccountRequest.getId();
				log.error(message);
				throw new EntityNotFoundException(message);
			}
		}
		Merchant merchant = merchantRepository.findById(merchantBankAccountRequest.getMerchantId()).get();
		merchantBankAccount.setMerchant(merchant);
		merchantBankAccount.setPaymentMethod(merchantBankAccountRequest.getPaymentMethod());
		merchantBankAccount.setBankName(merchantBankAccountRequest.getBankName());
		merchantBankAccount.setAccountName(merchantBankAccountRequest.getAccountName());
		merchantBankAccount.setAccountNumber(merchantBankAccountRequest.getAccountNumber());
		merchantBankAccount.setUpiId(merchantBankAccountRequest.getUpiId());
		merchantBankAccount.setApprovalStatus(merchantBankAccountRequest.getApprovalStatus());
		merchantBankAccount.setContactNumber(merchantBankAccountRequest.getContactNumber());
		merchantBankAccount.setEmailAddress(merchantBankAccountRequest.getEmailAddress());
		merchantBankAccount.setIfscCode(merchantBankAccountRequest.getIfscCode());
		return merchantBankAccountRepository.save(merchantBankAccount);
	}

	public List<MerchantBankAccountRequest> getMerchantBankDetails(Long merchantId) {
		List<MerchantBankAccount> merchantBankDetails = merchantBankAccountRepository
				.getMerchantBankDetailsByMerchantId(merchantId);
		List<MerchantBankAccountRequest> accountDetailsList = new ArrayList<MerchantBankAccountRequest>();

		if (CollectionUtils.isNotEmpty(merchantBankDetails)) {
			merchantBankDetails.stream().forEach(merchantBankAccount -> {
				MerchantBankAccountRequest accountDetails = new MerchantBankAccountRequest();
				commonService.setMerchantBankAccountData(accountDetails, merchantBankAccount);
				accountDetailsList.add(accountDetails);
			});
		}
		return accountDetailsList;
	}

	public List<MerchantSettlementRequest> getMerchantSettledAmounts(Long merchantId) {
		List<MerchantSettlementRequest> list = new ArrayList<>();
		List<MerchantSettlement> merchantSettlements = new ArrayList<>();
		if (merchantId == null) {
			merchantSettlements = merchantSettlementRepository.getAllSettledAmounts();
		} else {
			merchantSettlements = merchantSettlementRepository.getMerchantSettledAmounts(merchantId);
		}
		for (MerchantSettlement merchantSettlement : merchantSettlements) {
			MerchantSettlementRequest request = new MerchantSettlementRequest();
			request.setAmount(merchantSettlement.getAmount());
			Merchant merchant = merchantSettlement.getMerchant();
			UserProfile member = merchantSettlement.getMember();
			request.setMerchant(merchant.getName());
			request.setMerchantId(merchant.getMerchantId());
			request.setMember(member.getFirstName() + " " + member.getLastName());
			request.setMemberId(member.getUserId());

			UPIDetail upiDetail = merchantSettlement.getUpiDetail();
			UPIDetailsDTO upiDetailsDTO = new UPIDetailsDTO();
			commonService.setUPIDetails(upiDetailsDTO, upiDetail);
			request.setUpiDetails(upiDetailsDTO);

			list.add(request);
		}
		return list;
	}

	public Map<String, Object> settleAmount(MerchantSettlementRequest request) {
		Optional<UPIDetail> upiDetails = upiDetailRepository.findById(request.getUpiDetailId());
		Optional<Merchant> merchants = merchantRepository.findById(request.getMerchantId());
		Map<String, Object> resultMap = new HashMap<>();

		if (upiDetails.isPresent()) {
			UPIDetail upiDetail = upiDetails.get();
			UserProfile member = upiDetail.getUserId();
			Merchant merchant = merchants.get();

			Wallet merchantWallet = walletRepository.getWalletByMerchantId(merchant.getMerchantId()).get();

			BigDecimal settleAmount = request.getAmount();
			double amount = settleAmount.doubleValue();
			if (settleAmount.doubleValue() > amount) {
				resultMap.put("errorMessage", "Settle amount can not be greater than the balance");
				resultMap.put("result", "failed");
				return resultMap;
			}

			MerchantSettlement merchantSettlement = new MerchantSettlement();
			merchantSettlement.setMerchant(merchant);
			merchantSettlement.setAmount(request.getAmount());
			merchantSettlement.setMember(member);
			merchantSettlement.setUpiDetail(upiDetail);
			merchantSettlementRepository.save(merchantSettlement);

			Transaction transaction = new Transaction();
			transaction.setTransactionType(TransactionType.DEBIT);
			transaction.setTransactionMethod("SETTLEMENT");

			Double merchantCommission = (2 * settleAmount.doubleValue()) / 100;

			transaction.setBalanceBefore(merchantWallet.getBalanceAmount());
			transaction.setBalanceAfter(BigDecimal.valueOf(merchantWallet.getBalanceAmount().doubleValue() - (amount + ((2 * amount) / 100))));
			transaction.setCommissionAmount(BigDecimal.valueOf(-merchantCommission));

			merchantWallet.setBalanceAmount(BigDecimal.valueOf(merchantWallet.getBalanceAmount().doubleValue() - (amount + ((2 * amount) / 100))));
			merchantWallet.setCommissionAmount(BigDecimal.valueOf((merchantWallet.getCommissionAmount().doubleValue() - merchantCommission)));
			walletRepository.save(merchantWallet);

			transaction.setMerchant(merchant);
			transaction.setWalletType(WalletType.MERCHANT);

			transactionRepository.save(transaction);


			// System wallet
			transaction = new Transaction();

			transaction.setTransactionType(TransactionType.CREDIT);
			transaction.setTransactionMethod("SETTLEMENT");

			// get system commission ( 1% from the incoming amount)
			Double systemCommission = (1 * amount) / 100;
			Wallet systemWallet = walletRepository.getWalletForSystem(WalletType.PAYMENT_SYSTEM).get();
			systemWallet.setCommissionAmount(BigDecimal.valueOf(systemWallet.getCommissionAmount().doubleValue() + systemCommission));
			walletRepository.save(systemWallet);

			transaction.setWalletType(WalletType.PAYMENT_SYSTEM);
			transaction.setCommissionAmount(BigDecimal.valueOf(systemCommission));
			transactionRepository.save(transaction);


			// Member wallet
			Wallet memberWallet = walletRepository.getWalletByMemberId(member.getUserId()).get();

			// get member commission ( 1% from the amount)
			Double memberCommission = (1 * amount) / 100;

			transaction = new Transaction();

			transaction.setTransactionType(TransactionType.CREDIT);
			transaction.setTransactionMethod("SETTLEMENT");

			transaction.setBalanceBefore(memberWallet.getBalanceAmount());
			transaction.setBalanceAfter(BigDecimal.valueOf((memberWallet.getBalanceAmount().doubleValue() + amount) + memberCommission));
			transaction.setCommissionAmount(BigDecimal.valueOf(memberCommission));

			memberWallet.setBalanceAmount(BigDecimal.valueOf((memberWallet.getBalanceAmount().doubleValue() + amount) + memberCommission));
			memberWallet.setCommissionAmount(BigDecimal.valueOf(memberWallet.getCommissionAmount().doubleValue() + memberCommission));

			walletRepository.save(memberWallet);

			transaction.setMember(member);
			transaction.setWalletType(WalletType.MEMBER);
			transactionRepository.save(transaction);
		}
		resultMap.put("result", "success");
		resultMap.put("message", "Amount has been settled");
		return resultMap;
	}

	public List<HashMap<String, Object>> getMerchantMemberUPIDetails(long merchantId) {
		List<HashMap<String, Object>> list = new ArrayList<>();
		List<Object[]> records = upiDetailRepository.getMerchantMemberUPIDetails(merchantId);
		if (records != null) {
			for (Object[] record : records) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("id", record[0]);
				map.put("upi", record[1]);
				list.add(map);
			}
		}
		return list;
	}

	public List<Object> getIdAndAccountNamesByMerchantId(Long merchantId) {
		List<Object[]> accountList = merchantBankAccountRepository.getIdAndAccountNamesByMerchantId(merchantId);

		List<Object> idAndAccountNameList = new ArrayList<>();

		for (Object[] obj : accountList) {
			Map<String, Object> map = new HashMap<String, Object>();
			Long id = ((Long) obj[0]);
			String bankName = (String) obj[1];
			String upiId = (String) obj[2];
			map.put("accountId", id);
			if (bankName != null) {
				map.put("name", bankName);
			} else {
				map.put("name", upiId);
			}

			idAndAccountNameList.add(map);
		}

		return idAndAccountNameList;

	}
}
