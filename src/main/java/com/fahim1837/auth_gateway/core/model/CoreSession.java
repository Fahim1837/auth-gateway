package com.fahim1837.auth_gateway.core.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "core_sessions")
public class CoreSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private CoreUser user;

	@Column(nullable = false, unique = true, length = 128)
	private String refreshTokenHash;

	private String userAgent;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant revokedAt;

	protected CoreSession() {
	}

	public CoreSession(CoreUser user, String refreshTokenHash, String userAgent, Instant expiresAt) {
		this.user = user;
		this.refreshTokenHash = refreshTokenHash;
		this.userAgent = userAgent;
		this.expiresAt = expiresAt;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public CoreUser getUser() {
		return user;
	}

	public String getRefreshTokenHash() {
		return refreshTokenHash;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public boolean isActive() {
		return revokedAt == null && expiresAt.isAfter(Instant.now());
	}

	public void revoke() {
		this.revokedAt = Instant.now();
	}
}
