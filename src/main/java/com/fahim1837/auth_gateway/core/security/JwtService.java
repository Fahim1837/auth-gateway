package com.fahim1837.auth_gateway.core.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fahim1837.auth_gateway.core.model.CoreUser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;
	private final byte[] secret;
	private final Duration accessTokenDuration;
	private final Duration refreshTokenDuration;

	public JwtService(
			ObjectMapper objectMapper,
			@Value("${auth.jwt.secret}") String secret,
			@Value("${auth.jwt.access-token-minutes}") long accessTokenMinutes,
			@Value("${auth.jwt.refresh-token-days}") long refreshTokenDays) {
		if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("auth.jwt.secret must be at least 32 bytes");
		}

		this.objectMapper = objectMapper;
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.accessTokenDuration = Duration.ofMinutes(accessTokenMinutes);
		this.refreshTokenDuration = Duration.ofDays(refreshTokenDays);
	}

	public TokenPair createTokenPair(CoreUser user) {
		String accessToken = createToken(user, "access", accessTokenDuration);
		String refreshToken = createToken(user, "refresh", refreshTokenDuration);
		return new TokenPair(accessToken, refreshToken, parse(refreshToken).expiresAt());
	}

	public String createAccessToken(CoreUser user) {
		return createToken(user, "access", accessTokenDuration);
	}

	public String createRefreshToken(CoreUser user) {
		return createToken(user, "refresh", refreshTokenDuration);
	}

	public JwtClaims parse(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				throw new IllegalArgumentException("Invalid JWT format");
			}

			String signedContent = parts[0] + "." + parts[1];
			String expectedSignature = base64UrlEncode(hmac(signedContent));
			if (!MessageDigestTimingSafe.equals(expectedSignature, parts[2])) {
				throw new IllegalArgumentException("Invalid JWT signature");
			}

			Map<String, Object> payload = objectMapper.readValue(base64UrlDecode(parts[1]), MAP_TYPE);
			Instant expiresAt = Instant.ofEpochSecond(asLong(payload.get("exp")));
			if (expiresAt.isBefore(Instant.now())) {
				throw new IllegalArgumentException("JWT is expired");
			}

			return new JwtClaims(
					Long.valueOf(String.valueOf(payload.get("sub"))),
					String.valueOf(payload.get("email")),
					String.valueOf(payload.get("type")),
					String.valueOf(payload.get("jti")),
					Instant.ofEpochSecond(asLong(payload.get("iat"))),
					expiresAt);
		} catch (Exception exception) {
			throw new IllegalArgumentException("Invalid JWT", exception);
		}
	}

	private String createToken(CoreUser user, String type, Duration duration) {
		try {
			Instant now = Instant.now();
			Instant expiresAt = now.plus(duration);

			Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("sub", String.valueOf(user.getId()));
			payload.put("email", user.getEmail());
			payload.put("type", type);
			payload.put("jti", UUID.randomUUID().toString());
			payload.put("iat", now.getEpochSecond());
			payload.put("exp", expiresAt.getEpochSecond());

			String encodedHeader = base64UrlEncode(objectMapper.writeValueAsBytes(header));
			String encodedPayload = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
			String signedContent = encodedHeader + "." + encodedPayload;
			return signedContent + "." + base64UrlEncode(hmac(signedContent));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to create JWT", exception);
		}
	}

	private byte[] hmac(String content) throws Exception {
		Mac mac = Mac.getInstance(HMAC_ALGORITHM);
		mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
		return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
	}

	private String base64UrlEncode(byte[] value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
	}

	private byte[] base64UrlDecode(String value) {
		return Base64.getUrlDecoder().decode(value);
	}

	private long asLong(Object value) {
		if (value instanceof Number number) {
			return number.longValue();
		}

		return Long.parseLong(String.valueOf(value));
	}

	public record TokenPair(String accessToken, String refreshToken, Instant refreshTokenExpiresAt) {
	}
}
