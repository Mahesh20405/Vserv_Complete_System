package com.vserv.features.booking.service.impl;

import com.vserv.features.booking.mapper.BookingMapper;

import com.vserv.features.booking.repository.ServiceBookingRepository;

import com.vserv.features.booking.service.BookingService;

import com.vserv.entity.BookingHistory;
import com.vserv.entity.Invoice;
import com.vserv.entity.ServiceBooking;
import com.vserv.features.audit.repository.BookingHistoryRepository;

import com.vserv.entity.AppUser;
import com.vserv.entity.Vehicle;
import com.vserv.entity.ServiceCatalog;
import com.vserv.entity.ServiceAvailability;
import com.vserv.features.availability.repository.ServiceAvailabilityRepository;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.servicerecord.service.ServiceRecordService;
import com.vserv.entity.Payment;
import com.vserv.entity.Notification;
import com.vserv.features.notification.service.NotificationService;
import com.vserv.features.booking.dto.BookingDto;
import com.vserv.features.invoice.dto.BookingPaymentInfoDto;
import com.vserv.features.invoice.service.InvoiceService;
import com.vserv.features.paymentgateway.service.VerifiedGatewayPaymentService;
import com.vserv.features.user.service.UserService;
import com.vserv.core.util.SlotTimeUtils;

import com.vserv.core.exception.BusinessException;
import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.NotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
	private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

	private final ServiceBookingRepository bookingRepo;
	private final BookingHistoryRepository historyRepo;
	private final ServiceAvailabilityRepository availabilityRepo;
	private final NotificationService notificationService;
	private final InvoiceService invoiceService;
	private final ServiceRecordService recordService;
	private final UserService userService;
	private final VerifiedGatewayPaymentService verifiedGatewayPaymentService;

	public BookingServiceImpl(ServiceBookingRepository bookingRepo, BookingHistoryRepository historyRepo,
			ServiceAvailabilityRepository availabilityRepo, NotificationService notificationService,
			InvoiceService invoiceService, ServiceRecordService recordService, UserService userService,
			VerifiedGatewayPaymentService verifiedGatewayPaymentService) {
		this.bookingRepo = bookingRepo;
		this.historyRepo = historyRepo;
		this.availabilityRepo = availabilityRepo;
		this.notificationService = notificationService;
		this.invoiceService = invoiceService;
		this.recordService = recordService;
		this.userService = userService;
		this.verifiedGatewayPaymentService = verifiedGatewayPaymentService;
	}

	public List<ServiceBooking> findAll(ServiceBooking.BookingStatus status) {
		return status == null ? bookingRepo.findAllByOrderByCreatedAtDesc()
				: bookingRepo.findAllByBookingStatusOrderByCreatedAtDesc(status);
	}

	public Optional<ServiceBooking> findById(Integer id) {
		return bookingRepo.findById(id);
	}

	public List<ServiceBooking> findByCustomer(AppUser user, ServiceBooking.BookingStatus status) {
		return status == null ? bookingRepo.findByCustomer(user.getUserId())
				: bookingRepo.findByCustomerAndStatus(user.getUserId(), status);
	}

	public List<ServiceBooking> findByAdvisor(AppUser advisor, ServiceBooking.BookingStatus status) {
		return status == null ? bookingRepo.findByAdvisor(advisor.getUserId())
				: bookingRepo.findByAdvisorAndStatus(advisor.getUserId(), status);
	}

	public List<ServiceBooking> findOverdueConfirmedBookings() {
		return bookingRepo.findOverdueConfirmedBookings(LocalDate.now());
	}

	public BookingDto toDto(ServiceBooking booking) {
		ServiceRecord record = recordService.findByBookingId(booking.getBookingId()).orElse(null);
		Invoice invoice = invoiceService.findByBookingId(booking.getBookingId()).orElse(null);
		Payment bookingChargePayment = invoiceService
				.findLatestSuccessfulBookingChargeByBookingId(booking.getBookingId()).orElse(null);
		BookingDto dto = BookingMapper.toDto(booking, record, invoice, bookingChargePayment);
		applyBookingPaymentInfo(dto, booking.getBookingId());
		return dto;
	}

	public Optional<ServiceRecord> findRecordByBookingId(Integer bookingId) {
		return recordService.findByBookingId(bookingId);
	}

	public List<BookingDto> toDtos(List<ServiceBooking> bookings) {
		Map<Integer, ServiceRecord> recordsByBookingId = recordService
				.findByBookingIds(bookings.stream().map(ServiceBooking::getBookingId).toList());
		return bookings.stream().map(booking -> {
			ServiceRecord record = recordsByBookingId.get(booking.getBookingId());
			Invoice invoice = invoiceService.findByBookingId(booking.getBookingId()).orElse(null);
			Payment bookingChargePayment = invoiceService
					.findLatestSuccessfulBookingChargeByBookingId(booking.getBookingId()).orElse(null);
			BookingDto dto = BookingMapper.toDto(booking, record, invoice, bookingChargePayment);
			applyBookingPaymentInfo(dto, booking.getBookingId());
			return dto;
		}).toList();
	}

	private void applyBookingPaymentInfo(BookingDto dto, Integer bookingId) {
		invoiceService.findBookingPaymentInfo(bookingId).ifPresent(info -> mergeBookingPaymentInfo(dto, info));
	}

	private void mergeBookingPaymentInfo(BookingDto dto, BookingPaymentInfoDto info) {
		if ((dto.getPaymentStatus() == null || dto.getPaymentStatus().isBlank()) && info.getPaymentStatus() != null
				&& !info.getPaymentStatus().isBlank()) {
			dto.setPaymentStatus(info.getPaymentStatus());
		}
		if (info.getBookingChargeAmount() != null) {
			dto.setBookingChargeAmount(info.getBookingChargeAmount());
		}
		if (info.getBookingChargePaymentMethod() != null && !info.getBookingChargePaymentMethod().isBlank()) {
			dto.setBookingChargePaymentMethod(info.getBookingChargePaymentMethod());
		}
		if (info.getBookingChargeTransactionReference() != null
				&& !info.getBookingChargeTransactionReference().isBlank()) {
			dto.setBookingChargeTransactionReference(info.getBookingChargeTransactionReference());
		}
		if (info.getBookingChargePaidAt() != null) {
			dto.setBookingChargePaidAt(info.getBookingChargePaidAt());
			if (dto.getPaymentStatus() == null || dto.getPaymentStatus().isBlank()) {
				dto.setPaymentStatus("PARTIALLY_PAID");
			}
		}
	}

	public long countByStatus(ServiceBooking.BookingStatus status) {
		return bookingRepo.countByBookingStatus(status);
	}

	public List<Object[]> countByStatusGrouped() {
		return bookingRepo.countGroupedByBookingStatus();
	}

	public List<Object[]> countCreatedByMonthSince(LocalDateTime from) {
		return bookingRepo.countCreatedByMonthSince(from);
	}

	public List<Object[]> countByServiceTypeGrouped() {
		return bookingRepo.countGroupedByServiceType();
	}

	public List<ServiceBooking> findRecent() {
		return bookingRepo.findTop10ByOrderByCreatedAtDesc();
	}

	private void guardFutureSlot(LocalDate date, String slot) {
		if (date.isBefore(LocalDate.now()))
			throw new BusinessException("Cannot book a past date.");
		if (date.equals(LocalDate.now())) {
			LocalTime slotStart = SlotTimeUtils.parseSlotStartTime(slot);
			if (slotStart != null && slotStart.isBefore(LocalTime.now()))
				throw new BusinessException(
						"The time slot \"" + slot + "\" has already passed today. Please choose a future slot.");
		}
	}

	private void guardNoDuplicate(Integer vehicleId, LocalDate date, String slot, String vehicleLabel) {
		// Coarse check: any active booking on the same date (keeps UX message clear)
		if (bookingRepo.countActiveForVehicleOnDate(vehicleId, date) > 0)
			throw new ConflictException("An active booking for " + vehicleLabel + " on " + date
					+ " already exists. Cancel or complete it before booking again.");
		// Fine check: mirrors the DB unique constraint (vehicle + date + slot).
		// Catches the race-condition window where two concurrent requests both pass
		// the coarse check before either has committed its insert.
		if (bookingRepo.countActiveForVehicleOnDateAndSlot(vehicleId, date, slot) > 0)
			throw new ConflictException("The slot " + slot + " on " + date
					+ " is already booked for this vehicle. Please choose a different slot.");
	}

	private ServiceAvailability requireBookableSlot(LocalDate date, String slot) {
		ServiceAvailability availability = availabilityRepo.findByServiceDateAndTimeSlot(date, slot)
				.orElseThrow(() -> new BusinessException(
						"The selected slot is not available anymore. Please refresh and choose another slot."));
		if (!Boolean.TRUE.equals(availability.getIsAvailable())
				|| (availability.getMaxBookings() != null && availability.getCurrentBookings() != null
						&& availability.getCurrentBookings() >= availability.getMaxBookings())) {
			throw new BusinessException("The selected slot is full or closed. Please choose another slot.");
		}
		return availability;
	}

	private ServiceBooking requireBooking(Integer bookingId) {
		return bookingRepo.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found."));
	}

	private void guardActionable(ServiceBooking booking, Optional<ServiceRecord> record, String action) {
		if (booking.getBookingStatus() == ServiceBooking.BookingStatus.CANCELLED) {
			throw new BusinessException("Cancelled bookings cannot be " + action + ".");
		}
		if (booking.getBookingStatus() == ServiceBooking.BookingStatus.COMPLETED) {
			throw new BusinessException("Completed bookings cannot be " + action + ".");
		}
		record.ifPresent(currentRecord -> {
			if (currentRecord.getStatus() == ServiceRecord.ServiceStatus.IN_PROGRESS) {
				throw new BusinessException("Bookings with service in progress cannot be " + action + ".");
			}
			if (currentRecord.getStatus() == ServiceRecord.ServiceStatus.COMPLETED) {
				throw new BusinessException("Bookings with completed service cannot be " + action + ".");
			}
		});
	}

	private void guardCancellable(ServiceBooking booking, Optional<ServiceRecord> record) {
		guardActionable(booking, record, "cancelled");
		record.ifPresent(currentRecord -> {
			if (currentRecord.getAdvisor() != null) {
				throw new BusinessException(
						"Assigned bookings cannot be cancelled once an advisor has been allocated.");
			}
		});
	}

	private void updateAvailabilityAfterBooking(ServiceAvailability availability, int delta) {
		int current = availability.getCurrentBookings() == null ? 0 : availability.getCurrentBookings();
		int next = Math.max(0, current + delta);
		availability.setCurrentBookings(next);
		Integer max = availability.getMaxBookings();
		LocalTime slotStart = availability.getTimeSlot() != null && !availability.getTimeSlot().isBlank()
				? SlotTimeUtils.parseSlotStartTime(availability.getTimeSlot())
				: null;
		boolean slotStartedOrPast = availability.getServiceDate() != null && availability.getTimeSlot() != null
				&& !availability.getTimeSlot().isBlank() && (availability.getServiceDate().isBefore(LocalDate.now())
						|| (availability.getServiceDate().isEqual(LocalDate.now())
								&& slotStart != null && !slotStart.isAfter(LocalTime.now())));
		availability.setIsAvailable(!slotStartedOrPast && (max == null || next < max));
		availabilityRepo.save(availability);
	}

	@Transactional
	public ServiceBooking create(Vehicle vehicle, ServiceCatalog catalog, LocalDate date, String slot, String notes,
			AppUser actor, Payment.PaymentMethod paymentMethod, String txRef, Boolean waiveBookingCharge) {
		if (vehicle == null)
			throw new BusinessException("Vehicle is required.");
		if (catalog == null)
			throw new BusinessException("Service is required.");
		if (date == null)
			throw new BusinessException("Service date is required.");
		if (slot == null || slot.isBlank())
			throw new BusinessException("Time slot is required.");
		if (Boolean.FALSE.equals(catalog.getIsActive()))
			throw new BusinessException("Selected service is inactive.");
		boolean isCustomerActor = actor.getRole() != null
				&& actor.getRole().getRoleName() == com.vserv.entity.Role.RoleName.CUSTOMER;
		boolean isAdminActor = actor.getRole() != null
				&& actor.getRole().getRoleName() == com.vserv.entity.Role.RoleName.ADMIN;
		boolean waiveCharge = Boolean.TRUE.equals(waiveBookingCharge);
		BigDecimal bookingCharge = catalog.getBookingCharge() != null ? catalog.getBookingCharge()
				: ServiceCatalog.DEFAULT_BOOKING_CHARGE;
		if (waiveCharge && !isAdminActor) {
			throw new BusinessException("Only admins can waive booking charges using loyalty benefits.");
		}
AppUser vehicleOwner = vehicle.getUser();
		if (vehicleOwner == null) {
			throw new NotFoundException("Vehicle owner not found.");
		}
		if (waiveCharge && !userService.isLoyalCustomer(vehicleOwner.getUserId())) {
			throw new BusinessException("This customer is not eligible for loyalty booking charge waiver yet.");
		}
		if (waiveCharge) {
			bookingCharge = BigDecimal.ZERO;
		}
		if (bookingCharge.compareTo(BigDecimal.ZERO) > 0 && paymentMethod == null)
			throw new BusinessException("Booking charge payment method is required.");
		if (isCustomerActor && paymentMethod == Payment.PaymentMethod.CASH) {
			throw new BusinessException("Customers must pay booking charges using a digital payment method.");
		}
		if (bookingCharge.compareTo(BigDecimal.ZERO) > 0 && paymentMethod != Payment.PaymentMethod.CASH) {
			if (txRef == null || txRef.isBlank()) {
				throw new BusinessException("Verified Razorpay payment reference is required for booking charge.");
			}
			verifiedGatewayPaymentService.consumeVerifiedBookingCharge(txRef.trim(), paymentMethod, bookingCharge);
		}
		if (catalog.getCarType() != null && catalog.getCarType() != ServiceCatalog.CatalogCarType.ALL) {
			if (!catalog.getCarType().name().equals(vehicle.getCarType().name())) {
				throw new BusinessException("Selected service is not available for this vehicle type.");
			}
		}
		if (notes != null && !notes.isBlank()) {
			String trimmedNotes = notes.trim();
			if (trimmedNotes.matches("^\\d+$"))
				throw new BusinessException("Special instructions must contain text, not numbers only.");
			notes = trimmedNotes;
		}
		guardFutureSlot(date, slot);
		guardNoDuplicate(vehicle.getVehicleId(), date, slot, vehicle.getBrand() + " " + vehicle.getModel());
		ServiceAvailability availability = requireBookableSlot(date, slot);

		ServiceBooking b = new ServiceBooking();
		b.setVehicle(vehicle);
		b.setCatalog(catalog);
		b.setServiceDate(date);
		b.setTimeSlot(slot);
		b.setBookingNotes(notes != null && !notes.isBlank() ? notes : null);
		b.setBookingStatus(ServiceBooking.BookingStatus.PENDING);

		ServiceBooking saved;
		try {
			saved = bookingRepo.save(b);
			log.info("Booking created bookingId={} vehicleId={} date={} slot={} actor={}", saved.getBookingId(),
					vehicle.getVehicleId(), date, slot, actor.getUserId());
		} catch (DataIntegrityViolationException e) {
			log.warn("Duplicate booking DB constraint hit vehicleId={} date={} slot={}", vehicle.getVehicleId(), date,
					slot);
			throw new ConflictException(
					"This vehicle already has a booking for the same date and slot. Please choose a different slot.");
		}

		updateAvailabilityAfterBooking(availability, 1);

		logHistory(saved, BookingHistory.ActionType.CREATED, null, date, null, slot, null, actor);
		ServiceRecord record = recordService.createPendingRecord(saved);
		invoiceService.createBookingChargeInvoice(record, bookingCharge, paymentMethod, txRef, vehicle.getUser(),
				waiveCharge);
		String transactionInfo = (bookingCharge.compareTo(BigDecimal.ZERO) > 0 && txRef != null && !txRef.isBlank())
				? " Transaction reference: " + txRef.trim() + "."
				: "";
		String chargeInfo = waiveCharge ? " Booking charge waived under loyalty benefits."
				: " Booking charge received: \u20b9" + bookingCharge + ".";
		notificationService.send(vehicle.getUser(), Notification.NotificationType.BOOKING_CONFIRMATION,
				"Booking Created", "Your booking #" + saved.getBookingId() + " is created for " + date + " at " + slot
						+ "." + chargeInfo + transactionInfo,
				saved);
		return saved;
	}

	@Transactional
	public void confirm(Integer bookingId, AppUser actor) {
		log.info("Confirming bookingId={} actor={}", bookingId, actor.getUserId());
		confirmInternal(bookingId, null, actor);
	}

	@Transactional
	public void cancel(Integer bookingId, String reason, AppUser actor) {
		log.info("Cancelling bookingId={} actor={} reason={}", bookingId, actor.getUserId(), reason);
		if (reason == null || reason.trim().isEmpty()) {
			throw new BusinessException("Cancellation reason is required.");
		}
		ServiceBooking b = requireBooking(bookingId);
		Optional<ServiceRecord> record = recordService.findByBookingId(bookingId);
		guardCancellable(b, record);
		LocalDate oldDate = b.getServiceDate();
		String oldSlot = b.getTimeSlot();
		b.setBookingStatus(ServiceBooking.BookingStatus.CANCELLED);
		bookingRepo.save(b);
		availabilityRepo.findByServiceDateAndTimeSlot(oldDate, oldSlot)
				.ifPresent(av -> updateAvailabilityAfterBooking(av, -1));
		logHistory(b, BookingHistory.ActionType.CANCELLED, oldDate, null, oldSlot, null, reason, actor);
		notificationService.send(b.getVehicle().getUser(), Notification.NotificationType.STATUS_UPDATE,
				"Booking Cancelled", "Booking #" + bookingId + " has been cancelled.", b);
	}

	@Transactional
	public void reschedule(Integer bookingId, LocalDate newDate, String newSlot, String reason, AppUser actor) {
		if (newDate == null)
			throw new BusinessException("New service date is required.");
		if (newSlot == null || newSlot.isBlank())
			throw new BusinessException("New time slot is required.");
		if (reason == null || reason.trim().isEmpty())
			throw new BusinessException("Reschedule reason is required.");
		guardFutureSlot(newDate, newSlot);
		ServiceBooking b = requireBooking(bookingId);
		Optional<ServiceRecord> record = recordService.findByBookingId(bookingId);
		guardActionable(b, record, "rescheduled");
		LocalDate oldDate = b.getServiceDate();
		String oldSlot = b.getTimeSlot();
		if (oldDate != null && oldDate.equals(newDate) && oldSlot != null && oldSlot.equals(newSlot)) {
			throw new BusinessException("Choose a different date or time slot to reschedule.");
		}
		ServiceAvailability newAvailability = requireBookableSlot(newDate, newSlot);
		availabilityRepo.findByServiceDateAndTimeSlot(oldDate, oldSlot)
				.ifPresent(av -> updateAvailabilityAfterBooking(av, -1));
		b.setServiceDate(newDate);
		b.setTimeSlot(newSlot);
		log.info("Booking rescheduled bookingId={} newDate={} newSlot={} actor={}", bookingId, newDate, newSlot,
				actor.getUserId());
		b.setBookingStatus(ServiceBooking.BookingStatus.RESCHEDULED);
		bookingRepo.save(b);
		boolean advisorCleared = recordService.unassignAdvisor(bookingId);
		updateAvailabilityAfterBooking(newAvailability, 1);
		String historyReason = advisorCleared ? reason + " Advisor assignment cleared; reconfirmation required."
				: reason;
		logHistory(b, BookingHistory.ActionType.RESCHEDULED, oldDate, newDate, oldSlot, newSlot, historyReason, actor);
		notificationService
				.send(b.getVehicle().getUser(), Notification.NotificationType.STATUS_UPDATE, "Booking Rescheduled",
						"Booking #" + bookingId + " rescheduled to " + newDate + " at " + newSlot
								+ (advisorCleared ? ". Advisor assignment will be reconfirmed for the new slot." : ""),
						b);
	}

	private void logHistory(ServiceBooking b, BookingHistory.ActionType type, LocalDate oldDate, LocalDate newDate,
			String oldSlot, String newSlot, String reason, AppUser actor) {
		BookingHistory h = new BookingHistory();
		h.setBooking(b);
		h.setActionType(type);
		h.setOldServiceDate(oldDate);
		h.setNewServiceDate(newDate);
		h.setOldTimeSlot(oldSlot);
		h.setNewTimeSlot(newSlot);
		h.setReason(reason);
		h.setActionBy(actor);
		historyRepo.save(h);
	}

	public List<BookingHistory> getHistory(ServiceBooking booking) {
		return historyRepo.findByBookingOrderByActionDateDesc(booking);
	}

	@Transactional
	public void confirmWithAdvisor(Integer bookingId, AppUser advisor, AppUser actor) {
		confirmInternal(bookingId, advisor, actor);
	}

	private void confirmInternal(Integer bookingId, AppUser advisor, AppUser actor) {
		ServiceBooking b = requireBooking(bookingId);
		Optional<ServiceRecord> record = recordService.findByBookingId(bookingId);
		guardActionable(b, record, "confirmed");
		if (b.getBookingStatus() == ServiceBooking.BookingStatus.CONFIRMED) {
			log.warn("Confirm rejected – bookingId={} already confirmed", bookingId);
			throw new ConflictException("Booking is already confirmed.");
		}
		if (b.getBookingStatus() == ServiceBooking.BookingStatus.RESCHEDULED && advisor == null) {
			throw new BusinessException("Rescheduled bookings must be reconfirmed with an advisor.");
		}
		String historyReason = null;
		if (advisor != null) {
			recordService.assignAdvisor(bookingId, advisor);
			historyReason = "Advisor assigned: " + advisor.getFullName();
		}
		b.setBookingStatus(ServiceBooking.BookingStatus.CONFIRMED);
		bookingRepo.save(b);
		logHistory(b, BookingHistory.ActionType.CONFIRMED, b.getServiceDate(), b.getServiceDate(), b.getTimeSlot(),
				b.getTimeSlot(), historyReason, actor);
		notificationService.send(b.getVehicle().getUser(), Notification.NotificationType.STATUS_UPDATE,
				"Booking Confirmed", "Booking #" + bookingId + " has been confirmed.", b);
	}

	@Transactional
	public void reassignAdvisor(Integer bookingId, AppUser newAdvisor, AppUser actor) {
		log.info("Reassigning advisor for bookingId={} newAdvisorId={} actor={}", bookingId, newAdvisor.getUserId(),
				actor.getUserId());
		ServiceBooking b = requireBooking(bookingId);
		Optional<ServiceRecord> record = recordService.findByBookingId(bookingId);
		guardActionable(b, record, "reassigned");
		recordService.reassignAdvisor(bookingId, newAdvisor);
		// b is still current here because reassignAdvisor only updates the service
		// record, not the booking row.
		logHistory(b, BookingHistory.ActionType.CONFIRMED, b.getServiceDate(), b.getServiceDate(), b.getTimeSlot(),
				b.getTimeSlot(), "Reassigned to advisor: " + newAdvisor.getFullName(), actor);
		notificationService.send(b.getVehicle().getUser(), Notification.NotificationType.STATUS_UPDATE,
				"Advisor Reassigned", "Your booking #" + bookingId + " has been reassigned to a new advisor.", b);
	}
}
