package com.mpay.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mpay.repository.OrderRepository;
import com.mpay.repository.WalletRepository;
import com.mpay.util.DateUtils;
import com.mpay.dto.MerchantBankAccountRequest;
import com.mpay.model.MerchantBankAccount;
import com.mpay.repository.MerchantBankAccountRepository;
import org.apache.commons.collections4.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	WalletRepository walletRepository;

	@Autowired
	MerchantBankAccountRepository merchantBankAccountRepository;

	@Autowired
	CommonService commonService;

	public Map<String, Object> getAdminOrderStats() {
		HashMap<String, Object> adminOrderStats = new HashMap<>();
		String today = DateUtils.todayShortDbFormat();
		String yesterday = DateUtils.addRemoveDaysFromToday(-1);

		Integer todayPayInOrderCount = orderRepository.adminPayInOrderCountsByDate(today);
		Integer yesterdayPayInOrderCount = orderRepository.adminPayInOrderCountsByDate(yesterday);
		Integer todayPayOutOrderCount = orderRepository.adminPayOutOrderCountsByDate(today);
		Integer yesterdayPayOutOrderCount = orderRepository.adminPayOutOrderCountsByDate(yesterday);

		BigDecimal todayPayInOrderAmount = orderRepository.adminPayInOrderAmountByDate(today);
		if (todayPayInOrderAmount == null) {
			todayPayInOrderAmount = new BigDecimal(0d);
		}
		BigDecimal yesterdayPayInOrderAmount = orderRepository.adminPayInOrderAmountByDate(yesterday);
		if (yesterdayPayInOrderAmount == null) {
			yesterdayPayInOrderAmount = new BigDecimal(0d);
		}
		BigDecimal todayPayOutOrderAmount = orderRepository.adminPayOutOrderAmountByDate(today);
		if (todayPayOutOrderAmount == null) {
			todayPayOutOrderAmount = new BigDecimal(0d);
		}
		BigDecimal yesterdayPayOutOrderAmount = orderRepository.adminPayOutOrderAmountByDate(yesterday);
		if (yesterdayPayOutOrderAmount == null) {
			yesterdayPayOutOrderAmount = new BigDecimal(0d);
		}

		// System total commission
		BigDecimal systemCommission = walletRepository.getSystemCommission();

		adminOrderStats.put("todayPayInOrderCount", todayPayInOrderCount);
		adminOrderStats.put("yesterdayPayInOrderCount", yesterdayPayInOrderCount);
		adminOrderStats.put("todayPayOutOrderCount", todayPayOutOrderCount);
		adminOrderStats.put("yesterdayPayOutOrderCount", yesterdayPayOutOrderCount);

		adminOrderStats.put("todayPayInOrderAmount", todayPayInOrderAmount);
		adminOrderStats.put("yesterdayPayInOrderAmount", yesterdayPayInOrderAmount);
		adminOrderStats.put("todayPayOutOrderAmount", todayPayOutOrderAmount);
		adminOrderStats.put("yesterdayPayOutOrderAmount", yesterdayPayOutOrderAmount);

		adminOrderStats.put("systemEarnings", systemCommission);

		return adminOrderStats;
	}

    public List<MerchantBankAccountRequest> getAllMerchantBankDetails(Long merchantId) {
        List<MerchantBankAccount> merchantBankDetails = new ArrayList<>();
        if (merchantId == null) {
            merchantBankDetails = merchantBankAccountRepository.getAllMerchantBankDetails();
        } else {
            merchantBankDetails = merchantBankAccountRepository.getMerchantBankDetailsByMerchantId(merchantId);
        }
        merchantBankAccountRepository.getMerchantBankDetailsByMerchantId(merchantId);
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

}
