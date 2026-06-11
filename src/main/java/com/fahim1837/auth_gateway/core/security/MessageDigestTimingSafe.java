package com.fahim1837.auth_gateway.core.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

final class MessageDigestTimingSafe {

	private MessageDigestTimingSafe() {
	}

	static boolean equals(String expected, String actual) {
		return MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8),
				actual.getBytes(StandardCharsets.UTF_8));
	}
}
