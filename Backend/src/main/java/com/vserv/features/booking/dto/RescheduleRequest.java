package com.vserv.features.booking.dto;

import com.vserv.core.util.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class RescheduleRequest {
	@NotNull(message = "New service date is required")
	private LocalDate newDate;
	@NotBlank(message = "New time slot is required")
	@Size(max = 20, message = "New time slot must be at most 20 characters")
	@Pattern(regexp = ValidationPatterns.SLOT, message = "New time slot contains unsupported characters")
	private String newSlot;
	@NotBlank(message = "Reason is required")
	@Size(max = 500, message = "Reason must be at most 500 characters")
	@Pattern(regexp = ValidationPatterns.REASON, message = "Reason contains unsupported characters")
	private String reason;

	public LocalDate getNewDate() {
		return newDate;
	}

	public void setNewDate(LocalDate v) {
		this.newDate = v;
	}

	public String getNewSlot() {
		return newSlot;
	}

	public void setNewSlot(String v) {
		this.newSlot = v;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String v) {
		this.reason = v;
	}
}
