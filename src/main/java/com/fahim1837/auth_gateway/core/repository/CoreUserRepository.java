package com.fahim1837.auth_gateway.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fahim1837.auth_gateway.core.model.CoreUser;

public interface CoreUserRepository extends JpaRepository<CoreUser, Long> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<CoreUser> findByEmailIgnoreCase(String email);

	Optional<CoreUser> findByOauthProviderAndOauthProviderUserId(String oauthProvider, String oauthProviderUserId);
}
