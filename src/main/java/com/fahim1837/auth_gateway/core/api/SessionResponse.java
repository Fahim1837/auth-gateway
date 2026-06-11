package com.fahim1837.auth_gateway.core.api;

import java.time.Instant;

import com.fahim1837.auth_gateway.core.model.CoreSession;

public record SessionResponse(
		Long id,
		String userAgent,
		Instant createdAt,
		Instant expiresAt,
		Instant revokedAt,
		boolean active) {

	public static SessionResponse from(CoreSession session) {
		return new SessionResponse(
				session.getId(),
				session.getUserAgent(),
				session.getCreatedAt(),
				session.getExpiresAt(),
				session.getRevokedAt(),
				session.isActive());
	}
}
