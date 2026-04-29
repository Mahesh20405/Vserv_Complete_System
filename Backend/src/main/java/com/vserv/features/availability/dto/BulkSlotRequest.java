package com.vserv.features.availability.dto;

import com.vserv.core.util.ValidationPatterns;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class BulkSlotRequest {
	@NotNull(message = "From date is required")
	private LocalDate from;
	@NotNull(message = "To date is required")
	private LocalDate to;
	@NotEmpty(message = "At least one slot is required")
	private List<@NotBlank(message = "Slot value is required") @Size(max = 20, message = "Slot must be at most 20 characters") @Pattern(regexp = ValidationPatterns.SLOT, message = "Slot contains unsupported characters") String> slots;
	@Min(value = 1, message = "Maximum bookings must be at least 1")
	@Max(value = 20, message = "Maximum bookings must be at most 20")
	private int maxBookings = 3;

	public LocalDate getFrom() {
		return from;
	}

	public void setFrom(LocalDate v) {
		this.from = v;
	}

	public LocalDate getTo() {
		return to;
	}

	public void setTo(LocalDate v) {
		this.to = v;
	}

	public List<String> getSlots() {
		return slots;
	}

	public void setSlots(List<String> v) {
		this.slots = v;
	}

	public int getMaxBookings() {
		return maxBookings;
	}

	public void setMaxBookings(int v) {
		this.maxBookings = v;
	}
}
