package com.vserv.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RefreshTokenRequest {

	@NotBlank(message = "Refresh token is required")
	@Size(max = 4000, message = "Refresh token must be at most 4000 characters")
	private String refreshToken;

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
