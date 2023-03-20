package com.mpay.repository;

import com.mpay.model.Order;
import com.mpay.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public interface OrderRepository  extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    public List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("from Order r where r.orderNumber = :orderNumber and r.merchant.merchantId = :merchantId")
    public Optional<Order> getOrderByOrdeNumberAndMerchant(@Param("orderNumber") String orderNumber, @Param("merchantId") Long merchantId);

    @Query("select count(*) from Order r where r.orderType = 'WITHDRAW' and r.merchant.merchantId = :merchantId ")
    public Integer getTotalWithdrawOrderCountByMerchantId (@Param("merchantId") Long merchantId);

	@Query("select sum(r.amount) from Order r where r.orderType = 'WITHDRAW' and r.merchant.merchantId = :merchantId ")
	public BigDecimal getTotalWithdrawOrderAmountByMerchant(@Param("merchantId") Long merchantId);

	@Query("select sum(r.amount) from Order r where r.orderType = 'WITHDRAW' and r.merchant.merchantId = :merchantId and r.orderStatus = 'COMPLETED'")
	public BigDecimal getTotalWithdrawPaidOrderAmountByMerchant(@Param("merchantId") Long merchantId);

	@Query("select sum(r.amount) from Order r where r.orderType = 'WITHDRAW' and r.merchant.merchantId = :merchantId and r.orderStatus IS NOT 'COMPLETED'")
	public BigDecimal getTotalWithdrawUnPaidOrderAmountByMerchant(@Param("merchantId") Long merchantId);

	@Query("select count(*) from Order r where r.orderType = 'DEPOSIT' and r.merchant.merchantId = :merchantId ")
	public Integer getTotalDepositOrderCountByMerchantId(@Param("merchantId") Long merchantId);

	@Query("select sum(r.amount) from Order r where r.orderType = 'DEPOSIT' and r.merchant.merchantId = :merchantId ")
	public BigDecimal getTotalDepositOrderAmountByMerchant(@Param("merchantId") Long merchantId);

	@Query("select sum(r.amount) from Order r where r.orderType = 'DEPOSIT' and r.merchant.merchantId = :merchantId and r.orderStatus = 'COMPLETED'")
	public BigDecimal getTotalDepositPaidOrderAmountByMerchant(@Param("merchantId") Long merchantId);

	@Query("select sum(r.amount) from Order r where r.orderType = 'DEPOSIT' and r.merchant.merchantId = :merchantId and r.orderStatus IS NOT 'COMPLETED'")
	public BigDecimal getTotalDepositUnPaidOrderAmountByMerchant(@Param("merchantId") Long merchantId);

	@Query(value = "select count(*) from orders o where o.ORDER_TYPE= 'PAYIN' and date(o.date_created)=  date(:dateParam)", nativeQuery = true)
	public Integer adminPayInOrderCountsByDate(@Param("dateParam") String date);

	@Query(value = "select count(*) from orders o where o.ORDER_TYPE= 'PAYOUT' and date(o.date_created)=  date(:dateParam)", nativeQuery = true)
	public Integer adminPayOutOrderCountsByDate(@Param("dateParam") String date);

	@Query(value = "select sum(o.amount) from orders o where o.ORDER_TYPE= 'PAYIN' and date(o.date_created)=  date(:dateParam)", nativeQuery = true)
	public BigDecimal adminPayInOrderAmountByDate(@Param("dateParam") String date);

	@Query(value = "select sum(o.amount) from orders o where o.ORDER_TYPE= 'PAYOUT' and date(o.date_created)=  date(:dateParam)", nativeQuery = true)
	public BigDecimal adminPayOutOrderAmountByDate(@Param("dateParam") String date);
}
