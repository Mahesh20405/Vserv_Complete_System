package com.vserv.features.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CompletePasswordChangeRequest {

	@NotBlank(message = "Reset token is required.")
	private String resetToken;

	@NotBlank(message = "New password is required.")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$", message = "Password must include uppercase, lowercase, digit, and special character.")
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
