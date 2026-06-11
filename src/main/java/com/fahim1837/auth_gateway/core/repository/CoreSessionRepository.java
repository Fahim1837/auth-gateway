package com.fahim1837.auth_gateway.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fahim1837.auth_gateway.core.model.CoreSession;
import com.fahim1837.auth_gateway.core.model.CoreUser;

public interface CoreSessionRepository extends JpaRepository<CoreSession, Long> {

	Optional<CoreSession> findByRefreshTokenHash(String refreshTokenHash);

	List<CoreSession> findByUserOrderByCreatedAtDesc(CoreUser user);
}
