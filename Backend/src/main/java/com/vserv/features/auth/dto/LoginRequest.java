package com.vserv.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Size(max = 100, message = "Email must be at most 100 characters")
	private String email;
	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
