package com.vserv.features.auth.dto;

import com.vserv.core.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

	@NotBlank(message = "Reset token is required.")
	private String resetToken;

	@NotBlank(message = "New password is required.")
	@Size(min = 8, max = 255, message = "New password must be between 8 and 255 characters.")
	@Pattern(regexp = ValidationPatterns.PASSWORD, message = "Password must include uppercase, lowercase, digit, and special character")
	private String newPassword;

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
