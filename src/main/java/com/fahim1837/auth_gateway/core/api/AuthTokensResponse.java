package com.fahim1837.auth_gateway.core.api;

public record AuthTokensResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		UserResponse user) {
}
