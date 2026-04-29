package com.vserv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(length = 100)
	private String token;

	@Column(name = "otp_hash", length = 255)
	private String otpHash;

	@Column(name = "otp_verified")
	private Boolean otpVerified = false;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	private Boolean used = false;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser user) {
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getOtpHash() {
		return otpHash;
	}

	public void setOtpHash(String otpHash) {
		this.otpHash = otpHash;
	}

	public Boolean getOtpVerified() {
		return otpVerified;
	}

	public void setOtpVerified(Boolean otpVerified) {
		this.otpVerified = otpVerified;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public Boolean getUsed() {
		return used;
	}

	public void setUsed(Boolean used) {
		this.used = used;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
