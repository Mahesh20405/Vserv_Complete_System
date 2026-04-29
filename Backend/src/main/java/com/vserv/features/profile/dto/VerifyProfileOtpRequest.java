package com.vserv.features.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyProfileOtpRequest {

	@NotBlank(message = "OTP is required.")
	@Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits.")
	private String otp;

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}
}
