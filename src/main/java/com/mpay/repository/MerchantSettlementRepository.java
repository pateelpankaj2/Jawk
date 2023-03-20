package com.mpay.repository;

import com.mpay.model.MerchantSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MerchantSettlementRepository extends JpaRepository<MerchantSettlement, Long> {

    @Query("from MerchantSettlement ms where ms.merchant.id = :merchantId")
    public List<MerchantSettlement> getMerchantSettledAmounts(@Param("merchantId") Long merchantId);

    @Query("from MerchantSettlement ms")
    public List<MerchantSettlement> getAllSettledAmounts();
}
