package com.mpay.repository;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;
import com.mpay.enums.OrderStatus;
import com.mpay.enums.OrderType;
import com.mpay.model.Order;
import com.mpay.util.Constants;

@Repository
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom<Long, Order> {

	@PersistenceContext
	EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public List<Order> getOrders(Long id, String type, String status, String orderType) {

		String sql = "select r from Order r where (r.orderType = 'PAYIN' OR r.orderType = 'PAYOUT')";

		if (status != null && !status.equalsIgnoreCase("all")) {
			sql += " and r.orderStatus = :status";

		}

		StringBuilder queryBuilder = new StringBuilder(sql);
		if (type != null && type.equalsIgnoreCase(Constants.MEMBER)) {

			queryBuilder.append("  and r.member.userId = :id");

			if (OrderType.PAYIN.toString().equalsIgnoreCase(orderType)) {
				queryBuilder.append(" and r.orderType = 'PAYIN'");
			} else if (OrderType.PAYOUT.toString().equalsIgnoreCase(orderType)) {
				queryBuilder.append(" and r.orderType = 'PAYOUT'");
			}
		} else if (type != null && (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN)
				|| type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))) {

			queryBuilder.append(" and r.merchant.merchantId = :id");

			if (OrderType.PAYIN.toString().equalsIgnoreCase(orderType)) {
				queryBuilder.append(" and r.orderType = 'PAYIN'");
			} else if (OrderType.PAYOUT.toString().equalsIgnoreCase(orderType)) {
				queryBuilder.append(" and r.orderType = 'PAYOUT'");
			}
		}

        if (type != null && type.equalsIgnoreCase(Constants.SUPER_ADMIN) && (status == null || status.equalsIgnoreCase("all"))) {
            if (OrderType.PAYIN.toString().equalsIgnoreCase(orderType)) {
                queryBuilder.append(" and r.orderType = 'PAYIN'");
            } else if (OrderType.PAYOUT.toString().equalsIgnoreCase(orderType)) {
                queryBuilder.append(" and r.orderType = 'PAYOUT'");
            }
        } else {
            if (OrderType.PAYIN.toString().equalsIgnoreCase(orderType)) {
                queryBuilder.append(" and r.orderType = 'PAYIN'");
            } else if (OrderType.PAYOUT.toString().equalsIgnoreCase(orderType)) {
                queryBuilder.append(" and r.orderType = 'PAYOUT'");
            }
        }

		queryBuilder.append(" order by r.orderStatus desc");
		Query nativeQuery = entityManager.createQuery(queryBuilder.toString());

		if (id != null && !type.equalsIgnoreCase(Constants.SUPER_ADMIN)) {
			nativeQuery.setParameter("id", id);
		}

		if (status != null && !status.equalsIgnoreCase("all")) {
			nativeQuery.setParameter("status", OrderStatus.valueOf(status.toUpperCase()));
		}
		return nativeQuery.getResultList();
	}

	@Override
	public Integer getOrderCountByTypeAndDate(Long id, String type, String date) {

		StringBuilder queryBuilder = new StringBuilder(
				"select count(*) from orders o where  (o.ORDER_TYPE= 'PAYIN' OR o.ORDER_TYPE= 'PAYOUT') and date(o.date_created)=  date(:dateParam)");

		if (type.equalsIgnoreCase(Constants.MEMBER))
			queryBuilder.append(" and o.member_id=:memberId ");

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			queryBuilder.append(" and o.merchant_id=:merchantId");

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString());
		nativeQuery.setParameter("dateParam", date);

		if (type.equalsIgnoreCase(Constants.MEMBER))
			nativeQuery.setParameter("memberId", id);

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			nativeQuery.setParameter("merchantId", id);

		Integer orderCount = ((Number) nativeQuery.getSingleResult()).intValue();
		return orderCount;

	}

	@Override
	public Double getOrderIncomeByTypeAndDate(Long id, String type, String date) {

		StringBuilder queryBuilder = new StringBuilder();

		if(type.equalsIgnoreCase(Constants.SUPER_ADMIN))
			queryBuilder.append(" select sum(t.commission_amount) from transaction t where  date(t.date_created) = DATE(:dateParam) and t.wallet_type='PAYMENT_SYSTEM' and t.transaction_type not in ('DEPOSIT', 'WITHDRAW') ");
		if (type.equalsIgnoreCase(Constants.MEMBER))
			queryBuilder.append(" select sum(t.commission_amount) from transaction t where  date(t.date_created) = DATE(:dateParam) and t.member_id=:memberId and t.transaction_type not in ('DEPOSIT', 'WITHDRAW')");

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.contentEquals(Constants.MERCHANT_SUBACCOUNT))
			queryBuilder.append("select  (sum(t.balance_after) - sum(t.balance_before)) from transaction t where  date(t.date_created) = DATE(:dateParam) and t.merchant_id=:merchantId and t.transaction_type not in ('DEPOSIT', 'WITHDRAW')");

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString());
		nativeQuery.setParameter("dateParam", date);

		if (type.equalsIgnoreCase(Constants.MEMBER))
			nativeQuery.setParameter("memberId", id);

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.contentEquals(Constants.MERCHANT_SUBACCOUNT))
			nativeQuery.setParameter("merchantId", id);

		Double orderIncome = 0.0d;
		if (nativeQuery.getSingleResult() != null) {
			orderIncome = ((Number) nativeQuery.getSingleResult()).doubleValue();
		}
		return orderIncome;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Order> getOrderRecentByIdAndType(Long id, String type) {

		StringBuilder queryBuilder = new StringBuilder("select r.* from orders r where (r.ORDER_TYPE = 'PAYIN' OR r.ORDER_TYPE = 'PAYOUT')");

		if (type.equalsIgnoreCase(Constants.MEMBER)) {
			queryBuilder.append(" and r.MEMBER_ID= :memberId");
		} else if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT)) {
			queryBuilder.append(" and r.MERCHANT_ID = :merchantId");
		}

		queryBuilder.append(" order by r.DATE_CREATED desc limit 10");

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString(), Order.class);

		if (type.equalsIgnoreCase(Constants.MEMBER)) {
			nativeQuery.setParameter("memberId", id);
		} else if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT)) {
			nativeQuery.setParameter("merchantId", id);
		}
		return nativeQuery.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getOrderAnalyticByTypeAndTimeframe(Long id, String type, String timeframe) {

		StringBuilder queryBuilder = new StringBuilder("SELECT date_trunc(:timeframe, o.date_created) as datecreated, "
				+ "count(distinct o.order_id) filter (where o.order_type = 'PAYIN') as payin, "
				+ "count(distinct o.order_id) filter (where o.order_type = 'PAYOUT') as payout, " + "n.income "
				+ "from orders o " + "left join ");

		if (type.equalsIgnoreCase(Constants.SUPER_ADMIN)) {
			if (timeframe.equalsIgnoreCase("DAY")) {
				queryBuilder.append(
						" (select  date(t.date_created) as dc, sum(t.commission_amount) as income from transaction t "
								+ "where t.wallet_type = 'PAYMENT_SYSTEM' group by dc) n on n.dc = date_trunc(:timeframe, o.date_created) "
								+ "where o.order_type in ('PAYIN','PAYOUT') "
								+ "group by datecreated, n.income");
			} else if (timeframe.equalsIgnoreCase("MONTH") || timeframe.equalsIgnoreCase("YEAR")) {
				queryBuilder.append(
						" (select date_trunc(:timeframe, t.date_created) as dc, sum(t.commission_amount) as income from transaction t "
								+ "where t.wallet_type = 'PAYMENT_SYSTEM' group by dc) n on n.dc = date_trunc(:timeframe, o.date_created) "
								+ "where o.order_type in ('PAYIN','PAYOUT')"
								+ "group by datecreated, n.income ");
			}
		} else if (type.equalsIgnoreCase(Constants.MEMBER)) {
			if (timeframe.equalsIgnoreCase("DAY")) {
				queryBuilder.append(
						" (select t.member_id, date(t.date_created) as dc, sum(t.commission_amount) as income from transaction t "
								+ "where t.wallet_type = 'MEMBER' group by t.member_id, dc) n on n.member_id = o.member_id and n.dc = date_trunc(:timeframe, o.date_created) "
								+ "where o.member_id = :memberId and o.order_type in ('PAYIN','PAYOUT') group by datecreated, n.income ");
			} else if (timeframe.equalsIgnoreCase("MONTH") || timeframe.equalsIgnoreCase("YEAR")) {
				queryBuilder.append(
						" (select t.member_id, date_trunc(:timeframe, t.date_created) as dc, sum(t.commission_amount) as income from transaction t "
								+ "where t.wallet_type = 'MEMBER' group by t.member_id, dc) n on n.member_id = o.member_id and n.dc = date_trunc(:timeframe, o.date_created) "
								+ "where o.member_id = :memberId and o.order_type in ('PAYIN','PAYOUT') group by datecreated, n.income");
			}
		} else if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT)) {
			if (timeframe.equalsIgnoreCase("DAY")) {
				queryBuilder.append(
						" (select t.merchant_id, date(t.date_created) as dc, sum(t.balance_after) - sum(t.balance_before) as income from transaction t "
								+ "where t.wallet_type = 'MERCHANT' group by t.merchant_id, dc) n on n.merchant_id = o.merchant_id and n.dc = date_trunc(:timeframe, o.date_created) "
								+ "where o.merchant_id = :merchantId and o.order_type in ('PAYIN','PAYOUT') group by datecreated, n.income");
			} else if (timeframe.equalsIgnoreCase("MONTH") || timeframe.equalsIgnoreCase("YEAR")) {
				queryBuilder.append(
						" (select t.merchant_id, date_trunc(:timeframe, t.date_created) as dc, sum(t.balance_after) - sum(t.balance_before) as income from transaction t "
								+ "where t.wallet_type = 'MERCHANT' group by t.merchant_id, dc) n on n.merchant_id = o.merchant_id and n.dc = date_trunc(:timeframe, o.date_created) "
								+ "where o.merchant_id = :merchantId and o.order_type in ('PAYIN','PAYOUT') group by datecreated, n.income");
			}
		}

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString());

		nativeQuery.setParameter("timeframe", timeframe);
		if (type.equalsIgnoreCase(Constants.MEMBER)) {
			nativeQuery.setParameter("memberId", id);
		} else if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT)) {
			nativeQuery.setParameter("merchantId", id);
		}
		return nativeQuery.getResultList();
	}

	@Override
	public Double getCurrentBalance(Long id, String type) {

		if (!type.equalsIgnoreCase(Constants.MEMBER) && !type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) && !type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT)) {
			return 0.0d;
		}

		StringBuilder queryBuilder = new StringBuilder("select w.balance_amount from wallet w");

		if (type.equalsIgnoreCase(Constants.MEMBER)) {
			queryBuilder.append(" where w.wallet_type = 'MEMBER' and member_id= :memberId");
		} else if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT)) {
			queryBuilder.append(" where w.wallet_type='MERCHANT' and w.merchant_id = :merchantId");
		}

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString());

		if (type.equalsIgnoreCase(Constants.MEMBER)) {
			nativeQuery.setParameter("memberId", id);
		} else if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT)) {
			nativeQuery.setParameter("merchantId", id);
		}

		BigDecimal balanceAmount = (BigDecimal) nativeQuery.getSingleResult();
		return balanceAmount.doubleValue();
	}

	@Override
	public Integer getTotalOrderCountByIdAndType(Long id, String type) {

		StringBuilder queryBuilder = new StringBuilder("select count(*) from orders o");

		if (type.equalsIgnoreCase(Constants.MEMBER))
			queryBuilder.append(" where o.member_id=:memberId and (o.ORDER_TYPE = 'PAYIN' OR o.ORDER_TYPE = 'PAYOUT')");

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			queryBuilder.append(" where o.merchant_id=:merchantId and (o.ORDER_TYPE = 'PAYIN' OR o.ORDER_TYPE = 'PAYOUT')");

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString());

		if (type.equalsIgnoreCase(Constants.MEMBER))
			nativeQuery.setParameter("memberId", id);

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			nativeQuery.setParameter("merchantId", id);

		Integer orderCount = 0;
		if (nativeQuery.getSingleResult() != null) {
			orderCount = ((Number) nativeQuery.getSingleResult()).intValue();
		}

		return orderCount;
	}

	@Override
	public BigDecimal getTotalOrderAmountByIdAndType(Long id, String type) {

		StringBuilder queryBuilder = new StringBuilder("select sum(o.amount) from orders o");

		if (type.equalsIgnoreCase(Constants.MEMBER))
			queryBuilder.append(" where o.member_id=:memberId and (o.ORDER_TYPE = 'PAYIN' OR o.ORDER_TYPE = 'PAYOUT')");

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			queryBuilder.append(" where o.merchant_id=:merchantId and (o.ORDER_TYPE = 'PAYIN' OR o.ORDER_TYPE = 'PAYOUT')");

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString());

		if (type.equalsIgnoreCase(Constants.MEMBER))
			nativeQuery.setParameter("memberId", id);

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			nativeQuery.setParameter("merchantId", id);

		Double orderAmount = 0.0d;
		if (nativeQuery.getSingleResult() != null) {
			orderAmount = ((Number) nativeQuery.getSingleResult()).doubleValue();
		}
		return BigDecimal.valueOf(orderAmount);
	}

	@Override
	public BigDecimal getTotalOrderPaidAmountByIdAndType(Long id, String type) {
		StringBuilder queryBuilder = new StringBuilder(
				"select sum(o.amount) from orders o where o.order_status ='COMPLETED'");

		if (type.equalsIgnoreCase(Constants.MEMBER))
			queryBuilder.append(" and o.member_id=:memberId and (o.ORDER_TYPE = 'PAYIN' OR o.ORDER_TYPE = 'PAYOUT')");

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			queryBuilder.append(" and o.merchant_id=:merchantId and (o.ORDER_TYPE = 'PAYIN' OR o.ORDER_TYPE = 'PAYOUT')");

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString());

		if (type.equalsIgnoreCase(Constants.MEMBER))
			nativeQuery.setParameter("memberId", id);

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			nativeQuery.setParameter("merchantId", id);

		Double orderAmount = 0.0d;
		if (nativeQuery.getSingleResult() != null) {
			orderAmount = ((Number) nativeQuery.getSingleResult()).doubleValue();
		}
		return BigDecimal.valueOf(orderAmount);
	}

	@Override
	public BigDecimal getTotalOrderIncomeByIdAndType(Long id, String type) {
		StringBuilder queryBuilder = new StringBuilder();

		if(type.equalsIgnoreCase(Constants.SUPER_ADMIN)) {
			queryBuilder.append("select sum(t.commission_amount) from transaction t");
		}

		if (type.equalsIgnoreCase(Constants.MEMBER))
			queryBuilder.append("select sum(t.commission_amount) from transaction t  where t.member_id=:memberId ");

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			queryBuilder.append("select  (sum(t.balance_after) - sum(t.balance_before)) from transaction t where t.merchant_id=:merchantId");

		Query nativeQuery = entityManager.createNativeQuery(queryBuilder.toString());

		if (type.equalsIgnoreCase(Constants.MEMBER))
			nativeQuery.setParameter("memberId", id);

		if (type.equalsIgnoreCase(Constants.MERCHANT_ADMIN) || type.equalsIgnoreCase(Constants.MERCHANT_SUBACCOUNT))
			nativeQuery.setParameter("merchantId", id);

		Double orderAmount = 0.0d;
		if (nativeQuery.getSingleResult() != null) {
			orderAmount = ((Number) nativeQuery.getSingleResult()).doubleValue();
		}
		return BigDecimal.valueOf(orderAmount);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Order> getOpenOrders(String orderType) {

		String sql = "select r from Order r where r.orderStatus = 'PENDING' and r.member.userId IS NULL";

		if(!orderType.equalsIgnoreCase("all")) {
			if (OrderType.PAYIN.toString().equalsIgnoreCase(orderType)) {
				sql += " and r.orderType = 'PAYIN' and r.member.userId IS NULL";
			} else if (OrderType.PAYOUT.toString().equalsIgnoreCase(orderType)) {
				sql += " and r.orderType = 'PAYOUT' and r.member.userId IS NULL";
			}
		}

		Query query = entityManager.createQuery(sql, Order.class);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Order> getWithdrawOrders(Long id, String type) {
		String sql = "select r from Order r where r.orderType = 'WITHDRAW'";

		if (Constants.MERCHANT_ADMIN.equalsIgnoreCase(type) || Constants.MERCHANT_SUBACCOUNT.equalsIgnoreCase(type)) {
			sql += " and r.merchant.merchantId= :id";
		} else if (Constants.MEMBER.equalsIgnoreCase(type)) {
			sql += " and r.member.userId= :id";
		}

		Query query = entityManager.createQuery(sql, Order.class);
		if (id != null) {
			query.setParameter("id", id);
		}
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Order> getDepositOrders(Long id, String type) {
		String sql = "select r from Order r where r.orderType = 'DEPOSIT'";

		if (Constants.MERCHANT_ADMIN.equalsIgnoreCase(type) || Constants.MERCHANT_SUBACCOUNT.equalsIgnoreCase(type)) {
			sql += " and r.merchant.merchantId= :id";
		} else if (Constants.MEMBER.equalsIgnoreCase(type)) {
			sql += " and r.member.userId= :id";
		}

		Query query = entityManager.createQuery(sql, Order.class);
		if (id != null) {
			query.setParameter("id", id);
		}
		return query.getResultList();
	}
}
