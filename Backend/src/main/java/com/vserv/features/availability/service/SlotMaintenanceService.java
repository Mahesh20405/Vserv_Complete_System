package com.vserv.features.availability.service;

import com.vserv.entity.ServiceAvailability;
import com.vserv.features.availability.repository.ServiceAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring equivalent of the console app's {@code AvailabilityManagementService}.
 *
 * <p>
 * Responsibilities (run once at startup via {@code ApplicationStartupRunner}):
 * <ol>
 * <li>Mark every past slot unavailable — single bulk UPDATE, no fetch
 * loop.</li>
 * <li>Seed today's slots if none exist yet.</li>
 * <li>Seed tomorrow's slots if none exist yet.</li>
 * </ol>
 *
 * Slot set and DEFAULT_MAX_BOOKINGS deliberately match the values in
 * {@code AvailabilityServiceImpl#ALLOWED_SLOTS} so there is no conflict with
 * the existing validation layer.
 */
@Service
public class SlotMaintenanceService {

	private static final Logger log = LoggerFactory.getLogger(SlotMaintenanceService.class);

	/** Must stay in sync with AvailabilityServiceImpl#ALLOWED_SLOTS */
	private static final List<String> DEFAULT_TIME_SLOTS = List.of("09:00-11:00", "11:00-13:00", "13:00-15:00",
			"15:00-17:00", "17:00-19:00");

	private static final int DEFAULT_MAX_BOOKINGS = 5;

	private final ServiceAvailabilityRepository repo;

	public SlotMaintenanceService(ServiceAvailabilityRepository repo) {
		this.repo = repo;
	}

	/**
	 * Full maintenance run — mirrors
	 * {@code AvailabilityManagementService#ensureTomorrowSlotsExist()} from the
	 * console app.
	 *
	 * <p>
	 * Wrapped in a single transaction so cleanup + seeding are atomic.
	 */
	@Transactional
	public void runMaintenance() {
		cleanupPastSlots();

		LocalDate today = LocalDate.now();
		ensureSlotsExist(today);
		ensureSlotsExist(today.plusDays(1));
	}

	// -----------------------------------------------------------------------
	// private helpers
	// -----------------------------------------------------------------------

	/**
	 * Bulk-marks all past slots unavailable — equivalent to
	 * {@code AvailabilityDAOImpl#cleanupPastSlots()}.
	 */
	private void cleanupPastSlots() {
		int marked = repo.markPastSlotsUnavailable(LocalDate.now());
		if (marked > 0) {
			log.info("[SlotMaintenance] {} past availability slot(s) marked unavailable", marked);
		}
	}

	/**
	 * Seeds the standard slot set for {@code date} if no rows exist for that date.
	 * Equivalent to the console's {@code generateSlotsForDate()}.
	 */
	private void ensureSlotsExist(LocalDate date) {
		if (repo.existsByServiceDate(date)) {
			log.debug("[SlotMaintenance] Slots already present for {}, skipping seed", date);
			return;
		}

		for (String timeSlot : DEFAULT_TIME_SLOTS) {
			ServiceAvailability slot = new ServiceAvailability();
			slot.setServiceDate(date);
			slot.setTimeSlot(timeSlot);
			slot.setMaxBookings(DEFAULT_MAX_BOOKINGS);
			slot.setCurrentBookings(0);
			slot.setIsAvailable(true);
			repo.save(slot);
		}
		log.info("[SlotMaintenance] Generated {} availability slots for {}", DEFAULT_TIME_SLOTS.size(), date);
	}
}
