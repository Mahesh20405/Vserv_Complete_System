package com.vserv.features.availability.service;

import com.vserv.entity.ServiceAvailability;
import com.vserv.features.availability.dto.BulkCreateResultDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailabilityService {
	List<ServiceAvailability> findRange(LocalDate from, LocalDate to);

	List<ServiceAvailability> findBookableRange(LocalDate from, LocalDate to);

	List<ServiceAvailability> findAvailableSlots(LocalDate date);

	Optional<ServiceAvailability> findByDateAndSlot(LocalDate date, String slot);

	List<ServiceAvailability> findAll();

	ServiceAvailability save(ServiceAvailability av);

	ServiceAvailability create(LocalDate date, String slot, int maxBookings);

	ServiceAvailability update(Integer id, int maxBookings, boolean isAvailable);

	void toggle(Integer id);

	void markUnavailable(Integer id);

	BulkCreateResultDto bulkCreate(LocalDate from, LocalDate to, List<String> slots, int maxBookings);

	boolean isBookable(ServiceAvailability slot);
}
