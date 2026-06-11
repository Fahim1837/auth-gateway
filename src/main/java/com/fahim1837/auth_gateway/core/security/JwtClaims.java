package com.fahim1837.auth_gateway.core.security;

import java.time.Instant;

public record JwtClaims(
		Long userId,
		String email,
		String type,
		String tokenId,
		Instant issuedAt,
		Instant expiresAt) {
}
