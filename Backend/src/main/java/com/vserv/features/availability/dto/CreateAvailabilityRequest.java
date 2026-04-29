package com.vserv.features.availability.dto;

import com.vserv.core.util.ValidationPatterns;
import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateAvailabilityRequest {
	@NotNull(message = "Service date is required")
	private LocalDate serviceDate;

	@NotBlank(message = "Time slot is required")
	@Size(max = 20, message = "Time slot must be at most 20 characters")
	@Pattern(regexp = ValidationPatterns.SLOT, message = "Time slot contains unsupported characters")
	private String timeSlot;

	@Min(value = 1, message = "Maximum bookings must be between 1 and 20")
	@Max(value = 20, message = "Maximum bookings must be between 1 and 20")
	private Integer maxBookings = 3;

	public LocalDate getServiceDate() {
		return serviceDate;
	}

	public void setServiceDate(LocalDate serviceDate) {
		this.serviceDate = serviceDate;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public Integer getMaxBookings() {
		return maxBookings;
	}

	public void setMaxBookings(Integer maxBookings) {
		this.maxBookings = maxBookings;
	}
}
