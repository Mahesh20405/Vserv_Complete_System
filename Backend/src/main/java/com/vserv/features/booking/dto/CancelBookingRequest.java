package com.vserv.features.booking.dto;

import com.vserv.core.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CancelBookingRequest {
	@NotBlank(message = "Reason is required")
	@Size(max = 500, message = "Reason must be at most 500 characters")
	@Pattern(regexp = ValidationPatterns.REASON, message = "Reason contains unsupported characters")
	private String reason;

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
