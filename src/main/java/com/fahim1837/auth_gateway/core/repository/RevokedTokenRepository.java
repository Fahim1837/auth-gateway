package com.fahim1837.auth_gateway.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fahim1837.auth_gateway.core.model.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

	boolean existsByTokenHash(String tokenHash);
}
