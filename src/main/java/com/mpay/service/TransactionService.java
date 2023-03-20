package com.mpay.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mpay.dto.MemberTransactionResponse;
import com.mpay.dto.MerchantTransactionResponse;
import com.mpay.dto.TrasactionHistoryResponse;
import com.mpay.enums.OrderType;
import com.mpay.enums.WalletType;
import com.mpay.model.Order;
import com.mpay.model.Transaction;
import com.mpay.model.UserProfile;
import com.mpay.repository.TransactionRepository;
import com.mpay.repository.UserRepository;
import com.mpay.repository.WalletRepository;
import com.mpay.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class TransactionService {

	@Autowired
	TransactionRepository transactionRepository;
	
	@Autowired
	UserRepository userRepository;

	@Autowired
	WalletRepository walletRepository;

	public List<MerchantTransactionResponse> getMerchantTransactions(String type, Long id) {

		List<MerchantTransactionResponse> transactionList = new ArrayList<MerchantTransactionResponse>();

		List<Transaction> merchantTransactions = null;
		if (type == null || Constants.SUPER_ADMIN.equalsIgnoreCase(type)) {
			merchantTransactions = transactionRepository.getTransactionsByWalletType(WalletType.MERCHANT);
		} else if (Constants.MERCHANT_ADMIN.equalsIgnoreCase(type)
				|| Constants.MERCHANT_SUBACCOUNT.equalsIgnoreCase(type)) {
			Optional<UserProfile> checkUser = userRepository.findById(id);
			if (checkUser.isPresent()) {
				UserProfile userProfile = checkUser.get();
				if (userProfile.getMerchant() != null) {
					merchantTransactions = transactionRepository.getTransactionByMerchantIdAndWalletType(
							userProfile.getMerchant().getMerchantId(), WalletType.MERCHANT);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(merchantTransactions)) {
			merchantTransactions.stream().forEach(transation -> {
				MerchantTransactionResponse merchantTransaction = new MerchantTransactionResponse();

				if (transation.getMerchant() != null) {
					merchantTransaction.setMerchant(transation.getMerchant().getName());
				}

				Order order = transation.getOrder();
				if (order != null) {
					merchantTransaction.setOrderNumber(order.getOrderNumber());
					merchantTransaction.setAmount(order.getAmount());
					merchantTransaction.setOrderType(order.getOrderType().toString());
					merchantTransaction.setPaymentMethod(order.getPaymentMethod().toString());
					double merchantIncome = (2*order.getAmount().doubleValue())/100;

					if (order.getOrderType().toString().equalsIgnoreCase(OrderType.PAYIN.toString())) {
						merchantTransaction.setMerchantIncome(
								BigDecimal.valueOf(order.getAmount().doubleValue() - merchantIncome));
					} else {
						merchantTransaction.setMerchantIncome(
								BigDecimal.valueOf(-(order.getAmount().doubleValue() + merchantIncome)));
					}
				}

				// set User id and name
				if (transation.getOrder() != null && transation.getOrder().getUser() != null) {
					UserProfile user = transation.getOrder().getUser();
					merchantTransaction.setUserId(user.getUserId());
					merchantTransaction.setUserName(user.getFirstName() + " " + user.getLastName());
				}

				merchantTransaction.setId(transation.getTransactionId());
				merchantTransaction.setBalanceBefore(transation.getBalanceBefore());
				merchantTransaction.setCommission(transation.getCommissionAmount());
				merchantTransaction.setBalanceAfter(transation.getBalanceAfter());
				merchantTransaction.setCreatedDate(transation.getDateCreated());
				merchantTransaction.setMerchantFee(transation.getCommissionAmount());
				transactionList.add(merchantTransaction);
			});
		}
		log.debug("Fetched all transaction for the merchants");
		return transactionList;

	}

	public List<MemberTransactionResponse> getMemberTransactions(String type, Long id) {

		List<MemberTransactionResponse> transactionList = new ArrayList<MemberTransactionResponse>();
		List<Transaction> memberTransactions = null;
		if (Constants.SUPER_ADMIN.equalsIgnoreCase(type) || type == null) {
			memberTransactions = transactionRepository.getTransactionsByWalletType(WalletType.MEMBER);
		}  else if(Constants.MEMBER.equalsIgnoreCase(type)){
			memberTransactions = transactionRepository.getTransactionByMemberIdAndWalletType(id, WalletType.MEMBER);
		}

		if (CollectionUtils.isNotEmpty(memberTransactions)) {
			memberTransactions.stream().forEach(transation -> {
				MemberTransactionResponse memberTransaction = new MemberTransactionResponse();

				UserProfile member = transation.getMember();
				if (member != null) {
					memberTransaction.setMemberId(member.getUserId());
					memberTransaction.setMemberName(member.getFirstName() + " " + member.getLastName());

				}

				Order order = transation.getOrder();
				if (order != null) {
					memberTransaction.setOrderNumber(order.getOrderNumber());
					memberTransaction.setAmount(order.getAmount());
					memberTransaction.setOrderType(order.getOrderType().toString());
					memberTransaction.setPaymentMethod(order.getPaymentMethod().toString());
					if (order.getMerchant() != null) {
						memberTransaction.setMerchantName(order.getMerchant().getName());
					}

					if (order.getOrderType().toString().equalsIgnoreCase(OrderType.PAYIN.toString())) {
						memberTransaction.setMemberIncome(BigDecimal.valueOf(-order.getAmount().doubleValue()));
					} else {
						memberTransaction.setMemberIncome(order.getAmount());
					}
				}

				//set User id and name
				if (transation.getOrder() != null && transation.getOrder().getUser() != null) {
					UserProfile user = transation.getOrder().getUser();
					memberTransaction.setUserId(user.getUserId());
					memberTransaction.setUserName(user.getFirstName() + " " + user.getLastName());
				}

				memberTransaction.setId(transation.getTransactionId());
				memberTransaction.setBalanceBefore(transation.getBalanceBefore());
				memberTransaction.setBalanceAfter(transation.getBalanceAfter());
				memberTransaction.setCommission(transation.getCommissionAmount());
				memberTransaction.setCreatedDate(transation.getDateCreated());
				transactionList.add(memberTransaction);
			});
		}
		log.debug("Fetched all transaction for the members");
		return transactionList;

	}

	public Map<String, Object> getSystemEarnings() {
		BigDecimal systemCommission = walletRepository.getSystemCommission();
		Map<String, Object> resultMap = new HashedMap<String, Object>();
		resultMap.put("systemEarnings", systemCommission.doubleValue());
		return resultMap;
	}

	public List<TrasactionHistoryResponse> getWithdrawAndDepositTransactions(Long id, String type) {

		List<TrasactionHistoryResponse> transactionList = new ArrayList<TrasactionHistoryResponse>();

		List<Transaction> transactionsList = null;
		if (Constants.MEMBER.equalsIgnoreCase(type)) {
			transactionsList = transactionRepository.getWithdrawTransactionsByMemberId(WalletType.MEMBER, id);
		} else if (Constants.MERCHANT_ADMIN.equalsIgnoreCase(type) || Constants.MERCHANT_SUBACCOUNT.equalsIgnoreCase(type)) {
			transactionsList = transactionRepository.getWithdrawAndDepositTransactionsByMerchantId(WalletType.MERCHANT, id);
		}

		if (CollectionUtils.isNotEmpty(transactionsList)) {
			transactionsList.stream().forEach(transaction -> {
				TrasactionHistoryResponse historyResponse = new TrasactionHistoryResponse();
				historyResponse.setId(transaction.getTransactionId());

				Order order = transaction.getOrder();
				if (order != null) {
					historyResponse.setAmount(order.getAmount());
					historyResponse.setOrderType(order.getOrderType());
					historyResponse.setDetails(order.getComment());
				}

				historyResponse.setPreBalance(transaction.getBalanceBefore());
				historyResponse.setCurrentBalance(transaction.getBalanceAfter());
				historyResponse.setDateCreated(transaction.getDateCreated().getTime());
				if (transaction.getFee() != null) {
					historyResponse.setIncome(transaction.getFee());
				} else {
					historyResponse.setIncome(new BigDecimal(0d));
				}
				transactionList.add(historyResponse);
			});
		}
		return transactionList;
	}
}
