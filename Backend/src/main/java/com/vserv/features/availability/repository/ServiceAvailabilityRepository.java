package com.vserv.features.availability.repository;

import com.vserv.entity.ServiceAvailability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ServiceAvailabilityRepository extends JpaRepository<ServiceAvailability, Integer> {
	List<ServiceAvailability> findByServiceDateBetweenOrderByServiceDateAscTimeSlotAsc(LocalDate from, LocalDate to);

	List<ServiceAvailability> findByServiceDateAndIsAvailableTrue(LocalDate date);

	Optional<ServiceAvailability> findByServiceDateAndTimeSlot(LocalDate date, String slot);

	boolean existsByServiceDateAndTimeSlot(LocalDate date, String slot);

	boolean existsByServiceDate(LocalDate date);

	/**
	 * Bulk-marks all past slots unavailable in a single UPDATE
	 * AvailabilityDAOImpl#cleanupPastSlots() from the console app.
	 */
	@Modifying
	@Query("UPDATE ServiceAvailability s SET s.isAvailable = false "
			+ "WHERE s.serviceDate < :today AND s.isAvailable = true")
	int markPastSlotsUnavailable(@Param("today") LocalDate today);

	@Modifying
	@Query(value = """
			UPDATE service_availability
			SET is_available = false
			WHERE service_date = :today
			  AND is_available = true
			  AND SUBSTRING(time_slot, 1, 5) <= :currentTime
			""", nativeQuery = true)
	int markStartedSlotsUnavailable(@Param("today") LocalDate today, @Param("currentTime") String currentTime);
}
