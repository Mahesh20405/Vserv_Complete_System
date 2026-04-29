package com.vserv.features.availability.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateAvailabilityRequest {
	@NotNull(message = "Maximum bookings is required")
	@Min(value = 1, message = "Maximum bookings must be between 1 and 20")
	@Max(value = 20, message = "Maximum bookings must be between 1 and 20")
	private Integer maxBookings;

	@NotNull(message = "Availability flag is required")
	private Boolean isAvailable;

	public Integer getMaxBookings() {
		return maxBookings;
	}

	public void setMaxBookings(Integer maxBookings) {
		this.maxBookings = maxBookings;
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
}
