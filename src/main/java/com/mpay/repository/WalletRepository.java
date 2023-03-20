package com.mpay.repository;

import com.mpay.enums.WalletType;
import com.mpay.model.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Query("from Wallet w where w.member.userId = :userId")
    public Wallet getMemberWallet(@Param("userId") Long userId);

	@Query("from Wallet w where w.member.userId = :memberId")
	public Optional<Wallet> getWalletByMemberId(@Param("memberId") Long memberId);

	@Query("from Wallet w where w.merchant.merchantId = :merchantId")
	public Optional<Wallet> getWalletByMerchantId(@Param("merchantId") Long merchantId);

	@Query("from Wallet w where w.walletType = :walletType")
	public Optional<Wallet> getWalletForSystem(@Param("walletType") WalletType walletType);

	@Query("from Wallet w where w.balanceAmount >= :amount and w.walletType = 'MEMBER'")
	List<Wallet> getMemberWalletsByAmount(@Param("amount") BigDecimal amount);

	@Query("select w.commissionAmount from Wallet w where w.walletType = 'PAYMENT_SYSTEM'")
	public BigDecimal getSystemCommission();
	
	@Query("select w.balanceAmount from Wallet w where w.member.userId = :memberId")
	public BigDecimal getBalanceAmountByMemberId(@Param("memberId") Long memberId);

	@Query("select w.balanceAmount from Wallet w where w.merchant.merchantId = :merchantId")
	public BigDecimal getBalanceAmountByMerchantId(@Param("merchantId") Long merchantId);
	
}
