package com.vserv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_token")
public class RevokedToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, columnDefinition = "TEXT")
	private String token;

	@Column(name = "revoked_at", nullable = false)
	private LocalDateTime revokedAt = LocalDateTime.now();

	public RevokedToken() {
	}

	public RevokedToken(String token) {
		this.token = token;
	}

	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getRevokedAt() {
		return revokedAt;
	}

	public void setRevokedAt(LocalDateTime revokedAt) {
		this.revokedAt = revokedAt;
	}
}
