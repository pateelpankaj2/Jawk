package com.mpay.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.mpay.model.UserProfile;

@Repository
public interface UserRepository extends JpaRepository<UserProfile, Long> {
    UserProfile findByUsername(String username);

	@Query("from UserProfile u where u.email = :email OR u.userId = :id")
	UserProfile findByUsernameOrId(@Param("email") String username, @Param("id") Long id);

    @Query("select u.userId, CONCAT(u.firstName,' ', u.lastName), u.email from UserProfile u where u.role.name = :rolename")
    List<Object[]> getUsersByRole(@Param("rolename") String rolename);

    @Query("from UserProfile u where u.merchant.merchantId = :merchantId and u.superUserId.userId IS NULL")
    UserProfile getSuperUserByMerchantId(@Param("merchantId") Long merchantId);

	@Query("from UserProfile u where u.superUserId.userId = :superUserId")
	List<UserProfile> getSubAccounts(@Param("superUserId") long id);

	@Query(value = "select u.user_id, u.email, max(t.date_created) dc from user_profile u inner join roles r on u.role_id = r.role_id "
			+ "inner join wallet w on w.member_id = u.user_id left join transaction t on t.member_id = u.user_id "
			+ "where r.role_name = 'MEMBER' and w.balance_amount >= :amount group by u.email, u.user_id "
			+ "order by dc, u.user_id ", nativeQuery = true)
	public List<Object[]> getEligibleMembers(@Param("amount") BigDecimal amount);
}