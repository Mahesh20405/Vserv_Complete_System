package com.vserv.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "service_availability", uniqueConstraints = @UniqueConstraint(columnNames = { "service_date",
		"time_slot" }))
public class ServiceAvailability {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "availability_id")
	private Integer availabilityId;

	@Column(name = "service_date", nullable = false)
	private LocalDate serviceDate;

	@Column(name = "time_slot", nullable = false, length = 20)
	private String timeSlot;

	@Column(name = "max_bookings")
	private Integer maxBookings = 5;

	@Column(name = "current_bookings")
	private Integer currentBookings = 0;

	@Column(name = "is_available")
	private Boolean isAvailable = true;

	public ServiceAvailability() {
	}

	public Integer getAvailabilityId() {
		return availabilityId;
	}

	public void setAvailabilityId(Integer availabilityId) {
		this.availabilityId = availabilityId;
	}

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

	public Integer getCurrentBookings() {
		return currentBookings;
	}

	public void setCurrentBookings(Integer currentBookings) {
		this.currentBookings = currentBookings;
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
}
