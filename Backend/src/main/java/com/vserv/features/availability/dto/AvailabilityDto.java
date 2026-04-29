package com.vserv.features.availability.dto;

import com.vserv.entity.ServiceAvailability;
import java.time.LocalDate;

public class AvailabilityDto {
	private Integer availabilityId;
	private LocalDate serviceDate;
	private String timeSlot;
	private Integer maxBookings;
	private Integer currentBookings;
	private Boolean isAvailable;

	public static AvailabilityDto from(ServiceAvailability a) {
		AvailabilityDto dto = new AvailabilityDto();
		dto.availabilityId = a.getAvailabilityId();
		dto.serviceDate = a.getServiceDate();
		dto.timeSlot = a.getTimeSlot();
		dto.maxBookings = a.getMaxBookings();
		dto.currentBookings = a.getCurrentBookings();
		dto.isAvailable = a.getIsAvailable();
		return dto;
	}

	public Integer getAvailabilityId() {
		return availabilityId;
	}

	public LocalDate getServiceDate() {
		return serviceDate;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public Integer getMaxBookings() {
		return maxBookings;
	}

	public Integer getCurrentBookings() {
		return currentBookings;
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}
}
