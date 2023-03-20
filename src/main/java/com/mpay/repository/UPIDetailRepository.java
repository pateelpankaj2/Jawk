package com.mpay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mpay.model.UPIDetail;

public interface UPIDetailRepository extends JpaRepository<UPIDetail, Long> {

	@Query("from UPIDetail u where userId.userId = :userId")
	public List<UPIDetail> getUipDetailByUserId(@Param("userId") Long userId);

	@Query(nativeQuery = true, value="select distinct upi.upi_detail_id, concat(u.first_name, ' ', u.last_name, ' - ', upi.upi) from upi_detail upi" +
			" inner join user_profile u on u.user_id = upi.user_id" +
			" inner join orders o on o.member_id = upi.user_id" +
			" where o.merchant_id = :merchantId")
	public List<Object[]> getMerchantMemberUPIDetails(@Param("merchantId") long merchantId);

	@Query("from UPIDetail u where u.userId.userId = :userId and u.isDefault = true")
	public Optional<UPIDetail> getDefaultPaymentByUserId(@Param("userId") Long userId);

}
