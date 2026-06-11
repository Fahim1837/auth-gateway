package com.fahim1837.auth_gateway.core.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "core_users")
public class CoreUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	private String oauthProvider;

	private String oauthProviderUserId;

	@Column(nullable = false)
	private boolean emailVerified;

	@Column(nullable = false)
	private boolean twoFactorEnabled;

	private String twoFactorSecret;

	@Column(nullable = false)
	private boolean active;

	private String emailVerificationCodeHash;

	private String passwordResetCodeHash;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected CoreUser() {
	}

	public CoreUser(String name, String email, String passwordHash, String emailVerificationCodeHash) {
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.emailVerificationCodeHash = emailVerificationCodeHash;
		this.active = true;
	}

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getOauthProvider() {
		return oauthProvider;
	}

	public void setOauthProvider(String oauthProvider) {
		this.oauthProvider = oauthProvider;
	}

	public String getOauthProviderUserId() {
		return oauthProviderUserId;
	}

	public void setOauthProviderUserId(String oauthProviderUserId) {
		this.oauthProviderUserId = oauthProviderUserId;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public boolean isTwoFactorEnabled() {
		return twoFactorEnabled;
	}

	public void setTwoFactorEnabled(boolean twoFactorEnabled) {
		this.twoFactorEnabled = twoFactorEnabled;
	}

	public String getTwoFactorSecret() {
		return twoFactorSecret;
	}

	public void setTwoFactorSecret(String twoFactorSecret) {
		this.twoFactorSecret = twoFactorSecret;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getEmailVerificationCodeHash() {
		return emailVerificationCodeHash;
	}

	public void setEmailVerificationCodeHash(String emailVerificationCodeHash) {
		this.emailVerificationCodeHash = emailVerificationCodeHash;
	}

	public String getPasswordResetCodeHash() {
		return passwordResetCodeHash;
	}

	public void setPasswordResetCodeHash(String passwordResetCodeHash) {
		this.passwordResetCodeHash = passwordResetCodeHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
