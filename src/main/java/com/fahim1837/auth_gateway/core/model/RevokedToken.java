package com.fahim1837.auth_gateway.core.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 128)
	private String tokenHash;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private Instant revokedAt;

	protected RevokedToken() {
	}

	public RevokedToken(String tokenHash, Instant expiresAt) {
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.revokedAt = Instant.now();
	}
}
