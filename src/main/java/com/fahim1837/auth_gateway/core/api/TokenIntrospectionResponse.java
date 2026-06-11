package com.fahim1837.auth_gateway.core.api;

import java.time.Instant;

public record TokenIntrospectionResponse(
		boolean active,
		String type,
		Long userId,
		String email,
		Instant issuedAt,
		Instant expiresAt) {
}
