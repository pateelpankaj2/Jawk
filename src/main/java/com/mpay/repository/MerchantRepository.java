package com.mpay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mpay.model.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, Long>{

}
