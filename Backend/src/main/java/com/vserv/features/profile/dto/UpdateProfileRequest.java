package com.vserv.features.profile.dto;

import com.vserv.core.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
	@NotBlank(message = "Full name is required")
	@Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
	@Pattern(regexp = ValidationPatterns.NAME, message = "Full name can only contain letters, spaces, hyphens, apostrophes, and dots")
	private String fullName;
	@Pattern(regexp = ValidationPatterns.PHONE, message = "Enter a valid Indian mobile number")
	private String phone;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String v) {
		this.fullName = v;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String v) {
		this.phone = v;
	}
}
