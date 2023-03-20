package com.mpay.service;

import com.mpay.model.Merchant;
import com.mpay.model.MerchantBankAccount;
import com.mpay.model.Order;
import com.mpay.model.Roles;
import com.mpay.model.Transaction;
import com.mpay.model.UPIDetail;
import com.mpay.model.UserProfile;
import com.mpay.model.Wallet;
import com.mpay.dto.MerchantBankAccountRequest;
import com.mpay.dto.OrderRequest;
import com.mpay.dto.UPIDetailsDTO;
import com.mpay.dto.UserRequest;
import com.mpay.dto.WithdrawAndDepositOrdersRequest;
import com.mpay.enums.OrderStatus;
import com.mpay.enums.OrderType;
import com.mpay.enums.PaymentMethod;
import com.mpay.enums.TransactionType;
import com.mpay.enums.WalletType;
import com.mpay.repository.MerchantBankAccountRepository;
import com.mpay.repository.MerchantRepository;
import com.mpay.repository.OrderRepository;
import com.mpay.repository.RoleRepository;
import com.mpay.repository.TransactionRepository;
import com.mpay.repository.UPIDetailRepository;
import com.mpay.repository.UserRepository;
import com.mpay.repository.WalletRepository;
import com.mpay.util.Constants;
import com.mpay.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.guieffect.qual.UIPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Transactional
@Service
@Slf4j
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	CommonService commonService;

	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WebhookService webhookService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	WalletRepository walletRepository;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	MerchantRepository merchantRepository;
	
	@Autowired
	MerchantBankAccountRepository merchantBankAccountRepository;

	@Autowired
	UPIDetailRepository upiDetailRepository;

	public OrderRequest getOrderDetails(Long id) {
		Optional<Order> orders = orderRepository.findById(id);
		OrderRequest orderRequest = new OrderRequest();
		if (orders.isPresent()) {
			Order order = orders.get();
			setOrderDetails(orderRequest, order, false);
		}
		return orderRequest;
	}

	public Map<String, Object> savePayInOrder(OrderRequest orderRequest) {

		// Checked user is already exist or not
		UserProfile user = null;
		if (orderRequest.getUser() != null || orderRequest.getUserId() != null) {
			String email = null;
			if (orderRequest.getUser() != null) {
				email = orderRequest.getUser().getEmailAddress();
			}
			user = userRepository.findByUsernameOrId(email, orderRequest.getUserId());
		}

		Map<String, Object> resultMap = new HashMap<>();

		// If not exist then create new user
		if (user == null && orderRequest.getUser() != null) {
			user = new UserProfile();
			UserRequest userData = orderRequest.getUser();
			user.setFirstName(userData.getFirstName());
			user.setLastName(userData.getLastName());
			user.setEmail(userData.getEmailAddress());
			user.setIsUserActive(true);

			Roles role = roleRepository.findByName(Constants.USER);
			user.setRole(role);
			user.setPassword(passwordEncoder.encode("Test@1234"));
			user.setUsername(userData.getEmailAddress());

			userRepository.save(user);

			// Save user UPI
			userProfileService.saveOrUpdateUPIDetails(userData.getUpiInfo(), user);

		}

		// Insert data into the order table
		Order order = new Order();
		order.setUser(user);
		order.setOrderNumber(orderRequest.getOrderNumber());
		order.setOrderType(OrderType.PAYIN);
		order.setAmount(orderRequest.getAmount());
		order.setSettlementType("API");

		if (orderRequest.getMerchantId() == null) {
			log.error("Merchant is required to create PAYIN order");
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", "Merchant is required to create PAYIN order");
			return resultMap;
		}

		Optional<Merchant> checkMerchant = merchantRepository.findById(orderRequest.getMerchantId());
		if (checkMerchant.isPresent()) {
			order.setMerchant(checkMerchant.get());
		} else {
			resultMap.put("result", "failed");
			log.error("Merchant not found with the Id: " + orderRequest.getMerchantId());
			resultMap.put("errorMessage", "Merchant not found with the Id: " + orderRequest.getMerchantId());
			return resultMap;
		}

		BigDecimal amount = orderRequest.getAmount();

		boolean isAdmin = false;

		//admin true
		if (orderRequest.getUserId() != null) {
			isAdmin = true;
		}

		Long memberId = null;
		if (!isAdmin) {
			// Find the eligible member from the wallet table.
			 memberId = getEligibleMemberId(amount);
			if (memberId == null) {
				log.error("No any member is eligible for the order");
				resultMap.put("result", "failed");
				resultMap.put("errorMessage", "No any member is eligible for the order");
				return resultMap;
			}
		}else {
			// admin case
			 memberId = orderRequest.getMemberId();
		}

		Optional<UPIDetail> defaultPaymentByUserId = upiDetailRepository.getDefaultPaymentByUserId(memberId);
		if (defaultPaymentByUserId.isPresent()) {
			order.setUpiDetail(defaultPaymentByUserId.get());
		}

		order.setOrderStatus(OrderStatus.PENDING);
		if (memberId != null) {
			UserProfile member = userRepository.findById(memberId).get();
			order.setMember(member);
			order.setOrderStatus(OrderStatus.ASSIGNED);
		}

		String paymentMethod = orderRequest.getPaymentMethod();
		if (PaymentMethod.UPI.getValue().equalsIgnoreCase(paymentMethod)
				|| PaymentMethod.EWALLET.getValue().equalsIgnoreCase(paymentMethod)
				|| PaymentMethod.NETBANKING.getValue().equalsIgnoreCase(paymentMethod)) {
			order.setPaymentMethod(PaymentMethod.valueOf(orderRequest.getPaymentMethod()));
		} else {
			String message = "No suitable payment method. Payment method must be " + PaymentMethod.UPI + " OR "
					+ PaymentMethod.EWALLET + " OR " + PaymentMethod.NETBANKING;
			log.error(message);
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", message);
			return resultMap;
		}

		// save order into order table
		orderRepository.save(order);

		// Find user using member id and return its UPI and QR code scanner

		if (memberId != null) {
			if (defaultPaymentByUserId.isPresent()) {
				UPIDetail upiDetail = defaultPaymentByUserId.get();
				HashMap<String, Object> upiInfo = new HashMap<>();
				commonService.setUPIDetailsForPayInOrder(upiInfo, upiDetail);
				resultMap.put("upiInfo", upiInfo);
			}
		}

//		if(memberId != null) {
//			Optional<UserProfile> findById = userRepository.findById(memberId);
//			List<UPIDetailsDTO> detailsDTOs = new ArrayList<UPIDetailsDTO>();
//			if (findById.isPresent()) {
//				UserProfile userProfile = findById.get();
//				List<UPIDetail> upiDetails = userProfile.getUPIDetails();
//				for (UPIDetail upiDetails2 : upiDetails) {
//					UPIDetailsDTO detailsDTO = new UPIDetailsDTO();
//					detailsDTO.setUpiId(upiDetails2.getUpi());
//
//					if (upiDetails2.getUpiScanner() != null) {
//						String scannerImg = Base64.getEncoder().encodeToString(upiDetails2.getUpiScanner());
//						detailsDTO.setScannerImg(scannerImg);
//					}
//					detailsDTOs.add(detailsDTO);
//				}
//			}
//			resultMap.put("upiInfo", detailsDTOs);
//		}

		resultMap.put("result", "success");
		resultMap.put("orderId", order.getOrderId());

		return resultMap;
	}

	public Map<String, Object> completeWithdrawOrder(OrderRequest orderRequest) {
		Optional<Order> orders = orderRepository.findById(orderRequest.getId());
		Map<String, Object> resultMap = new HashMap<>();
		if (orders.isPresent()) {
			Order order = orders.get();
			if (orderRequest.getTransactionNumber() == null) {
				resultMap.put("result", "failed");
				log.error("TrasactionNumber must be required");
				resultMap.put("errorMessage", "TrasactionNumber must be required");
				return resultMap;
			}

			if (order.getMerchant() != null) {
				Optional<Wallet> checkMerchantWallet = walletRepository
						.getWalletByMerchantId(order.getMerchant().getMerchantId());
				if (!checkMerchantWallet.isPresent()) {
					resultMap.put("result", "failed");
					String message = "Wallet not found for merchant Id: " + order.getMerchant().getMerchantId();
					log.error(message);
					resultMap.put("errorMessage", message);
					return resultMap;
				}
				Wallet merchantWallet = checkMerchantWallet.get();
				BigDecimal balance = merchantWallet.getBalanceAmount();

				BigDecimal balanceBefore = merchantWallet.getBalanceAmount();
				balance = balance.subtract(order.getAmount());

				String transactionNumber = orderRequest.getTransactionNumber();
				order.setTransactionNumber(transactionNumber);
				order.setOrderStatus(OrderStatus.COMPLETED);
				order.setDateCompleted(new Timestamp(System.currentTimeMillis()));
				orderRepository.save(order);

				merchantWallet.setBalanceAmount(balance);
				walletRepository.save(merchantWallet);

				//transaction
				Transaction transaction = new Transaction();
				transaction.setTransactionType(TransactionType.WITHDRAW);
				transaction.setBalanceBefore(balanceBefore);
				transaction.setBalanceAfter(balance);
				transaction.setMerchant(order.getMerchant());
				transaction.setWalletType(WalletType.MERCHANT);
				transaction.setOrder(order);
				Double withdrawFee = (2 * order.getAmount().doubleValue()) / 100;
				transaction.setFee(new BigDecimal(withdrawFee));
				transactionRepository.save(transaction);
			} else if (order.getMember() != null) {
				Optional<Wallet> checkMemberWallet = walletRepository.getWalletByMemberId(order.getMember().getUserId());
				if (!checkMemberWallet.isPresent()) {
					resultMap.put("result", "failed");
					String message = "Wallet not found for member Id: " + order.getMember().getUserId();
					log.error(message);
					resultMap.put("errorMessage", message);
					return resultMap;
				}
				order.setOrderStatus(OrderStatus.COMPLETED);
				order.setDateCompleted(new Timestamp(System.currentTimeMillis()));
				orderRepository.save(order);

				Wallet memberWallet = checkMemberWallet.get();
				BigDecimal balance = memberWallet.getBalanceAmount();
				BigDecimal balanceBefore = memberWallet.getBalanceAmount();
				balance = balance.subtract(order.getAmount());
				memberWallet.setBalanceAmount(balance);
				walletRepository.save(memberWallet);

				//transaction
				Transaction transaction = new Transaction();
				transaction.setTransactionType(TransactionType.WITHDRAW);
				transaction.setBalanceBefore(balanceBefore);
				transaction.setBalanceAfter(balance);
				transaction.setMember(order.getMember());
				transaction.setWalletType(WalletType.MEMBER);
				transaction.setOrder(order);
				Double fee = (2 * order.getAmount().doubleValue()) / 100;
				transaction.setFee(new BigDecimal(fee));
				transactionRepository.save(transaction);
			}
		}
		resultMap.put("result", "success");
		resultMap.put("message", "Order completed successfully");
		return resultMap;
	}

	public Map<String, Object> completeDepositOrder(OrderRequest orderRequest) {
		Optional<Order> orders = orderRepository.findById(orderRequest.getId());
		Map<String, Object> resultMap = new HashMap<>();
		if (orders.isPresent()) {
			Order order = orders.get();

			if (order.getMerchant() != null) {
				Optional<Wallet> checkMerchantWallet = walletRepository
						.getWalletByMerchantId(order.getMerchant().getMerchantId());
				if (!checkMerchantWallet.isPresent()) {
					resultMap.put("result", "failed");
					String message = "Wallet not found for merchant Id: " + order.getMerchant().getMerchantId();
					log.error(message);
					resultMap.put("errorMessage", message);
					return resultMap;
				}
				Wallet merchantWallet = checkMerchantWallet.get();
				BigDecimal balance = merchantWallet.getBalanceAmount();
				BigDecimal balanceBefore = merchantWallet.getBalanceAmount();
				Double fee = 0d;
				if (balanceBefore.equals(new BigDecimal(0))) {
					fee = (2 * order.getAmount().doubleValue()) / 100;
				}
				balance = balance.add(order.getAmount()).subtract(new BigDecimal(fee));

				order.setOrderStatus(OrderStatus.COMPLETED);
				order.setDateCompleted(new Timestamp(System.currentTimeMillis()));
				orderRepository.save(order);

				merchantWallet.setBalanceAmount(balance);
				walletRepository.save(merchantWallet);

				//transaction
				Transaction transaction = new Transaction();
				transaction.setTransactionType(TransactionType.DEPOSIT);
				transaction.setBalanceBefore(balanceBefore);
				transaction.setBalanceAfter(balance);
				transaction.setMerchant(order.getMerchant());
				transaction.setWalletType(WalletType.MERCHANT);
				transaction.setOrder(order);
				// Fee will be applied for 1st deposit
				if (balanceBefore.equals(new BigDecimal(0))) {
					fee = (2 * order.getAmount().doubleValue()) / 100;
					transaction.setFee(new BigDecimal(fee));
				}
				transactionRepository.save(transaction);
			}
		}
		resultMap.put("result", "success");
		resultMap.put("message", "Order completed successfully");
		return resultMap;
	}

	public Map<String, Object> confirmOrderPayment(OrderRequest orderRequest) {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Optional<Order> checkOrder = orderRepository.findById(orderRequest.getId());

			if (checkOrder.isPresent()) {
				Order order = checkOrder.get();

				if (StringUtils.isNotBlank(orderRequest.getTransactionReceipt())) {
					byte[] decodeImg = Base64.getDecoder().decode(orderRequest.getTransactionReceipt());
					String receiptUrl = S3Service.uploadOrderReceipt(decodeImg, order.getOrderId());
					order.setReceiptUrl(receiptUrl);
				}

				if (StringUtils.isNotBlank(orderRequest.getTransactionNumber())) {
					String transationNumber = orderRequest.getTransactionNumber();
					order.setTransactionNumber(transationNumber);
				}

				order.setOrderStatus(OrderStatus.PENDING_REVIEW);
				orderRepository.save(order);

				resultMap.put("message", "Order has been send for review");
				resultMap.put("result", "success");
				return resultMap;
			}
		} catch (Exception e){
			resultMap.put("errorMessage", "Failed to confirm the order: " + e.getMessage());
			resultMap.put("result", "failed");
			return resultMap;
		}
		return resultMap;
	}

	public Map<String, Object> rejectOrder(OrderRequest orderRequest) {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Optional<Order> checkOrder = orderRepository.findById(orderRequest.getId());

			if (checkOrder.isPresent()) {
				Order order = checkOrder.get();
				order.setOrderStatus(OrderStatus.REJECTED);
				order.setRejectionComment(orderRequest.getRejectionComment());
				orderRepository.save(order);

				resultMap.put("message", "Order has been rejected successfully");
				resultMap.put("result", "success");
				return resultMap;
			}
		} catch (Exception e){
			resultMap.put("errorMessage", "Failed to reject the order: " + e.getMessage());
			resultMap.put("result", "failed");
			return resultMap;
		}
		return resultMap;
	}

	public Map<String, Object> completeOrder(OrderRequest orderRequest) {

		Optional<Order> checkOrder = orderRepository.findById(orderRequest.getId());

		Map<String, Object> resultMap = new HashMap<>();
		if (checkOrder.isPresent()) {
			Order order = checkOrder.get();
			order.setOrderStatus(OrderStatus.COMPLETED);
			order.setDateCompleted(new Timestamp(System.currentTimeMillis()));
			orderRepository.save(order);

			// Get wallet for the member using memberId
			Optional<Wallet> checkMemberWallet = walletRepository.getWalletByMemberId(order.getMember().getUserId());

			if (!checkMemberWallet.isPresent()) {
				resultMap.put("result", "failed");
				log.error("Wallet not found for member Id: " + order.getMember().getUserId());
				resultMap.put("errorMessage", "Wallet not found for member Id: " + order.getMember().getUserId());
				return resultMap;
			}

			// Get wallet for the merchant using merchantId
			Optional<Wallet> checkMerchantWallet = walletRepository
					.getWalletByMerchantId(order.getMerchant().getMerchantId());
			if (!checkMerchantWallet.isPresent()) {
				resultMap.put("result", "failed");
				String message = "Wallet not found for merchant Id: " + order.getMerchant().getMerchantId();
				log.error(message);
				resultMap.put("errorMessage", "Wallet not found for merchant Id: " + message);
				return resultMap;
			}

			// Get our system wallet
			Optional<Wallet> checkWalletForSystem = walletRepository.getWalletForSystem(WalletType.PAYMENT_SYSTEM);
			if (!checkWalletForSystem.isPresent()) {
				String message = "Wallet not found for system with wallet type: " + WalletType.PAYMENT_SYSTEM.toString();
				resultMap.put("errorMessage", message);
				resultMap.put("result", "failed");
				return resultMap;
			}

			if (checkMemberWallet.isPresent()) {
				Wallet wallet = checkMemberWallet.get();

				// get member commission ( 1% from the amount)
				Double memberCommission = (1 * order.getAmount().doubleValue()) / 100;

				Transaction transaction = new Transaction();

				if (StringUtils.equalsIgnoreCase(order.getOrderType().name(), OrderType.PAYIN.getValue())) {
					transaction.setTransactionType(TransactionType.DEBIT);
				} else if (StringUtils.equalsIgnoreCase(order.getOrderType().name(), OrderType.PAYOUT.getValue())) {
					transaction.setTransactionType(TransactionType.CREDIT);
				}

				transaction.setBalanceBefore(wallet.getBalanceAmount());
				if (StringUtils.equalsIgnoreCase(order.getOrderType().name(), OrderType.PAYIN.getValue())) {
					transaction.setBalanceAfter(
							BigDecimal.valueOf((wallet.getBalanceAmount().doubleValue() - order.getAmount().doubleValue())
									+ memberCommission));
					transaction.setCommissionAmount(BigDecimal.valueOf(memberCommission));

					wallet.setBalanceAmount(
							BigDecimal.valueOf((wallet.getBalanceAmount().doubleValue() - order.getAmount().doubleValue())
									+ memberCommission));

					wallet.setCommissionAmount(
							BigDecimal.valueOf(wallet.getCommissionAmount().doubleValue() + memberCommission));
				} else {
					transaction.setBalanceAfter(BigDecimal.valueOf(wallet.getBalanceAmount().doubleValue()
							+ (order.getAmount().doubleValue() + memberCommission)));
					transaction.setCommissionAmount(BigDecimal.valueOf(memberCommission));

					wallet.setBalanceAmount(BigDecimal.valueOf(wallet.getBalanceAmount().doubleValue()
							+ (order.getAmount().doubleValue() + memberCommission)));

					wallet.setCommissionAmount(
							BigDecimal.valueOf(wallet.getCommissionAmount().doubleValue() + memberCommission));
				}
				wallet.setDate(new Date());
				walletRepository.save(wallet);

				transaction.setMember(order.getMember());
				transaction.setWalletType(WalletType.MEMBER);
				transaction.setOrder(order);
				transactionRepository.save(transaction);
			}

			if (checkMerchantWallet.isPresent()) {
				Wallet wallet = checkMerchantWallet.get();

				Transaction transaction = new Transaction();

				if (StringUtils.equalsIgnoreCase(order.getOrderType().name(), OrderType.PAYIN.getValue())) {
					transaction.setTransactionType(TransactionType.CREDIT);
				} else if (StringUtils.equalsIgnoreCase(order.getOrderType().name(), OrderType.PAYOUT.getValue())) {
					transaction.setTransactionType(TransactionType.DEBIT);
				}

				// get merchant commission ( 2% from the incoming amount)
				Double merchantCommission = (2 * order.getAmount().doubleValue()) / 100;

				transaction.setBalanceBefore(wallet.getBalanceAmount());
				if (StringUtils.equalsIgnoreCase(order.getOrderType().name(), OrderType.PAYIN.getValue())) {

					double amount = order.getAmount().doubleValue();

					transaction.setBalanceAfter(BigDecimal.valueOf(wallet.getBalanceAmount().doubleValue()
							+ (amount - ((2 * order.getAmount().doubleValue()) / 100))));
					transaction.setCommissionAmount(BigDecimal.valueOf(merchantCommission));

					wallet.setBalanceAmount(BigDecimal.valueOf(wallet.getBalanceAmount().doubleValue()
							+ (amount - ((2 * order.getAmount().doubleValue()) / 100))));

					wallet.setCommissionAmount(
							BigDecimal.valueOf(wallet.getCommissionAmount().doubleValue() + merchantCommission));
				} else {

					// order amount
					double amount = order.getAmount().doubleValue();

					transaction.setBalanceAfter(BigDecimal
							.valueOf(wallet.getBalanceAmount().doubleValue() - (amount + ((2 * amount) / 100))));

					transaction.setCommissionAmount(BigDecimal.valueOf(-merchantCommission));

					wallet.setBalanceAmount(BigDecimal
							.valueOf(wallet.getBalanceAmount().doubleValue() - (amount + ((2 * amount) / 100))));

					wallet.setCommissionAmount(
							BigDecimal.valueOf((wallet.getCommissionAmount().doubleValue() - merchantCommission)));
				}

				wallet.setDate(new Date());
				walletRepository.save(wallet);

				transaction.setMerchant(order.getMerchant());
				transaction.setWalletType(WalletType.MERCHANT);
				transaction.setOrder(order);
				transactionRepository.save(transaction);
			}

			if (checkWalletForSystem.isPresent()) {
				Transaction transaction = new Transaction();

				if (StringUtils.equalsIgnoreCase(order.getOrderType().name(), OrderType.PAYIN.getValue())) {
					transaction.setTransactionType(TransactionType.DEBIT);
				} else if (StringUtils.equalsIgnoreCase(order.getOrderType().name(), OrderType.PAYOUT.getValue())) {
					transaction.setTransactionType(TransactionType.CREDIT);
				}

				// get system commission ( 1% from the incoming amount)
				Double systemCommission = (1 * order.getAmount().doubleValue()) / 100;
				Wallet wallet = checkWalletForSystem.get();
				wallet.setCommissionAmount(
						BigDecimal.valueOf(wallet.getCommissionAmount().doubleValue() + systemCommission));
				wallet.setDate(new Date());
				walletRepository.save(wallet);

				// transaction.setMerchant(order.getMerchant());
				transaction.setWalletType(WalletType.PAYMENT_SYSTEM);
				transaction.setCommissionAmount(BigDecimal.valueOf(systemCommission));

				transaction.setOrder(order);
				transactionRepository.save(transaction);
			}

			resultMap.put("message", "Order completed successfully..!!");
			resultMap.put("result", "success");

			// webhookService.executeMerchantWebhook(order.getOrderId());
			return resultMap;
		} else {
			resultMap.put("result", "failed");
			String message = "Not order found with the order Number: " + orderRequest.getOrderNumber();
			log.error(message);
			resultMap.put("errorMessage", message);
			return resultMap;
		}
	}

	public Map<String, Object> assignOrder(OrderRequest orderRequest) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (orderRequest.getId() == null || orderRequest.getMemberId() == null) {
			resultMap.put("result", "failed");
			log.error("OrderId and memberId must be required");
			resultMap.put("errorMessage", "OrderId and memberId must be required");
			return resultMap;
		}

		Optional<Order> checkOrder = orderRepository.findById(orderRequest.getId());
		if (checkOrder.isPresent()) {
			Order order = checkOrder.get();
			Optional<UserProfile> checkMember = userRepository.findById(orderRequest.getMemberId());
			if (checkMember.isPresent()) {
				order.setMember(checkMember.get());
			} else {
				resultMap.put("result", "failed");
				log.error("Member not found with the member Id: " + orderRequest.getMemberId());
				resultMap.put("errorMessage", "Member not found with the member Id: " + orderRequest.getMemberId());
				return resultMap;
			}

			order.setOrderStatus(OrderStatus.ASSIGNED);
			orderRepository.save(order);
		} else {
			resultMap.put("errorMessage", "Order not found with the order Id: " + orderRequest.getId());
			return resultMap;
		}
		resultMap.put("result", "success");
		return resultMap;

	}

	public Long getEligibleMemberId(BigDecimal amount) {

		Long memberId = null;
		List<Object[]> eligibleMembers = userRepository.getEligibleMembers(amount);
		boolean flag = true;
		for (Object[] obj : eligibleMembers) {
			Long userId = ((BigInteger) obj[0]).longValue();
			// String email = (String) obj[1];
			Date date = (Date) obj[2];
			if (date == null) {
				memberId = userId;
				flag = false;
			}

		}
		if (flag && eligibleMembers != null && eligibleMembers.size() != 0) {
			Object[] objects = eligibleMembers.get(0);
			memberId = ((BigInteger) objects[0]).longValue();
		}
		return memberId;
	}

	public Map<String, Object> savePayOutOrder(OrderRequest orderRequest) {

		// Checked user is already exist or not
		UserProfile user = null;
		if (orderRequest.getUser() != null || orderRequest.getUserId() != null) {
			String email = null;
			if (orderRequest.getUser() != null) {
				email = orderRequest.getUser().getEmailAddress();
			}
			user = userRepository.findByUsernameOrId(email, orderRequest.getUserId());
		}

		Map<String, Object> resultMap = new HashMap<>();

		// If not exist then create new user
		if (user == null && orderRequest.getUser() != null) {
			user = new UserProfile();
			UserRequest userData = orderRequest.getUser();
			user.setFirstName(userData.getFirstName());
			user.setLastName(userData.getLastName());
			user.setEmail(userData.getEmailAddress());
			user.setIsUserActive(true);

			Roles role = roleRepository.findByName(Constants.USER);
			user.setRole(role);
			user.setPassword(passwordEncoder.encode("Test@1234"));
			user.setUsername(userData.getEmailAddress());

			userRepository.save(user);

			// Save user UPI
			userProfileService.saveOrUpdateUPIDetails(userData.getUpiInfo(), user);
		}

		// Insert data into the order table
		Order order = new Order();
		order.setUser(user);

		if (user.getUPIDetails() != null) {
			UPIDetail upiDetails = user.getUPIDetails().get(0);
			order.setUpiDetail(upiDetails);
		}

		order.setOrderNumber(orderRequest.getOrderNumber());

		order.setOrderType(OrderType.PAYOUT);
		order.setSettlementType("API");
		order.setAmount(orderRequest.getAmount());
		Optional<Merchant> checkMerchant = merchantRepository.findById(orderRequest.getMerchantId());
		if (checkMerchant.isPresent()) {
			order.setMerchant(checkMerchant.get());
		} else {
			resultMap.put("result", "failed");
			String message = "Not found the merchant with the merchant Id: " + orderRequest.getMerchantId();
			log.error(message);
			resultMap.put("errorMessage", message);
			return resultMap;
		}

		order.setOrderStatus(OrderStatus.PENDING);
		Long memberId = orderRequest.getMemberId();
		if (memberId != null) {
			UserProfile member = userRepository.findById(memberId).get();
			order.setMember(member);
			order.setOrderStatus(OrderStatus.ASSIGNED);
		}

		String paymentMethod = orderRequest.getPaymentMethod();
		if (PaymentMethod.UPI.getValue().equalsIgnoreCase(paymentMethod)
				|| PaymentMethod.EWALLET.getValue().equalsIgnoreCase(paymentMethod)
				|| PaymentMethod.NETBANKING.getValue().equalsIgnoreCase(paymentMethod)) {
			order.setPaymentMethod(PaymentMethod.UPI);
		} else {
			resultMap.put("result", "failed");
			String message = "No suitable payment method. Payment method must be " + PaymentMethod.UPI + " OR "
					+ PaymentMethod.EWALLET + " OR " + PaymentMethod.NETBANKING;
			resultMap.put("errorMessage", message);
			return resultMap;
		}

		// save order into order table
		orderRepository.save(order);

		OrderRequest orderReq = new OrderRequest();
		setOrderDetails(orderReq, order, false);
		resultMap.put("orderDetails", orderReq);
		resultMap.put("result", "success");
		return resultMap;
	}

	/*public Map<String, Object> saveAdminOrder(OrderRequest orderRequest) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (orderRequest.getEmail() != null && orderRequest.getUserId() != null) {
			log.error("User email or userId is mandatory");
			resultMap.put("errorMessage", "User email or userId is mandatory");
			resultMap.put("result", "failed");
			return resultMap;
		}

		UserProfile user = null;
		if (orderRequest.getEmail() != null) {
			user = userRepository.findByUsername(orderRequest.getEmail());
		} else {
			user = userRepository.findById(orderRequest.getUserId()).get();
		}

		// Insert data into the order table
		Order order = new Order();
		order.setOrderNumber(orderRequest.getOrderNumber());

		String orderType = orderRequest.getOrderType();

		if (OrderType.PAYIN.toString().equalsIgnoreCase(orderType)) {
			order.setOrderType(OrderType.PAYIN);
		} else if (OrderType.PAYOUT.toString().equalsIgnoreCase(orderType)) {
			order.setOrderType(OrderType.PAYOUT);
		} else {
			resultMap.put("result", "failed");
			String message = "No suitable order type. Order Type must be " + OrderType.PAYIN + " OR "
					+ OrderType.PAYOUT;
			resultMap.put("errorMessage", message);
			return resultMap;
		}

		order.setAmount(orderRequest.getAmount());
		Optional<Merchant> checkMerchant = merchantRepository.findById(orderRequest.getMerchantId());
		if (checkMerchant.isPresent()) {
			order.setMerchant(checkMerchant.get());
		}

		order.setUser(user);

		order.setOrderStatus(OrderStatus.PENDING);
		if (orderRequest.getMemberId() != null) {
			UserProfile member = userRepository.findById(orderRequest.getMemberId()).get();
			order.setOrderStatus(OrderStatus.ASSIGNED);
			order.setMember(member);
		}

		String paymentMethod = orderRequest.getPaymentMethod();
		if (PaymentMethod.UPI.toString().equalsIgnoreCase(paymentMethod)
				|| PaymentMethod.EWALLET.toString().equalsIgnoreCase(paymentMethod)
				|| PaymentMethod.NETBANKING.toString().equalsIgnoreCase(paymentMethod)) {
			order.setPaymentMethod(PaymentMethod.valueOf(orderRequest.getPaymentMethod()));
		} else {
			resultMap.put("result", "failed");
			String message = "No suitable payment method. Payment method must be " + PaymentMethod.UPI + " OR "
					+ PaymentMethod.EWALLET + " OR " + PaymentMethod.NETBANKING;
			log.error(message);
			resultMap.put("errorMessage", message);
			return resultMap;
		}

		// save order into order table
		orderRepository.save(order);

		OrderRequest orderReq = new OrderRequest();
		OrderRequest orderDetails = setOrderDetails(orderReq, order);
		resultMap.put("orderDetails", orderDetails);
		resultMap.put("result", "success");
		return resultMap;
	}*/

	public OrderRequest setOrderDetails(OrderRequest orderReq, Order order, boolean list) {

		// Set orderNumber, userFullname, merchantName, memberName, dateCreated, amount,
		// paymentMethod and orderType
		UserProfile user = order.getUser();
		orderReq.setId(order.getOrderId());
		orderReq.setOrderNumber(order.getOrderNumber());
		if (user != null) {
			UserRequest userProfile = new UserRequest();
			commonService.setUserDetails(userProfile, user, list);
			orderReq.setUser(userProfile);
		}

		Merchant merchant = order.getMerchant();
		if (merchant != null) {
			orderReq.setMerchantName(merchant.getName());
		}

		UserProfile member = order.getMember();
		if (member != null) {
			UserRequest memberProfile = new UserRequest();
			commonService.setUserDetails(memberProfile, member, list);
			orderReq.setMember(memberProfile);
		}

		if (!list) {
			orderReq.setTransactionReceipt(order.getReceiptUrl());
		}

		orderReq.setRejectionComment(order.getRejectionComment());
		orderReq.setDateCreated(order.getDateCreated().getTime());
		orderReq.setAmount(order.getAmount());
		orderReq.setPaymentMethod(order.getPaymentMethod().toString());
		orderReq.setOrderType(order.getOrderType().toString());
		orderReq.setOrderStatus(order.getOrderStatus().toString());
		orderReq.setTransactionNumber(order.getTransactionNumber());
		orderReq.setSettlementType(order.getSettlementType());

		UPIDetail upiDetail = null;
		if (order.getUpiDetail() != null) {
			Long upiDetailId = order.getUpiDetail().getUpiDetailsId();
			Optional<UPIDetail> checkUipDetails = upiDetailRepository.findById(upiDetailId);
			if (checkUipDetails.isPresent()) {
				upiDetail = checkUipDetails.get();
			}
		}

		if (upiDetail != null) {
			if ("UPI".equalsIgnoreCase(upiDetail.getPaymentType().toString())) {
				orderReq.setAccountInfo("UPI - " + upiDetail.getUpi());
			} else if ("NETBANKING".equalsIgnoreCase(upiDetail.getPaymentType().toString())) {
				orderReq.setAccountInfo(upiDetail.getBankName() + " - " + upiDetail.getAccountNumber());
			} else if ("EWALLET".equalsIgnoreCase(upiDetail.getPaymentType().toString())) {
				orderReq.setAccountInfo(upiDetail.getEwallet() + " - " + upiDetail.getMobileNumber());
			}
		}

		if (order.getOrderStatus().equals(OrderStatus.COMPLETED)) {
			orderReq.setPaymentStatus("SUCCESS");
		}else if (order.getOrderStatus().equals(OrderStatus.REJECTED))  {
			orderReq.setPaymentStatus("FAILED");
		}

		if (order.getOrderStatus().equals(OrderStatus.COMPLETED)) {
			orderReq.setPaymentAmount(order.getAmount());
			if (order.getDateCompleted() != null) {
				orderReq.setCompletedDate(order.getDateCompleted().getTime());
			}
			if (order.getOrderType().toString().equalsIgnoreCase(OrderType.PAYIN.toString())) {
				// fees
				BigDecimal merchantFee = BigDecimal.valueOf((2 * order.getAmount().doubleValue()) / 100);
				orderReq.setMerchantFee(merchantFee);
				// merchant Income
				orderReq.setMerchantIncome(order.getAmount().subtract(merchantFee));
			} else if (order.getOrderType().toString().equalsIgnoreCase(OrderType.PAYOUT.toString())) {
				// fees
				BigDecimal merchantFee = BigDecimal.valueOf((2 * order.getAmount().doubleValue()) / 100);
				orderReq.setMerchantFee(merchantFee);
				// merchant Income
				orderReq.setMerchantIncome(
						BigDecimal.valueOf(-(order.getAmount().doubleValue() + merchantFee.doubleValue())));
			}
		}

		return orderReq;

	}

	@SuppressWarnings("unchecked")
	public List<OrderRequest> getOrders(Long id, String type, String status, String orderType) {

		List<Order> orders = orderRepository.getOrders(id, type, status, orderType);
		List<OrderRequest> orderResponseList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(orders))
			orders.stream().forEach(order -> {
				OrderRequest orderRequest = new OrderRequest();
				setOrderDetails(orderRequest, order, true);
				orderResponseList.add(orderRequest);
			});

		return orderResponseList;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getOrderStats(String type, Long id) {
		HashMap<String, Object> orderStats = new HashMap<>();
		String today = DateUtils.todayShortDbFormat();
		String yesterday = DateUtils.addRemoveDaysFromToday(-1);

		Integer orderCountToday = orderRepository.getOrderCountByTypeAndDate(id, type, today);
		Integer orderCountYesterday = orderRepository.getOrderCountByTypeAndDate(id, type, yesterday);
		Double orderIncomeToday = orderRepository.getOrderIncomeByTypeAndDate(id, type, today);
		Double orderIncomeYesterday = orderRepository.getOrderIncomeByTypeAndDate(id, type, yesterday);
		Double currentBalance = orderRepository.getCurrentBalance(id, type);

		orderStats.put("orderCountToday", orderCountToday);
		orderStats.put("orderCountYesterday", orderCountYesterday);
		orderStats.put("orderIncomeToday", orderIncomeToday);
		orderStats.put("orderIncomeYesterday", orderIncomeYesterday);
		orderStats.put("currentBalance", currentBalance);

		return orderStats;
	}

	@SuppressWarnings("unchecked")
	public List<Object> getRecentOrder(String type, Long id) {
		List<Order> orders = orderRepository.getOrderRecentByIdAndType(id, type);

		List<Object> ordersList = new ArrayList<Object>();
		for (Order order : orders) {

			Map<String, Object> resultMap = new HashMap<String, Object>();

			resultMap.put("id", order.getOrderId());
			resultMap.put("amount", order.getAmount());
			resultMap.put("status", order.getOrderStatus().toString());
			UserProfile user = order.getUser();
			if (user != null) {
				resultMap.put("userId", user.getUserId());
				resultMap.put("userName", user.getFirstName() + " " + user.getLastName());
			}
			ordersList.add(resultMap);
		}
		return ordersList;
	}

	@SuppressWarnings("all")
	public Map<String, Object> getOrderAnalytics(String type, Long id, String timeframe) {

		List<Object[]> Analytic = orderRepository.getOrderAnalyticByTypeAndTimeframe(id, type, timeframe);

		List<BigInteger> payIns = new ArrayList<BigInteger>();
		List<BigInteger> payOuts = new ArrayList<BigInteger>();
		List<BigDecimal> incomes = new ArrayList<BigDecimal>();
		List<Object> xAxis = new ArrayList<Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		for (Object[] objects : Analytic) {
			Date date = (Date) objects[0];

			if (timeframe.equalsIgnoreCase("day")) {
				xAxis.add(new SimpleDateFormat("MM/dd/yyyy").format(date));
			} else if (timeframe.equalsIgnoreCase("month")) {
				Calendar calendar = new GregorianCalendar();
				int month = date.getMonth();
				calendar.setTime(date);
				calendar.set(Calendar.MONTH, month - 1);
				int year = calendar.get(Calendar.YEAR);
				xAxis.add(Calendar.MONTH + "/" + year);

			} else if (timeframe.equalsIgnoreCase("year")) {
				Calendar calendar = new GregorianCalendar();
				calendar.setTime(date);
				int year = calendar.get(Calendar.YEAR);
				xAxis.add("" + year);
			}
			BigInteger payin = (BigInteger) objects[1];
			if (payin == null) {
				payin = new BigInteger(String.valueOf(0));
			}
			payIns.add(payin);
			BigInteger payout = (BigInteger) objects[2];
			if (payout == null) {
				payout = new BigInteger(String.valueOf(0));
			}
			payOuts.add(payout);
			BigDecimal income = (BigDecimal) objects[3];
			if (income == null) {
				income = new BigDecimal(String.valueOf(0));
			}
			incomes.add(income);
		}

		map.put("payIn", payIns);
		map.put("payOut", payOuts);
		map.put("income", incomes);
		map.put("xAxis", xAxis);
		return map;
	}

	public Map<String, Object> createWithdrawOrDepositOrder(OrderRequest orderRequest, String orderType) {
		HashMap<String, Object> resultMap = new HashMap<>();
		try {
			Order order = new Order();

			if (orderType.equalsIgnoreCase("withdraw")) {
				order.setOrderType(OrderType.WITHDRAW);
			} else {
				order.setOrderType(OrderType.DEPOSIT);
			}

			String type = orderRequest.getType();
			long id = orderRequest.getId();
			if (type.equalsIgnoreCase(Constants.MEMBER)) {
				UserProfile userProfile = userRepository.findById(id).get();
				String settlementPassword = orderRequest.getSettlementPassword();

				if (orderRequest.getAccountId() != null) {
					Optional<UPIDetail> checkUpiDetail = upiDetailRepository.findById(orderRequest.getAccountId());
					if (checkUpiDetail.isPresent()) {
						order.setUpiDetail(checkUpiDetail.get());
					} else {
						resultMap.put("result", "failed");
						resultMap.put("errorMessage",
								"UPI details not found for the accountId: " + orderRequest.getAccountId());
						return resultMap;
					}
				} else {
					resultMap.put("result", "failed");
					resultMap.put("errorMessage", "AccountId must be required");
					return resultMap;
				}
				boolean matches = passwordEncoder.matches(settlementPassword, userProfile.getSettlementPassword());
				if(!matches) {
					resultMap.put("result", "failed");
					resultMap.put("errorMessage", "Settlement Password is Wrong, Please try again..!!");
					return resultMap;
				}
				order.setMember(userProfile);
			} else if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN)) {
				Merchant merchant = merchantRepository.findById(id).get();
				UserProfile superUserByMerchantId = userRepository.getSuperUserByMerchantId(merchant.getMerchantId());
				String settlementPassword = orderRequest.getSettlementPassword();
				boolean matches = passwordEncoder.matches(settlementPassword,
						superUserByMerchantId.getSettlementPassword());
				if (!matches) {
					resultMap.put("result", "failed");
					resultMap.put("errorMessage", "Settlement Password is Wrong, Please try again..!!");
					return resultMap;
				}
				order.setMerchant(merchant);
				Optional<MerchantBankAccount> checkBankAccount = merchantBankAccountRepository
						.findById(orderRequest.getAccountId());
				if (checkBankAccount.isPresent()) {
					MerchantBankAccount merchantBankAccount = checkBankAccount.get();
					order.setMerchantBankAccount(merchantBankAccount);
				}

			} else {
				resultMap.put("result", "failed");
				resultMap.put("errorMessage", "Invalid user role");
				return resultMap;
			}

			order.setComment(orderRequest.getComment());
			order.setAmount(orderRequest.getAmount());

			if (orderRequest.getTransactionNumber() != null) {
				order.setTransactionNumber(orderRequest.getTransactionNumber());
			}

			order.setOrderStatus(OrderStatus.PENDING);
			orderRepository.save(order);

			resultMap.put("result", "success");
			resultMap.put("orderId", order.getOrderId());
		} catch (Exception e) {
			log.error("Error creating " + orderType + " order for " + orderRequest.getType() + " id " + orderRequest.getId() + ": " + e.getMessage());
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", "Failed to create " + orderType.toLowerCase() + " request: " + e.getMessage());
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getOrderAnalytics(String type, Long id) {

		HashMap<String, Object> resultMap = new HashMap<>();
		Integer totalOrderCount = orderRepository.getTotalOrderCountByIdAndType(id, type);
		BigDecimal totalOrderAmount = orderRepository.getTotalOrderAmountByIdAndType(id, type);
		BigDecimal totalOrderPaidAmount = orderRepository.getTotalOrderPaidAmountByIdAndType(id, type);
		BigDecimal totalOrderIncome = orderRepository.getTotalOrderIncomeByIdAndType(id, type);
		resultMap.put("totalOrder", totalOrderCount);
		resultMap.put("totalAmount", totalOrderAmount);
		resultMap.put("totalPaidAmount", totalOrderPaidAmount);
		resultMap.put("totalIncome", totalOrderIncome);
		resultMap.put("totalFees", (2 * totalOrderPaidAmount.doubleValue()) / 100);
		return resultMap;
	}
	
	@SuppressWarnings("unchecked")
	public List<OrderRequest> getOpenOrders(String orderType) {

		List<Order> orders = orderRepository.getOpenOrders(orderType);
		List<OrderRequest> orderResponseList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(orders))
			orders.stream().forEach(order -> {
				OrderRequest orderRequest = new OrderRequest();
				setOrderDetails(orderRequest, order, true);
				orderResponseList.add(orderRequest);
			});
		return orderResponseList;
	}

	@SuppressWarnings("unchecked")
	public List<WithdrawAndDepositOrdersRequest> getWithdrawOrders(Long id, String type) {

		List<Order> orders = orderRepository.getWithdrawOrders(id, type);
		List<WithdrawAndDepositOrdersRequest> orderResponseList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(orders))
			orders.stream().forEach(order -> {
				WithdrawAndDepositOrdersRequest withdrawOrdersRequest = new WithdrawAndDepositOrdersRequest();
				setWithdrawOrdersDetails(withdrawOrdersRequest, order, "WITHDRAW");
				orderResponseList.add(withdrawOrdersRequest);
			});
		return orderResponseList;
	}

	public WithdrawAndDepositOrdersRequest setWithdrawOrdersDetails(WithdrawAndDepositOrdersRequest orderReq, Order order, String type) {

		orderReq.setId(order.getOrderId());
		orderReq.setOrderNumber(order.getOrderNumber());
		orderReq.setDateCreated(order.getDateCreated().getTime());
		orderReq.setAmount(order.getAmount());
		orderReq.setOrderType(order.getOrderType().toString());
		orderReq.setOrderStatus(order.getOrderStatus().toString());
		orderReq.setTransactionNumber(order.getTransactionNumber());
		orderReq.setComment(order.getComment());

		if ("WITHDRAW".equalsIgnoreCase(type)) {
			Transaction transaction = transactionRepository
					.getTransactionByOrderIdAndTransactionType(order.getOrderId(), TransactionType.WITHDRAW);
			if (transaction != null) {
				orderReq.setFee(transaction.getFee());
			}
		} else if ("DEPOSIT".equalsIgnoreCase(type)) {
			Transaction transaction = transactionRepository
					.getTransactionByOrderIdAndTransactionType(order.getOrderId(), TransactionType.DEPOSIT);
			if (transaction != null) {
				orderReq.setFee(transaction.getFee());
			}
		}

		MerchantBankAccount merchantBankAccount = order.getMerchantBankAccount();
		if (merchantBankAccount != null) {
			MerchantBankAccountRequest merchantBankAccountRequest = new MerchantBankAccountRequest();
			merchantBankAccountRequest.setId(merchantBankAccount.getMerchantBankAccountId());
			merchantBankAccountRequest.setAccountName(merchantBankAccount.getAccountName());
			merchantBankAccountRequest.setApprovalStatus(merchantBankAccount.getApprovalStatus());
			merchantBankAccountRequest.setBankName(merchantBankAccount.getBankName());
			merchantBankAccountRequest.setDateCreated(merchantBankAccount.getDateCreated().getTime());
			merchantBankAccountRequest.setEmailAddress(merchantBankAccount.getEmailAddress());
			merchantBankAccountRequest.setIfscCode(merchantBankAccount.getIfscCode());
			merchantBankAccountRequest.setPaymentMethod(merchantBankAccount.getPaymentMethod());
			merchantBankAccountRequest.setUpiId(merchantBankAccount.getUpiId());
			orderReq.setBankDetails(merchantBankAccountRequest);
		}
		return orderReq;
	}

	public Map<String, Object> getWithdrawOrderAnalytics(String type, Long id) {

		HashMap<String, Object> resultMap = new HashMap<>();
		if (Constants.MERCHANT_ADMIN.equalsIgnoreCase(type) || Constants.MERCHANT_ADMIN.equalsIgnoreCase(type)) {
			Integer totalWithdrawOrderCount = orderRepository.getTotalWithdrawOrderCountByMerchantId(id);
			BigDecimal totalWithdrawOrderAmount = orderRepository.getTotalWithdrawOrderAmountByMerchant(id);
			BigDecimal totalWithdrawPaidOrderAmount = orderRepository.getTotalWithdrawPaidOrderAmountByMerchant(id);
			BigDecimal totalWithdrawUnPaidOrderAmount = orderRepository.getTotalWithdrawUnPaidOrderAmountByMerchant(id);
			if (totalWithdrawOrderCount != null) {
				resultMap.put("totalOrder", totalWithdrawOrderCount);
			} else {
				resultMap.put("totalOrder", 0);
			}
			if (totalWithdrawOrderAmount != null) {
				resultMap.put("totalAmount", totalWithdrawOrderAmount);
			} else {
				resultMap.put("totalAmount", 0.0);
			}
			if (totalWithdrawPaidOrderAmount != null) {
				resultMap.put("totalPaidAmount", totalWithdrawPaidOrderAmount);
			} else {
				resultMap.put("totalPaidAmount", 0.0);
			}
			if (totalWithdrawUnPaidOrderAmount != null) {
				resultMap.put("totalUnPaidAmount", totalWithdrawUnPaidOrderAmount);
			} else {
				resultMap.put("totalUnPaidAmount", 0.0);

			}
		}
		return resultMap;
	}

	public Map<String, Object> updateOrderAmount(OrderRequest orderRequest) {

		Optional<Order> checkOrder = orderRepository.findById(orderRequest.getId());
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (checkOrder.isPresent()) {
			Order order = checkOrder.get();
			OrderStatus orderStatus = order.getOrderStatus();
			if (!OrderStatus.COMPLETED.toString().equalsIgnoreCase(orderStatus.toString())) {
				if (orderRequest.getAmount().doubleValue() > 0.0d) {
					BigDecimal oldAmount = order.getAmount();
					order.setAmount(orderRequest.getAmount());
					orderRepository.save(order);
					resultMap.put("result", "success");
					resultMap.put("message", "Amount updated successfully");
					log.debug("Updated order amount " + oldAmount + " to " + orderRequest.getAmount() + " for order Id: " + order.getOrderId());
				} else {
					resultMap.put("result", "failed");
					resultMap.put("errorMessage", "Order amount must be grether than zero");
				}
			} else {
				resultMap.put("result", "failed");
				resultMap.put("errorMessage", "We can not update order amount because order status is: " + orderStatus);
			}
		} else {
			resultMap.put("result", "failed");
			resultMap.put("errorMessage", "Not found order with the order id: " + orderRequest.getId());
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	public List<WithdrawAndDepositOrdersRequest> getDepositOrders(Long id, String type) {

		List<Order> orders = orderRepository.getDepositOrders(id, type);
		List<WithdrawAndDepositOrdersRequest> orderResponseList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(orders))
			orders.stream().forEach(order -> {
				WithdrawAndDepositOrdersRequest depositOrdersRequest = new WithdrawAndDepositOrdersRequest();
				setWithdrawOrdersDetails(depositOrdersRequest, order, "DEPOSIT");
				orderResponseList.add(depositOrdersRequest);
			});
		return orderResponseList;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMemberProfileDetails(Long id, String type) {
		HashMap<String, Object> orderStats = new HashMap<>();
		String today = DateUtils.todayShortDbFormat();
		String yesterday = DateUtils.addRemoveDaysFromToday(-1);

		Double orderIncomeToday = orderRepository.getOrderIncomeByTypeAndDate(id, type, today);
		Double orderIncomeYesterday = orderRepository.getOrderIncomeByTypeAndDate(id, type, yesterday);
		Double balance = orderRepository.getCurrentBalance(id, type);
		BigDecimal yestedayBalance = transactionRepository.getMemberBalanceByDate(id, yesterday);
		BigDecimal totalOrderIncome = orderRepository.getTotalOrderIncomeByIdAndType(id, type);
		orderStats.put("TodayIncome", orderIncomeToday);
		orderStats.put("YesterdayIncome", orderIncomeYesterday);
		orderStats.put("TodayBalance", balance);
		if (yestedayBalance != null) {
			orderStats.put("YesterdayBalance", yestedayBalance);
		} else {
			orderStats.put("YesterdayBalance", 0d);
		}
		orderStats.put("quotaAmount", balance);
		orderStats.put("commission", totalOrderIncome);
		orderStats.put("memberAcceptOrder", userRepository.findById(id).get().getMemberAcceptOrder());

		return orderStats;
	}

	public Map<String, Object> getDepositOrderAnalytics(String type, Long id) {

		HashMap<String, Object> resultMap = new HashMap<>();
		if (Constants.MERCHANT_ADMIN.equalsIgnoreCase(type) || Constants.MERCHANT_ADMIN.equalsIgnoreCase(type)) {
			Integer totalDepositOrderCount = orderRepository.getTotalDepositOrderCountByMerchantId(id);
			BigDecimal totalDepositOrderAmount = orderRepository.getTotalDepositOrderAmountByMerchant(id);
			BigDecimal totalDepositPaidOrderAmount = orderRepository.getTotalDepositPaidOrderAmountByMerchant(id);
			BigDecimal totalDepositUnPaidOrderAmount = orderRepository.getTotalDepositUnPaidOrderAmountByMerchant(id);
			if (totalDepositOrderCount != null) {
				resultMap.put("totalOrder", totalDepositOrderCount);
			} else {
				resultMap.put("totalOrder", 0);
			}
			if (totalDepositOrderAmount != null) {
				resultMap.put("totalAmount", totalDepositOrderAmount);
			} else {
				resultMap.put("totalAmount", 0.0);
			}
			if (totalDepositPaidOrderAmount != null) {
				resultMap.put("totalPaidAmount", totalDepositPaidOrderAmount);
			} else {
				resultMap.put("totalPaidAmount", 0.0);
			}
			if (totalDepositUnPaidOrderAmount != null) {
				resultMap.put("totalUnPaidAmount", totalDepositUnPaidOrderAmount);
			} else {
				resultMap.put("totalUnPaidAmount", 0.0);
			}
		}
		return resultMap;
	}
}
