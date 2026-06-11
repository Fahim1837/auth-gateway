package com.fahim1837.auth_gateway.core.api;

import java.time.Instant;

import com.fahim1837.auth_gateway.core.model.CoreUser;

public record UserResponse(
		Long id,
		String name,
		String email,
		String oauthProvider,
		boolean emailVerified,
		boolean twoFactorEnabled,
		boolean active,
		Instant createdAt) {

	public static UserResponse from(CoreUser user) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getOauthProvider(),
				user.isEmailVerified(),
				user.isTwoFactorEnabled(),
				user.isActive(),
				user.getCreatedAt());
	}
}
