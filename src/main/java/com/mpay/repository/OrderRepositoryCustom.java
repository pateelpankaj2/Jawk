package com.mpay.repository;

import java.math.BigDecimal;
import java.util.List;

import com.mpay.model.Order;

@SuppressWarnings("hiding")
public interface OrderRepositoryCustom<Long, Order> {

	public List<Order> getOrders(Long id, String type, String status, String orderType);

	public Integer getOrderCountByTypeAndDate(Long id, String type, String date);

	public Double getOrderIncomeByTypeAndDate(Long id, String type, String date);

	public Double getCurrentBalance(Long id, String type);

	public List<Order> getOrderRecentByIdAndType(Long id, String type);

	public List<Object[]> getOrderAnalyticByTypeAndTimeframe(Long id, String type, String timeframe);

	public Integer getTotalOrderCountByIdAndType(Long id, String type);

	public BigDecimal getTotalOrderAmountByIdAndType(Long id, String type);

	public BigDecimal getTotalOrderPaidAmountByIdAndType(Long id, String type);

	public BigDecimal getTotalOrderIncomeByIdAndType(Long id, String type);
	
	public List<Order> getOpenOrders(String orderType);

	public List<Order> getWithdrawOrders(Long id, String type);

	public List<Order> getDepositOrders(Long id, String type);

}
