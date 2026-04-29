package com.vserv.features.auth.dto;

public class LoginResponse {
	private Integer userId;
	private String fullName;
	private String email;
	private String role;
	private String accessToken;
	private String refreshToken;
	private String tokenType;

	public LoginResponse(Integer userId, String fullName, String email, String role) {
		this.userId = userId;
		this.fullName = fullName;
		this.email = email;
		this.role = role;
	}

	public LoginResponse(Integer userId, String fullName, String email, String role, String accessToken,
			String refreshToken, String tokenType) {
		this.userId = userId;
		this.fullName = fullName;
		this.email = email;
		this.role = role;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.tokenType = tokenType;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getRole() {
		return role;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}
}
