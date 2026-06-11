package com.fahim1837.auth_gateway.core.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fahim1837.auth_gateway.core.api.AuthTokensResponse;
import com.fahim1837.auth_gateway.core.api.CodeResponse;
import com.fahim1837.auth_gateway.core.api.MessageResponse;
import com.fahim1837.auth_gateway.core.api.SessionResponse;
import com.fahim1837.auth_gateway.core.api.TokenIntrospectionResponse;
import com.fahim1837.auth_gateway.core.api.TwoFactorSetupResponse;
import com.fahim1837.auth_gateway.core.api.UserResponse;
import com.fahim1837.auth_gateway.core.model.CoreSession;
import com.fahim1837.auth_gateway.core.model.CoreUser;
import com.fahim1837.auth_gateway.core.model.RevokedToken;
import com.fahim1837.auth_gateway.core.repository.CoreSessionRepository;
import com.fahim1837.auth_gateway.core.repository.CoreUserRepository;
import com.fahim1837.auth_gateway.core.repository.RevokedTokenRepository;
import com.fahim1837.auth_gateway.core.security.AuthenticatedUser;
import com.fahim1837.auth_gateway.core.security.HashUtils;
import com.fahim1837.auth_gateway.core.security.JwtClaims;
import com.fahim1837.auth_gateway.core.security.JwtService;
import com.fahim1837.auth_gateway.core.security.JwtService.TokenPair;

@Service
public class AuthService {

	private final SecureRandom secureRandom = new SecureRandom();

	private final CoreUserRepository userRepository;
	private final CoreSessionRepository sessionRepository;
	private final RevokedTokenRepository revokedTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
			CoreUserRepository userRepository,
			CoreSessionRepository sessionRepository,
			RevokedTokenRepository revokedTokenRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.userRepository = userRepository;
		this.sessionRepository = sessionRepository;
		this.revokedTokenRepository = revokedTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthTokensResponse register(String name, String email, String password, String userAgent) {
		String normalizedEmail = normalizeEmail(email);
		if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw badRequest("Email is already registered");
		}

		String verificationCode = code();
		CoreUser user = userRepository.save(new CoreUser(
				required(name, "name"),
				normalizedEmail,
				passwordEncoder.encode(required(password, "password")),
				HashUtils.sha256(verificationCode)));
		return issueTokens(user, userAgent);
	}

	@Transactional
	public AuthTokensResponse login(String email, String password, String twoFactorCode, String userAgent) {
		CoreUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
				.orElseThrow(() -> unauthorized("Invalid email or password"));
		if (!user.isActive() || !passwordEncoder.matches(required(password, "password"), user.getPasswordHash())) {
			throw unauthorized("Invalid email or password");
		}
		if (user.isTwoFactorEnabled() && !safeEquals(user.getTwoFactorSecret(), twoFactorCode)) {
			throw unauthorized("Two-factor code is required");
		}

		return issueTokens(user, userAgent);
	}

	@Transactional
	public AuthTokensResponse loginWithOAuth2(String provider, Map<String, Object> attributes, String userAgent) {
		OAuth2Profile profile = oauthProfile(provider, attributes);
		CoreUser user = userRepository
				.findByOauthProviderAndOauthProviderUserId(profile.provider(), profile.providerUserId())
				.or(() -> userRepository.findByEmailIgnoreCase(profile.email()))
				.map(existingUser -> linkOAuthProfile(existingUser, profile))
				.orElseGet(() -> createOAuthUser(profile));
		return issueTokens(user, userAgent);
	}

	@Transactional
	public AuthTokensResponse refresh(String refreshToken, String userAgent) {
		JwtClaims claims = parseToken(refreshToken, "refresh");
		CoreSession session = sessionRepository.findByRefreshTokenHash(HashUtils.sha256(refreshToken))
				.orElseThrow(() -> unauthorized("Refresh token is not active"));
		if (!session.isActive() || !session.getUser().isActive() || !session.getUser().getId().equals(claims.userId())) {
			throw unauthorized("Refresh token is not active");
		}

		session.revoke();
		return issueTokens(session.getUser(), userAgent);
	}

	@Transactional
	public MessageResponse logout(AuthenticatedUser principal, String accessToken, String refreshToken) {
		if (accessToken != null && !accessToken.isBlank()) {
			revokeRawToken(accessToken);
		}
		if (refreshToken != null && !refreshToken.isBlank()) {
			sessionRepository.findByRefreshTokenHash(HashUtils.sha256(refreshToken)).ifPresent(CoreSession::revoke);
		}

		return new MessageResponse("Logged out");
	}

	@Transactional(readOnly = true)
	public UserResponse me(AuthenticatedUser principal) {
		return UserResponse.from(currentUser(principal));
	}

	@Transactional
	public UserResponse updateMe(AuthenticatedUser principal, String name) {
		CoreUser user = currentUser(principal);
		user.setName(required(name, "name"));
		return UserResponse.from(user);
	}

	@Transactional
	public MessageResponse deactivateMe(AuthenticatedUser principal) {
		CoreUser user = currentUser(principal);
		user.setActive(false);
		sessionRepository.findByUserOrderByCreatedAtDesc(user).forEach(CoreSession::revoke);
		return new MessageResponse("Account deactivated");
	}

	@Transactional
	public MessageResponse verifyEmail(String email, String code) {
		CoreUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
				.orElseThrow(() -> badRequest("Invalid verification code"));
		if (!safeEquals(user.getEmailVerificationCodeHash(), HashUtils.sha256(required(code, "code")))) {
			throw badRequest("Invalid verification code");
		}

		user.setEmailVerified(true);
		user.setEmailVerificationCodeHash(null);
		return new MessageResponse("Email verified");
	}

	@Transactional
	public CodeResponse resendVerification(String email) {
		CoreUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
				.orElseThrow(() -> badRequest("Email is not registered"));
		String verificationCode = code();
		user.setEmailVerificationCodeHash(HashUtils.sha256(verificationCode));
		return new CodeResponse("Verification code generated", verificationCode);
	}

	@Transactional
	public CodeResponse forgotPassword(String email) {
		CoreUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
				.orElseThrow(() -> badRequest("Email is not registered"));
		String resetCode = code();
		user.setPasswordResetCodeHash(HashUtils.sha256(resetCode));
		return new CodeResponse("Password reset code generated", resetCode);
	}

	@Transactional
	public MessageResponse resetPassword(String email, String code, String newPassword) {
		CoreUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
				.orElseThrow(() -> badRequest("Invalid reset code"));
		if (!safeEquals(user.getPasswordResetCodeHash(), HashUtils.sha256(required(code, "code")))) {
			throw badRequest("Invalid reset code");
		}

		user.setPasswordHash(passwordEncoder.encode(required(newPassword, "newPassword")));
		user.setPasswordResetCodeHash(null);
		sessionRepository.findByUserOrderByCreatedAtDesc(user).forEach(CoreSession::revoke);
		return new MessageResponse("Password reset");
	}

	@Transactional
	public MessageResponse changePassword(AuthenticatedUser principal, String currentPassword, String newPassword) {
		CoreUser user = currentUser(principal);
		if (!passwordEncoder.matches(required(currentPassword, "currentPassword"), user.getPasswordHash())) {
			throw badRequest("Current password is incorrect");
		}

		user.setPasswordHash(passwordEncoder.encode(required(newPassword, "newPassword")));
		sessionRepository.findByUserOrderByCreatedAtDesc(user).forEach(CoreSession::revoke);
		return new MessageResponse("Password changed");
	}

	@Transactional(readOnly = true)
	public List<SessionResponse> sessions(AuthenticatedUser principal) {
		return sessionRepository.findByUserOrderByCreatedAtDesc(currentUser(principal))
				.stream()
				.map(SessionResponse::from)
				.toList();
	}

	@Transactional
	public MessageResponse revokeSession(AuthenticatedUser principal, Long sessionId) {
		CoreUser user = currentUser(principal);
		CoreSession session = sessionRepository.findById(sessionId)
				.filter(candidate -> candidate.getUser().getId().equals(user.getId()))
				.orElseThrow(() -> notFound("Session not found"));
		session.revoke();
		return new MessageResponse("Session revoked");
	}

	@Transactional
	public MessageResponse revokeAllSessions(AuthenticatedUser principal) {
		sessionRepository.findByUserOrderByCreatedAtDesc(currentUser(principal)).forEach(CoreSession::revoke);
		return new MessageResponse("All sessions revoked");
	}

	@Transactional
	public TwoFactorSetupResponse setupTwoFactor(AuthenticatedUser principal) {
		CoreUser user = currentUser(principal);
		String setupCode = code();
		user.setTwoFactorSecret(setupCode);
		user.setTwoFactorEnabled(false);
		return new TwoFactorSetupResponse("Use this setup code to verify 2FA", setupCode);
	}

	@Transactional
	public MessageResponse verifyTwoFactor(AuthenticatedUser principal, String code) {
		CoreUser user = currentUser(principal);
		if (!safeEquals(user.getTwoFactorSecret(), required(code, "code"))) {
			throw badRequest("Invalid two-factor code");
		}

		user.setTwoFactorEnabled(true);
		return new MessageResponse("Two-factor authentication enabled");
	}

	@Transactional
	public MessageResponse disableTwoFactor(AuthenticatedUser principal, String password) {
		CoreUser user = currentUser(principal);
		if (!passwordEncoder.matches(required(password, "password"), user.getPasswordHash())) {
			throw badRequest("Password is incorrect");
		}

		user.setTwoFactorEnabled(false);
		user.setTwoFactorSecret(null);
		return new MessageResponse("Two-factor authentication disabled");
	}

	@Transactional(readOnly = true)
	public TokenIntrospectionResponse introspect(String token) {
		try {
			JwtClaims claims = jwtService.parse(required(token, "token"));
			boolean active = !revokedTokenRepository.existsByTokenHash(HashUtils.sha256(token));
			return new TokenIntrospectionResponse(
					active,
					claims.type(),
					claims.userId(),
					claims.email(),
					claims.issuedAt(),
					claims.expiresAt());
		} catch (IllegalArgumentException exception) {
			return new TokenIntrospectionResponse(false, null, null, null, null, null);
		}
	}

	@Transactional
	public MessageResponse revokeToken(String token) {
		revokeRawToken(required(token, "token"));
		return new MessageResponse("Token revoked");
	}

	private AuthTokensResponse issueTokens(CoreUser user, String userAgent) {
		TokenPair tokenPair = jwtService.createTokenPair(user);
		sessionRepository.save(new CoreSession(
				user,
				HashUtils.sha256(tokenPair.refreshToken()),
				userAgent,
				tokenPair.refreshTokenExpiresAt()));
		return new AuthTokensResponse(
				tokenPair.accessToken(),
				tokenPair.refreshToken(),
				"Bearer",
				UserResponse.from(user));
	}

	private CoreUser createOAuthUser(OAuth2Profile profile) {
		CoreUser user = new CoreUser(
				profile.name(),
				profile.email(),
				passwordEncoder.encode(UUID.randomUUID().toString()),
				null);
		user.setEmailVerified(profile.emailVerified());
		return userRepository.save(linkOAuthProfile(user, profile));
	}

	private CoreUser linkOAuthProfile(CoreUser user, OAuth2Profile profile) {
		user.setOauthProvider(profile.provider());
		user.setOauthProviderUserId(profile.providerUserId());
		if (profile.emailVerified()) {
			user.setEmailVerified(true);
			user.setEmailVerificationCodeHash(null);
		}
		return user;
	}

	private OAuth2Profile oauthProfile(String provider, Map<String, Object> attributes) {
		String normalizedProvider = required(provider, "provider").toLowerCase();
		return switch (normalizedProvider) {
			case "google" -> googleProfile(attributes);
			case "github" -> githubProfile(attributes);
			case "facebook" -> facebookProfile(attributes);
			default -> throw badRequest("Unsupported OAuth2 provider: " + provider);
		};
	}

	private OAuth2Profile googleProfile(Map<String, Object> attributes) {
		String providerUserId = attr(attributes, "sub");
		String email = attr(attributes, "email");
		String name = optionalAttr(attributes, "name", email);
		boolean emailVerified = Boolean.parseBoolean(String.valueOf(attributes.getOrDefault("email_verified", false)));
		return new OAuth2Profile("google", providerUserId, normalizeEmail(email), name, emailVerified);
	}

	private OAuth2Profile githubProfile(Map<String, Object> attributes) {
		String providerUserId = attr(attributes, "id");
		String login = optionalAttr(attributes, "login", "github-" + providerUserId);
		String email = optionalAttr(attributes, "email", login + "-" + providerUserId + "@github.oauth.local");
		String name = optionalAttr(attributes, "name", login);
		return new OAuth2Profile("github", providerUserId, normalizeEmail(email), name, true);
	}

	private OAuth2Profile facebookProfile(Map<String, Object> attributes) {
		String providerUserId = attr(attributes, "id");
		String email = optionalAttr(attributes, "email", "facebook-" + providerUserId + "@facebook.oauth.local");
		String name = optionalAttr(attributes, "name", email);
		return new OAuth2Profile("facebook", providerUserId, normalizeEmail(email), name, true);
	}

	private String attr(Map<String, Object> attributes, String name) {
		Object value = attributes.get(name);
		if (value == null || String.valueOf(value).isBlank()) {
			throw badRequest("OAuth2 profile is missing " + name);
		}

		return String.valueOf(value);
	}

	private String optionalAttr(Map<String, Object> attributes, String name, String fallback) {
		Object value = attributes.get(name);
		if (value == null || String.valueOf(value).isBlank()) {
			return fallback;
		}

		return String.valueOf(value);
	}

	private void revokeRawToken(String token) {
		try {
			JwtClaims claims = jwtService.parse(token);
			String tokenHash = HashUtils.sha256(token);
			if (!revokedTokenRepository.existsByTokenHash(tokenHash)) {
				revokedTokenRepository.save(new RevokedToken(tokenHash, claims.expiresAt()));
			}
		} catch (IllegalArgumentException ignored) {
			throw badRequest("Invalid token");
		}
	}

	private JwtClaims parseToken(String token, String expectedType) {
		JwtClaims claims = jwtService.parse(required(token, "token"));
		if (!expectedType.equals(claims.type())) {
			throw unauthorized("Invalid token type");
		}

		return claims;
	}

	private CoreUser currentUser(AuthenticatedUser principal) {
		if (principal == null) {
			throw unauthorized("Authentication is required");
		}

		return userRepository.findById(principal.id())
				.filter(CoreUser::isActive)
				.orElseThrow(() -> unauthorized("Authentication is required"));
	}

	private String code() {
		return String.format("%06d", secureRandom.nextInt(1_000_000));
	}

	private String normalizeEmail(String email) {
		return required(email, "email").trim().toLowerCase();
	}

	private String required(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw badRequest(fieldName + " is required");
		}

		return value.trim();
	}

	private boolean safeEquals(String expected, String actual) {
		return expected != null && actual != null && expected.equals(actual);
	}

	private ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	private ResponseStatusException unauthorized(String message) {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
	}

	private ResponseStatusException notFound(String message) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
	}

	private record OAuth2Profile(
			String provider,
			String providerUserId,
			String email,
			String name,
			boolean emailVerified) {
	}
}
