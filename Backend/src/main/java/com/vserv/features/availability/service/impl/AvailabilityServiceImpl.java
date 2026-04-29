package com.vserv.features.availability.service.impl;

import com.vserv.features.availability.repository.ServiceAvailabilityRepository;

import com.vserv.features.availability.service.AvailabilityService;

import com.vserv.entity.ServiceAvailability;
import com.vserv.core.exception.BusinessException;
import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.NotFoundException;
import com.vserv.core.util.SlotTimeUtils;
import com.vserv.features.availability.dto.BulkCreateResultDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {
	private static final Logger log = LoggerFactory.getLogger(AvailabilityServiceImpl.class);
	private static final DateTimeFormatter SLOT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private static final Set<String> ALLOWED_SLOTS = Set.of("09:00-11:00", "11:00-13:00", "13:00-15:00", "15:00-17:00",
			"17:00-19:00");

	private final ServiceAvailabilityRepository repo;

	public AvailabilityServiceImpl(ServiceAvailabilityRepository repo) {
		this.repo = repo;
	}

	@Transactional
	public List<ServiceAvailability> findRange(LocalDate from, LocalDate to) {
		synchronizeElapsedSlots();
		return repo.findByServiceDateBetweenOrderByServiceDateAscTimeSlotAsc(from, to);
	}

	@Transactional
	public List<ServiceAvailability> findBookableRange(LocalDate from, LocalDate to) {
		synchronizeElapsedSlots();
		LocalDate today = LocalDate.now();
		LocalTime now = LocalTime.now();
		return findRange(from, to).stream().filter(this::isBookable)
				.filter(slot -> !slot.getServiceDate().isBefore(today))
				.filter(slot -> !slot.getServiceDate().isEqual(today) || !hasStarted(slot.getTimeSlot(), now)).toList();
	}

	@Transactional
	public List<ServiceAvailability> findAvailableSlots(LocalDate date) {
		synchronizeElapsedSlots();
		LocalDate today = LocalDate.now();
		LocalTime now = LocalTime.now();
		return repo.findByServiceDateAndIsAvailableTrue(date).stream().filter(this::isBookable)
				.filter(slot -> !date.isBefore(today))
				.filter(slot -> !date.isEqual(today) || !hasStarted(slot.getTimeSlot(), now)).toList();
	}

	@Transactional
	public Optional<ServiceAvailability> findByDateAndSlot(LocalDate date, String slot) {
		synchronizeElapsedSlots();
		return repo.findByServiceDateAndTimeSlot(date, slot);
	}

	@Transactional
	public List<ServiceAvailability> findAll() {
		synchronizeElapsedSlots();
		return repo.findAll();
	}

	@Transactional
	public ServiceAvailability save(ServiceAvailability av) {
		return repo.save(av);
	}

	@Transactional
	public ServiceAvailability create(LocalDate date, String slot, int maxBookings) {
		log.info("Creating availability slot date={} slot={} maxBookings={}", date, slot, maxBookings);
		validateSlotRequest(date, slot, maxBookings);
		log.warn("Duplicate slot creation rejected date={} slot={}", date, slot);
		if (repo.existsByServiceDateAndTimeSlot(date, slot))
			throw new ConflictException("A slot already exists for this date and time.");
		ServiceAvailability av = new ServiceAvailability();
		av.setServiceDate(date);
		av.setTimeSlot(slot);
		av.setMaxBookings(maxBookings);
		return repo.save(av);
	}

	@Transactional
	public ServiceAvailability update(Integer id, int maxBookings, boolean isAvailable) {
		ServiceAvailability av = repo.findById(id)
				.orElseThrow(() -> new NotFoundException("Availability slot not found."));

		validateEditableSlot(av);
		validateMaxBookings(maxBookings);

		int currentBookings = av.getCurrentBookings() == null ? 0 : av.getCurrentBookings();
		if (maxBookings < currentBookings) {
			throw new BusinessException("Maximum bookings cannot be less than current bookings.");
		}

		av.setMaxBookings(maxBookings);
		av.setIsAvailable(isAvailable);
		return repo.save(av);
	}

	@Transactional
	public void toggle(Integer id) {
		ServiceAvailability av = repo.findById(id)
				.orElseThrow(() -> new NotFoundException("Availability slot not found."));
		validateEditableSlot(av);
		av.setIsAvailable(!av.getIsAvailable());
		repo.save(av);
	}

	@Transactional
	public void markUnavailable(Integer id) {
		ServiceAvailability av = repo.findById(id)
				.orElseThrow(() -> new NotFoundException("Availability slot not found."));
		validateEditableSlot(av);
		if ((av.getCurrentBookings() == null ? 0 : av.getCurrentBookings()) > 0) {
			throw new ConflictException("Cannot delete a slot that already has bookings.");
		}
		repo.delete(av);
	}

	@Transactional
	public BulkCreateResultDto bulkCreate(LocalDate from, LocalDate to, List<String> slots, int maxBookings) {
		validateBulkRequest(from, to, slots, maxBookings);
		Set<String> uniqueSlots = new LinkedHashSet<>(slots);
		int created = 0;
		int skipped = 0;
		for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
			for (String slot : uniqueSlots) {
				if (!repo.existsByServiceDateAndTimeSlot(d, slot)) {
					ServiceAvailability av = new ServiceAvailability();
					av.setServiceDate(d);
					av.setTimeSlot(slot);
					av.setMaxBookings(maxBookings);
					repo.save(av);
					created++;
				} else {
					skipped++;
				}
			}
		}
		log.info("Bulk availability created={} skipped={} from={} to={}", created, skipped, from, to);
		return new BulkCreateResultDto(created, skipped);
	}

	public boolean isBookable(ServiceAvailability slot) {
		if (slot == null)
			return false;
		if (!Boolean.TRUE.equals(slot.getIsAvailable()))
			return false;
		Integer max = slot.getMaxBookings();
		Integer current = slot.getCurrentBookings();
		if (max == null)
			return true;
		return current == null || current < max;
	}

	private void synchronizeElapsedSlots() {
		LocalDate today = LocalDate.now();
		repo.markPastSlotsUnavailable(today);
		repo.markStartedSlotsUnavailable(today, LocalTime.now().format(SLOT_TIME_FORMAT));
	}

	private boolean hasStarted(String slot, LocalTime now) {
		LocalTime start = SlotTimeUtils.parseSlotStartTime(slot);
		return start != null && !start.isAfter(now);
	}

	private void validateSlotRequest(LocalDate date, String slot, int maxBookings) {
		if (date == null) {
			throw new BusinessException("Service date is required.");
		}
		if (date.isBefore(LocalDate.now())) {
			throw new BusinessException("Cannot create slots for past dates.");
		}
		validateSlotValue(slot);
		if (date.isEqual(LocalDate.now()) && hasStarted(slot, LocalTime.now())) {
			throw new BusinessException("Cannot create slots for past time on today.");
		}
		validateMaxBookings(maxBookings);
	}

	private void validateBulkRequest(LocalDate from, LocalDate to, List<String> slots, int maxBookings) {
		if (from == null) {
			throw new BusinessException("From date is required.");
		}
		if (to == null) {
			throw new BusinessException("To date is required.");
		}
		if (from.isBefore(LocalDate.now())) {
			throw new BusinessException("From date cannot be in the past.");
		}
		if (to.isBefore(from)) {
			throw new BusinessException("To date cannot be earlier than from date.");
		}
		if (slots == null || slots.isEmpty()) {
			throw new BusinessException("Select at least one time slot.");
		}
		slots.forEach(this::validateSlotValue);
		if (!from.isAfter(LocalDate.now())) {
			LocalTime now = LocalTime.now();
			boolean hasStartedSlotToday = slots.stream().anyMatch(slot -> hasStarted(slot, now));
			if (hasStartedSlotToday) {
				throw new BusinessException("Started time slots cannot be bulk-created for today.");
			}
		}
		validateMaxBookings(maxBookings);
	}

	private void validateEditableSlot(ServiceAvailability slot) {
		if (slot.getServiceDate() == null || slot.getTimeSlot() == null)
			return;
		if (isPastOrStarted(slot.getServiceDate(), slot.getTimeSlot())) {
			throw new BusinessException("Past slots are read-only.");
		}
	}

	private boolean isPastOrStarted(LocalDate date, String slot) {
		LocalDate today = LocalDate.now();
		if (date.isBefore(today))
			return true;
		return date.isEqual(today) && hasStarted(slot, LocalTime.now());
	}

	private void validateSlotValue(String slot) {
		if (slot == null || slot.isBlank()) {
			throw new BusinessException("Time slot is required.");
		}
		if (!ALLOWED_SLOTS.contains(slot.trim())) {
			throw new BusinessException("Invalid time slot selected.");
		}
	}

	private void validateMaxBookings(int maxBookings) {
		if (maxBookings < 1 || maxBookings > 20) {
			throw new BusinessException("Maximum bookings must be between 1 and 20.");
		}
	}
}
