package com.vserv.features.availability.controller;

import com.vserv.features.availability.mapper.AvailabilityMapper;

import com.vserv.features.availability.service.AvailabilityService;

import com.vserv.entity.ServiceAvailability;

import com.vserv.features.availability.dto.AvailabilityDto;
import com.vserv.features.availability.dto.BulkCreateResultDto;
import com.vserv.features.availability.dto.BulkSlotRequest;
import com.vserv.features.availability.dto.CreateAvailabilityRequest;
import com.vserv.features.availability.dto.UpdateAvailabilityRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

	private final AvailabilityService availabilityService;

	public AvailabilityController(AvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	/** GET /api/availability?from=&to= */
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<AvailabilityDto>> listAvailabilitySlots(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		if (from == null)
			from = LocalDate.now().withDayOfMonth(1);
		if (to == null)
			to = from.plusMonths(1).minusDays(1);
		return ResponseEntity.status(HttpStatus.OK)
				.body(availabilityService.findRange(from, to).stream().map(AvailabilityMapper::toDto).toList());
	}

	/** GET /api/availability/bookable?from=&to= */
	@GetMapping("/bookable")
	public ResponseEntity<List<AvailabilityDto>> listBookableAvailabilitySlots(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		if (from == null)
			from = LocalDate.now();
		if (to == null)
			to = from.plusDays(30);
		return ResponseEntity.status(HttpStatus.OK)
				.body(availabilityService.findBookableRange(from, to).stream().map(AvailabilityMapper::toDto).toList());
	}

	/** POST /api/availability */
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AvailabilityDto> createAvailability(@Valid @RequestBody CreateAvailabilityRequest body) {
		int max = body.getMaxBookings() != null ? body.getMaxBookings() : 3;
		ServiceAvailability av = availabilityService.create(body.getServiceDate(), body.getTimeSlot(), max);
		return ResponseEntity.status(HttpStatus.CREATED).body(AvailabilityMapper.toDto(av));
	}

	/** PUT /api/availability/{id} */
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AvailabilityDto> updateAvailability(@PathVariable Integer id,
			@Valid @RequestBody UpdateAvailabilityRequest body) {
		ServiceAvailability av = availabilityService.update(id, body.getMaxBookings(), body.getIsAvailable());
		return ResponseEntity.status(HttpStatus.OK).body(AvailabilityMapper.toDto(av));
	}

	/** POST /api/availability/bulk */
	@PostMapping("/bulk")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> createAvailabilityInBulk(@Valid @RequestBody BulkSlotRequest req) {
		BulkCreateResultDto result = availabilityService.bulkCreate(req.getFrom(), req.getTo(), req.getSlots(),
				req.getMaxBookings());
		String message = result.getCreated() > 0 ? "Bulk slots created." : "No new slots were created.";
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("message", message, "created", result.getCreated(), "skipped", result.getSkipped()));
	}

	/** PATCH /api/availability/{id}/toggle */
	@PatchMapping("/{id}/toggle")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> toggle(@PathVariable Integer id) {
		availabilityService.toggle(id);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Slot toggled."));
	}

	/** DELETE /api/availability/{id} removes an empty future slot. */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> deleteAvailability(@PathVariable Integer id) {
		availabilityService.markUnavailable(id);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Availability slot deleted."));
	}
}
