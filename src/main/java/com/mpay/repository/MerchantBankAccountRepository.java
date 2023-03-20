package com.mpay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mpay.model.MerchantBankAccount;

public interface MerchantBankAccountRepository extends JpaRepository<MerchantBankAccount, Long> {
	
	@Query("from MerchantBankAccount m where m.merchant.merchantId = :merchantId ")
	public List<MerchantBankAccount> getMerchantBankDetailsByMerchantId(@Param("merchantId") Long merchantId);

	@Query("from MerchantBankAccount m")
	public List<MerchantBankAccount> getAllMerchantBankDetails();
	
	@Query("select m.merchantBankAccountId, m.bankName, m.upiId from MerchantBankAccount m where m.merchant.merchantId = :merchantId ")
	public List<Object[]> getIdAndAccountNamesByMerchantId(@Param("merchantId") Long merchantId);

}
