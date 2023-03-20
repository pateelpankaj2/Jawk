package com.mpay.repository;

import com.mpay.enums.TransactionType;
import com.mpay.enums.WalletType;
import com.mpay.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	@Query("from Transaction t where t.walletType = 'MERCHANT'")
	public List<Transaction> getMerchantTransaction();
	
	@Query("from Transaction t where t.walletType = 'MEMBER'")
	public List<Transaction> getMemberTransaction();
	
	@Query("from Transaction t where t.walletType = :walletType and (t.order.orderType ='PAYIN' OR t.order.orderType ='PAYOUT')")
	public List<Transaction> getTransactionsByWalletType(@Param("walletType") WalletType walletType);

	@Query("from Transaction t where t.member.userId = :memberId and t.walletType = :walletType and (t.order.orderType ='PAYIN' OR t.order.orderType ='PAYOUT')")
	public List<Transaction> getTransactionByMemberIdAndWalletType(@Param("memberId") Long memberId, @Param("walletType") WalletType walletType);

	@Query("from Transaction t where t.merchant.merchantId = :merchantId and t.walletType = :walletType and (t.order.orderType ='PAYIN' OR t.order.orderType ='PAYOUT')")
	public List<Transaction> getTransactionByMerchantIdAndWalletType(@Param("merchantId") Long merchantId, @Param("walletType") WalletType walletType);

	@Query("from Transaction t where t.order.orderId = :orderId and t.transactionType = :transactionType")
	public Transaction getTransactionByOrderIdAndTransactionType(@Param("orderId") Long orderId, @Param("transactionType") TransactionType transactionType);

	@Query("from Transaction t where t.walletType = :walletType and (t.transactionType = 'WITHDRAW' or t.transactionType = 'DEPOSIT') and t.merchant.merchantId = :merchantId")
	public List<Transaction> getWithdrawAndDepositTransactionsByMerchantId(@Param("walletType") WalletType walletType, @Param("merchantId") Long merchantId);

	@Query(value="select t.balance_after from transaction t where t.member_id = :memberId and DATE(t.date_created) = date(:date) order by t.transaction_id desc limit 1", nativeQuery = true)
	public BigDecimal getMemberBalanceByDate(@Param("memberId") Long memberId, @Param("date") String date);

	@Query("from Transaction t where t.walletType = :walletType and t.transactionType = 'WITHDRAW' and t.member.userId = :memberId")
	public List<Transaction> getWithdrawTransactionsByMemberId(@Param("walletType") WalletType walletType, @Param("memberId") Long memberId);


}
