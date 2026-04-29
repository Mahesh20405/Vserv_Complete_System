package com.vserv.features.booking.controller;

import com.vserv.features.booking.mapper.BookingMapper;

import com.vserv.features.booking.service.BookingService;

import com.vserv.core.exception.ForbiddenException;
import com.vserv.core.exception.NotFoundException;
import com.vserv.core.pagination.PaginationUtils;
import com.vserv.core.util.SecurityUtils;
import com.vserv.entity.AppUser;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.ServiceCatalog;
import com.vserv.entity.ServiceRecord;
import com.vserv.entity.Vehicle;
import com.vserv.features.booking.dto.BookingDto;
import com.vserv.features.booking.dto.BookingHistoryDto;
import com.vserv.features.booking.dto.CancelBookingRequest;
import com.vserv.features.booking.dto.ConfirmBookingRequest;
import com.vserv.features.booking.dto.CreateBookingRequest;
import com.vserv.features.booking.dto.ReassignAdvisorRequest;
import com.vserv.features.booking.dto.RescheduleRequest;
import com.vserv.features.catalog.service.CatalogService;
import com.vserv.features.user.service.UserService;
import com.vserv.features.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

	private final BookingService bookingService;
	private final VehicleService vehicleService;
	private final CatalogService catalogService;
	private final UserService userService;
	private final SecurityUtils securityUtils;

	public BookingController(BookingService bookingService, VehicleService vehicleService,
			CatalogService catalogService, UserService userService, SecurityUtils securityUtils) {
		this.bookingService = bookingService;
		this.vehicleService = vehicleService;
		this.catalogService = catalogService;
		this.userService = userService;
		this.securityUtils = securityUtils;
	}

	@GetMapping
	public ResponseEntity<?> listBookings(
			@RequestParam(required = false) ServiceBooking.BookingStatus status,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) LocalDate dateFrom,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			@RequestParam(required = false, defaultValue = "false") boolean unassignedOnly,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		AppUser me = securityUtils.requireCurrentUser();
		List<ServiceBooking> scoped = securityUtils.isAdmin() ? bookingService.findAll(status)
				: securityUtils.isAdvisor() ? bookingService.findByAdvisor(me, status)
						: bookingService.findByCustomer(me, status);
		List<BookingDto> items = bookingService.toDtos(scoped).stream().filter(dto -> matchesBookingQuery(dto, q))
				.filter(dto -> dateFrom == null
						|| (dto.getServiceDate() != null && !dto.getServiceDate().isBefore(dateFrom)))
				.filter(dto -> !unassignedOnly || (dto.getAdvisorId() == null
						&& (dto.getAdvisorName() == null || dto.getAdvisorName().isBlank())))
				.sorted(bookingComparator(sort)).toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookingDto> getBookingById(@PathVariable Integer id) {
		ServiceBooking booking = bookingService.findById(id)
				.orElseThrow(() -> new NotFoundException("Booking not found."));
		ServiceRecord record = bookingService.findRecordByBookingId(id).orElse(null);
		securityUtils.requireBookingAccess(booking, record);
		return ResponseEntity.status(HttpStatus.OK).body(bookingService.toDto(booking));
	}

	@GetMapping("/overdue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<BookingDto>> getOverdueBookings() {
		List<ServiceBooking> overdue = bookingService.findOverdueConfirmedBookings();
		List<BookingDto> dtos = overdue.stream().map(bookingService::toDto).toList();
		return ResponseEntity.ok(dtos);
	}

	@GetMapping("/{id}/history")
	public ResponseEntity<List<BookingHistoryDto>> getBookingHistory(@PathVariable Integer id) {
		ServiceBooking booking = bookingService.findById(id)
				.orElseThrow(() -> new NotFoundException("Booking not found."));
		ServiceRecord record = bookingService.findRecordByBookingId(id).orElse(null);
		securityUtils.requireBookingAccess(booking, record);
		return ResponseEntity.status(HttpStatus.OK)
				.body(bookingService.getHistory(booking).stream().map(BookingMapper::toHistoryDto).toList());
	}

	@PostMapping
	public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody CreateBookingRequest req) {
		AppUser me = securityUtils.requireCurrentUser();
		
		Vehicle vehicle = vehicleService.findById(req.getVehicleId())
				.orElseThrow(() -> new NotFoundException("Vehicle not found."));
		
		if (securityUtils.isCustomer()) {
			Integer ownerId = vehicle.getUser() != null ? vehicle.getUser().getUserId() : null;
			if (ownerId == null || !ownerId.equals(me.getUserId()))
				throw new ForbiddenException("Access denied.");
		}
		
		ServiceCatalog catalog = catalogService.findServiceById(req.getCatalogId())
				.orElseThrow(() -> new NotFoundException("Catalog service not found."));
		
		ServiceBooking saved = bookingService.create(vehicle, catalog, req.getServiceDate(), req.getTimeSlot(),
				req.getNotes(), me, req.getPaymentMethod(), req.getTransactionRef(), req.getWaiveBookingCharge());
		
		return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.toDto(saved));
	}

	@PatchMapping("/{id}/confirm")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> confirmBooking(@PathVariable Integer id,
			@Valid @RequestBody ConfirmBookingRequest req) {
		AppUser me = securityUtils.requireCurrentUser();
		if (req.getAdvisorId() != null) {
			AppUser advisor = userService.findById(req.getAdvisorId())
					.orElseThrow(() -> new NotFoundException("User not found."));
			bookingService.confirmWithAdvisor(id, advisor, me);
		} else {
			bookingService.confirm(id, me);
		}
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Booking confirmed."));
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable Integer id,
			@Valid @RequestBody CancelBookingRequest body) {
		AppUser me = securityUtils.requireCurrentUser();
		ServiceBooking booking = bookingService.findById(id)
				.orElseThrow(() -> new NotFoundException("Booking not found."));
		ServiceRecord record = bookingService.findRecordByBookingId(id).orElse(null);
		securityUtils.requireBookingAccess(booking, record);
		bookingService.cancel(id, body.getReason(), me);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Booking cancelled."));
	}

	@PatchMapping("/{id}/reschedule")
	public ResponseEntity<Map<String, String>> rescheduleBooking(@PathVariable Integer id,
			@Valid @RequestBody RescheduleRequest req) {
		AppUser me = securityUtils.requireCurrentUser();
		ServiceBooking booking = bookingService.findById(id)
				.orElseThrow(() -> new NotFoundException("Booking not found."));
		ServiceRecord record = bookingService.findRecordByBookingId(id).orElse(null);
		securityUtils.requireBookingAccess(booking, record);
		bookingService.reschedule(id, req.getNewDate(), req.getNewSlot(), req.getReason(), me);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Booking rescheduled."));
	}

	@PatchMapping("/{id}/reassign-advisor")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> reassignBookingAdvisor(@PathVariable Integer id,
			@Valid @RequestBody ReassignAdvisorRequest body) {
		AppUser me = securityUtils.requireCurrentUser();
		AppUser newAdvisor = userService.findById(body.getAdvisorId())
				.orElseThrow(() -> new NotFoundException("User not found."));
		bookingService.reassignAdvisor(id, newAdvisor, me);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Advisor reassigned."));
	}

	private boolean matchesBookingQuery(BookingDto dto, String q) {
		String normalized = q == null ? "" : q.trim().toLowerCase();
		if (normalized.isBlank()) {
			return true;
		}
		String haystack = String.join(" ", dto.getBookingNumber() == null ? "" : dto.getBookingNumber(),
				dto.getOwnerName() == null ? "" : dto.getOwnerName(),
				dto.getVehicleInfo() == null ? "" : dto.getVehicleInfo(),
				dto.getServiceName() == null ? "" : dto.getServiceName(),
				dto.getAdvisorName() == null ? "" : dto.getAdvisorName()).toLowerCase();
		return haystack.contains(normalized);
	}

	private Comparator<BookingDto> bookingComparator(String sort) {
		Comparator<BookingDto> byDate = Comparator.comparing(dto -> dto.getServiceDate() != null ? dto.getServiceDate()
				: dto.getCreatedAt() != null ? dto.getCreatedAt().toLocalDate() : LocalDate.MIN);
		return switch (sort == null ? "newest" : sort) {
		case "oldest", "date-asc" -> byDate;
		default -> byDate.reversed();
		};
	}
}
