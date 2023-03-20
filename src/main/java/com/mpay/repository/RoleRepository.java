package com.mpay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mpay.model.Roles;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {
	Roles findByName(String name);
}
