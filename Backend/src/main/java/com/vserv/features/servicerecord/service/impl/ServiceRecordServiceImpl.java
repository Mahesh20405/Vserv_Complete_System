package com.vserv.features.servicerecord.service.impl;

import com.vserv.features.servicerecord.repository.ServiceItemRepository;

import com.vserv.features.servicerecord.repository.ServiceRecordRepository;

import com.vserv.features.servicerecord.service.ServiceRecordService;

import com.vserv.entity.ServiceItem;
import com.vserv.entity.BookingHistory;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.audit.repository.BookingHistoryRepository;

import com.vserv.entity.AppUser;
import com.vserv.entity.ServiceBooking;
import com.vserv.features.booking.repository.ServiceBookingRepository;
import com.vserv.entity.Invoice;
import com.vserv.entity.Notification;
import com.vserv.features.notification.service.NotificationService;
import com.vserv.features.advisor.service.AdvisorService;
import com.vserv.entity.ServiceAdvisor;
import com.vserv.features.invoice.service.InvoiceService;
import com.vserv.features.user.service.UserService;
import com.vserv.entity.WorkItemCatalog;
import com.vserv.features.workitem.service.WorkItemService;
import com.vserv.features.availability.repository.ServiceAvailabilityRepository;
import com.vserv.core.util.SlotTimeUtils;

import com.vserv.core.exception.BusinessException;
import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.vserv.entity.Vehicle;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ServiceRecordServiceImpl implements ServiceRecordService {

	private static final Logger log = LoggerFactory.getLogger(ServiceRecordServiceImpl.class);

	private final ServiceRecordRepository recordRepo;
	private final ServiceItemRepository itemRepo;
	private final BookingHistoryRepository historyRepo;
	private final ServiceBookingRepository bookingRepo;
	private final InvoiceService invoiceService;
	private final NotificationService notificationService;
	private final AdvisorService advisorService;
	private final WorkItemService workItemService;
	private final com.vserv.features.vehicle.repository.VehicleRepository vehicleRepo;
	private final UserService userService;
	private final ServiceAvailabilityRepository availabilityRepo;

	public ServiceRecordServiceImpl(ServiceRecordRepository recordRepo, ServiceItemRepository itemRepo,
			BookingHistoryRepository historyRepo, ServiceBookingRepository bookingRepo, InvoiceService invoiceService,
			NotificationService notificationService, AdvisorService advisorService, WorkItemService workItemService,
			com.vserv.features.vehicle.repository.VehicleRepository vehicleRepo, UserService userService,
			ServiceAvailabilityRepository availabilityRepo) {
		this.recordRepo = recordRepo;
		this.itemRepo = itemRepo;
		this.historyRepo = historyRepo;
		this.bookingRepo = bookingRepo;
		this.invoiceService = invoiceService;
		this.notificationService = notificationService;
		this.advisorService = advisorService;
		this.workItemService = workItemService;
		this.vehicleRepo = vehicleRepo;
		this.userService = userService;
		this.availabilityRepo = availabilityRepo;
	}

	public Optional<ServiceRecord> findById(Integer id) {
		return recordRepo.findById(id);
	}

	public Optional<ServiceRecord> findByBookingId(Integer bid) {
		return recordRepo.findByBookingBookingId(bid);
	}

	public Map<Integer, ServiceRecord> findByBookingIds(Collection<Integer> bookingIds) {
		if (bookingIds == null || bookingIds.isEmpty()) {
			return Map.of();
		}
		return recordRepo.findByBookingBookingIdIn(bookingIds.stream().distinct().toList()).stream()
				.collect(Collectors.toMap(record -> record.getBooking().getBookingId(), Function.identity()));
	}

	public List<ServiceRecord> findByAdvisor(AppUser u) {
		return recordRepo.findByAdvisorUserUserIdOrderByServiceStartDateDesc(u.getUserId());
	}

	public List<ServiceRecord> findAll() {
		return recordRepo.findAllByOrderByServiceStartDateDesc();
	}

	public List<ServiceRecord> findByStatus(ServiceRecord.ServiceStatus status) {
		return recordRepo.findAllByStatusOrderByServiceStartDateDesc(status);
	}

	public long countByStatus(ServiceRecord.ServiceStatus s) {
		return recordRepo.countByStatus(s);
	}

	public List<ServiceRecord> findByAdvisorAndStatus(AppUser advisor, ServiceRecord.ServiceStatus status) {
		return recordRepo.findByAdvisorUserUserIdAndStatusOrderByServiceStartDateDesc(advisor.getUserId(), status);
	}

	public List<ServiceItem> getItems(ServiceRecord record) {
		return itemRepo.findByServiceRecord(record);
	}

	private ServiceAdvisor requireAssignableAdvisor(AppUser advisor) {
		ServiceAdvisor sa = advisorService.findById(advisor.getUserId())
				.orElseThrow(() -> new NotFoundException("Advisor profile not found."));
		if (sa.getUser() == null || Boolean.TRUE.equals(sa.getUser().getIsDeleted())) {
			throw new BusinessException("Advisor is not available for assignment.");
		}
		if (sa.getUser().getStatus() != AppUser.Status.ACTIVE) {
			throw new BusinessException("Advisor is inactive and cannot be assigned.");
		}
		ServiceAdvisor.AvailabilityStatus status = sa.getAvailabilityStatus();
		int currentLoad = sa.getCurrentLoad() == null ? 0 : sa.getCurrentLoad();

		if (status == ServiceAdvisor.AvailabilityStatus.RESIGNED
				|| status == ServiceAdvisor.AvailabilityStatus.ON_LEAVE) {
			throw new BusinessException("Advisor is not eligible for assignment. Current status: " + status + ".");
		}
		if (status == ServiceAdvisor.AvailabilityStatus.ASSIGNED && currentLoad >= AdvisorService.MAX_LOAD) {
			throw new BusinessException("Advisor has reached maximum load (" + AdvisorService.MAX_LOAD
					+ " services). Assign to another advisor.");
		}
		return sa;
	}

	private void ensureAdvisorSlotAvailable(ServiceBooking booking, Integer advisorId, Integer excludedBookingId) {
		if (booking == null || booking.getServiceDate() == null || booking.getTimeSlot() == null
				|| booking.getTimeSlot().isBlank()) {
			return;
		}
		boolean alreadyAssignedInSlot = recordRepo.existsActiveAssignmentForAdvisorAtSlot(advisorId,
				booking.getServiceDate(), booking.getTimeSlot(), excludedBookingId,
				ServiceBooking.BookingStatus.CANCELLED, ServiceRecord.ServiceStatus.COMPLETED);
		log.warn("Advisor {} already assigned in slot {} on {} for a different booking", advisorId,
				booking.getTimeSlot(), booking.getServiceDate());
		if (alreadyAssignedInSlot) {
			throw new ConflictException("Advisor is already assigned to another booking for " + booking.getServiceDate()
					+ " at " + booking.getTimeSlot() + ".");
		}
	}

	@Transactional
	public ServiceRecord assignAdvisor(Integer bookingId, AppUser advisor) {
		log.info("Assigning advisorId={} to bookingId={}", advisor.getUserId(), bookingId);
		ServiceAdvisor assignableAdvisor = requireAssignableAdvisor(advisor);
		Optional<ServiceRecord> existingRecord = recordRepo.findByBookingBookingId(bookingId);
		ServiceRecord record = existingRecord.orElseGet(ServiceRecord::new);
		if (record.getAdvisor() != null
				&& record.getAdvisor().getAdvisorId().equals(assignableAdvisor.getAdvisorId())) {
			return record;
		}
		if (record.getAdvisor() != null) {
			throw new ConflictException("Booking already has an assigned advisor. Use reassign instead.");
		}
		if (record.getBooking() == null) {
			ServiceBooking booking = bookingRepo.findById(bookingId)
					.orElseThrow(() -> new NotFoundException("Booking not found."));
			record.setBooking(booking);
		}
		ensureAdvisorSlotAvailable(record.getBooking(), assignableAdvisor.getAdvisorId(), bookingId);
		record.setAdvisor(assignableAdvisor);
		record.setStatus(ServiceRecord.ServiceStatus.PENDING);
		ServiceBooking booking = record.getBooking();
		if (booking != null) {
			booking.setAdvisor(assignableAdvisor.getUser());
			bookingRepo.save(booking);
		}
		record.setEstimatedHours(
				booking != null && booking.getCatalog() != null ? booking.getCatalog().getDurationHours() : null);
		ServiceRecord saved = recordRepo.save(record);

		advisorService.incrementLoad(advisor.getUserId());
		return saved;
	}

	@Transactional
	public ServiceRecord createPendingRecord(ServiceBooking booking) {
		return recordRepo.findByBookingBookingId(booking.getBookingId()).orElseGet(() -> {
			ServiceRecord record = new ServiceRecord();
			record.setBooking(booking);
			record.setStatus(ServiceRecord.ServiceStatus.PENDING);
			record.setEstimatedHours(booking.getCatalog() != null ? booking.getCatalog().getDurationHours() : null);
			return recordRepo.save(record);
		});
	}

	@Transactional
	public ServiceRecord reassignAdvisor(Integer bookingId, AppUser newAdvisor) {
		log.info("Reassigning advisor for bookingId={} newAdvisorId={}", bookingId, newAdvisor.getUserId());
		if (!bookingRepo.existsById(bookingId)) {
			throw new NotFoundException("Booking not found.");
		}
		ServiceRecord record = recordRepo.findByBookingBookingId(bookingId)
				.orElseThrow(() -> new NotFoundException("Booking does not have an assigned advisor yet."));

		ServiceAdvisor newAdvisorProfile = requireAssignableAdvisor(newAdvisor);
		ensureAdvisorSlotAvailable(record.getBooking(), newAdvisorProfile.getAdvisorId(), bookingId);

		ServiceAdvisor currentAdvisor = record.getAdvisor();
		if (currentAdvisor != null && currentAdvisor.getAdvisorId().equals(newAdvisorProfile.getAdvisorId())) {
			throw new ConflictException("Booking is already assigned to this advisor.");
		}
		if (currentAdvisor != null) {
			advisorService.decrementLoad(currentAdvisor.getAdvisorId());
		}

		record.setAdvisor(newAdvisorProfile);
		if (record.getStatus() == null) {
			record.setStatus(ServiceRecord.ServiceStatus.PENDING);
		}
		if (record.getBooking() != null) {
			record.getBooking().setAdvisor(newAdvisorProfile.getUser());
			bookingRepo.save(record.getBooking());
		}
		ServiceRecord saved = recordRepo.save(record);

		advisorService.incrementLoad(newAdvisorProfile.getAdvisorId());
		return saved;
	}

	@Transactional
	public boolean unassignAdvisor(Integer bookingId) {
		log.info("Unassigning advisor from bookingId={}", bookingId);
		ServiceRecord record = recordRepo.findByBookingBookingId(bookingId).orElse(null);
		if (record == null || record.getAdvisor() == null) {
			return false;
		}
		ServiceAdvisor currentAdvisor = record.getAdvisor();
		record.setAdvisor(null);
		record.setStatus(ServiceRecord.ServiceStatus.PENDING);
		if (record.getBooking() != null) {
			record.getBooking().setAdvisor(null);
			bookingRepo.save(record.getBooking());
		}
		recordRepo.save(record);
		advisorService.decrementLoad(currentAdvisor.getAdvisorId());
		return true;
	}

	@Transactional
	public void startService(Integer serviceId) {
		log.info("Starting service serviceId={}", serviceId);
		ServiceRecord r = recordRepo.findById(serviceId)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		if (r.getStatus() == ServiceRecord.ServiceStatus.COMPLETED)
			throw new ConflictException("Service is already completed.");
		if (r.getStatus() == ServiceRecord.ServiceStatus.IN_PROGRESS)
			throw new ConflictException("Service is already in progress.");
		if (r.getStatus() != ServiceRecord.ServiceStatus.PENDING)
			throw new BusinessException("Only pending services can be started.");
		r.setStatus(ServiceRecord.ServiceStatus.IN_PROGRESS);
		r.setServiceStartDate(LocalDateTime.now());
		recordRepo.save(r);
	}

	@Transactional
	public void saveItems(Integer serviceId, List<ServiceItem> items) {
		log.info("Saving BOM items for serviceId={} itemCount={}", serviceId, items != null ? items.size() : 0);
		ServiceRecord r = recordRepo.findById(serviceId)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		if (r.getStatus() != ServiceRecord.ServiceStatus.IN_PROGRESS)
			throw new BusinessException(
					"Cannot add items to a service that has not started. Click 'Start Service' first.");
		if (items == null || items.isEmpty())
			throw new BusinessException("Add at least one BOM item before saving.");
		itemRepo.deleteByServiceRecord(r);
		for (ServiceItem item : items) {
			validateServiceItem(item);
			item.setServiceRecord(r);
		}
		itemRepo.saveAll(items);
	}

	@Transactional
	public ServiceRecord updateRemarks(Integer serviceId, String remarks) {
		ServiceRecord r = recordRepo.findById(serviceId)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		r.setRemarks(remarks != null ? remarks.trim() : null);
		return recordRepo.save(r);
	}

	public WorkItemCatalog requireWorkItem(Integer workItemId) {
		return workItemService.findById(workItemId).orElseThrow(() -> new NotFoundException("Work item not found."));
	}

	@Transactional
	public Invoice completeService(Integer serviceId, BigDecimal actualHours, String remarks,
			List<ServiceItem> newItems, AppUser advisor) {
		ServiceRecord r = recordRepo.findById(serviceId)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		if (r.getStatus() == ServiceRecord.ServiceStatus.COMPLETED)
			throw new ConflictException("Service has already been completed.");
		if (r.getStatus() != ServiceRecord.ServiceStatus.IN_PROGRESS)
			throw new BusinessException("Only in-progress services can be completed.");
		if (actualHours == null || actualHours.compareTo(new BigDecimal("0.1")) < 0
				|| actualHours.compareTo(new BigDecimal("24")) > 0) {
			throw new BusinessException("Actual hours must be between 0.1 and 24.");
		}

		r.setStatus(ServiceRecord.ServiceStatus.COMPLETED);
		r.setServiceEndDate(LocalDateTime.now());
		r.setActualHours(actualHours);
		r.setRemarks(remarks != null ? remarks.trim() : null);

		List<ServiceItem> finalItems;
		if (newItems != null && !newItems.isEmpty()) {
			itemRepo.deleteByServiceRecord(r);
			for (ServiceItem item : newItems) {
				validateServiceItem(item);
				item.setServiceRecord(r);
			}
			itemRepo.saveAll(newItems);
			finalItems = newItems;
		} else {
			finalItems = itemRepo.findByServiceRecord(r);
		}
		if (finalItems.isEmpty()) {
			throw new BusinessException("Add BOM items before completing the service.");
		}
		recordRepo.save(r);

		ServiceBooking b = r.getBooking();

		// Update vehicle's last service date and compute next service due date
		Vehicle vehicle = b.getVehicle();
		if (vehicle != null) {
			LocalDate today = LocalDate.now();
			vehicle.setLastServiceDate(today);
			int intervalKm = vehicle.getServiceIntervalKm() != null ? vehicle.getServiceIntervalKm() : 10000;
			int monthsAhead = Math.max(1, (intervalKm / 10000) * 6);
			vehicle.setNextServiceDue(today.plusMonths(monthsAhead));
			vehicleRepo.save(vehicle);
		}

		b.setBookingStatus(ServiceBooking.BookingStatus.COMPLETED);
		bookingRepo.save(b);
		releaseBookingSlot(b);
		if (b.getVehicle() != null && b.getVehicle().getUser() != null
				&& b.getVehicle().getUser().getUserId() != null) {
			userService.refreshLoyaltyBadge(b.getVehicle().getUser().getUserId());
		}

		if (r.getAdvisor() != null)
			advisorService.decrementLoad(r.getAdvisor().getAdvisorId());

		BigDecimal itemsTotal = BigDecimal.ZERO;
		for (ServiceItem item : finalItems)
			if (item.getTotalPrice() != null)
				itemsTotal = itemsTotal.add(item.getTotalPrice());

		BigDecimal overtime = BigDecimal.ZERO;
		if (r.getEstimatedHours() != null && actualHours != null && actualHours.compareTo(r.getEstimatedHours()) > 0) {
			BigDecimal extra = actualHours.subtract(r.getEstimatedHours());
			BigDecimal rate = (r.getAdvisor() != null && r.getAdvisor().getOvertimeRate() != null)
					? r.getAdvisor().getOvertimeRate()
					: AdvisorService.DEFAULT_OVERTIME_RATE;
			overtime = extra.multiply(rate);
		}

		BigDecimal basePrice = BigDecimal.ZERO;
		if (b.getCatalog() != null && b.getCatalog().getBasePrice() != null)
			basePrice = b.getCatalog().getBasePrice();

		Invoice inv = invoiceService.getOrCreateForRecord(r);
		inv.setServiceRecord(r);
		inv.setBaseServicePrice(basePrice);
		BigDecimal bookingCharge = inv.getBookingCharge() != null ? inv.getBookingCharge()
				: b.getCatalog() != null && b.getCatalog().getBookingCharge() != null
						? b.getCatalog().getBookingCharge()
						: BigDecimal.ZERO;
		BigDecimal advanceAmount = inv.getAdvanceAmount() != null ? inv.getAdvanceAmount() : BigDecimal.ZERO;
		BigDecimal total = basePrice.add(itemsTotal).add(overtime).add(bookingCharge);
		inv.setBookingCharge(bookingCharge);
		inv.setAdvanceAmount(advanceAmount);
		inv.setAdvancePaid(advanceAmount.compareTo(BigDecimal.ZERO) > 0);
		inv.setItemsTotal(itemsTotal);
		inv.setOvertimeCharge(overtime);
		inv.setTotalAmount(total);
		inv = invoiceService.save(inv);
		Invoice saved = invoiceService.refreshInvoicePaymentStatus(inv);

		log.info("Service completed serviceId={} advisorId={} actualHours={}", serviceId,
				r.getAdvisor() != null ? r.getAdvisor().getAdvisorId() : null, actualHours);
		notificationService.send(b.getVehicle().getUser(), Notification.NotificationType.COMPLETION,
				"Service Completed", "Your vehicle service is complete. Invoice #" + saved.getInvoiceId()
						+ " generated. Total: \u20b9" + total,
				b);
		logCompletionHistory(b, advisor);
		return saved;
	}

	private void validateServiceItem(ServiceItem item) {
		if (item == null || item.getWorkItem() == null) {
			throw new BusinessException("Work item is required.");
		}
		Integer quantity = item.getQuantity();
		if (quantity == null || quantity < 1 || quantity > 99) {
			throw new BusinessException("Quantity must be between 1 and 99.");
		}
		if (item.getWorkItem().getItemType() == WorkItemCatalog.ItemType.LABOR && quantity != 1) {
			throw new BusinessException("Labor items must use quantity 1.");
		}
	}

	private void logCompletionHistory(ServiceBooking booking, AppUser actor) {
		if (booking == null || actor == null) {
			return;
		}
		try {
			BookingHistory history = new BookingHistory();
			history.setBooking(booking);
			history.setActionType(BookingHistory.ActionType.COMPLETED);
			history.setNewServiceDate(java.time.LocalDate.now());
			history.setNewTimeSlot(booking.getTimeSlot());
			history.setActionBy(actor);
			historyRepo.save(history);
		} catch (RuntimeException ex) {
			log.warn(
					"Completion succeeded but booking history could not be saved for booking {}. "
							+ "Apply db_update_audit_logs.sql so booking_history.action_type includes COMPLETED.",
					booking.getBookingId(), ex);
		}
	}

	private void releaseBookingSlot(ServiceBooking booking) {
		if (booking == null || booking.getServiceDate() == null || booking.getTimeSlot() == null
				|| booking.getTimeSlot().isBlank()) {
			return;
		}
		availabilityRepo.findByServiceDateAndTimeSlot(booking.getServiceDate(), booking.getTimeSlot()).ifPresent(slot -> {
			int current = slot.getCurrentBookings() == null ? 0 : slot.getCurrentBookings();
			int next = Math.max(0, current - 1);
			slot.setCurrentBookings(next);
			Integer max = slot.getMaxBookings();
			java.time.LocalTime slotStart = SlotTimeUtils.parseSlotStartTime(slot.getTimeSlot());
			boolean slotStartedOrPast = slot.getServiceDate().isBefore(LocalDate.now())
					|| (slot.getServiceDate().isEqual(LocalDate.now())
							&& slotStart != null && !slotStart.isAfter(LocalTime.now()));
			slot.setIsAvailable(!slotStartedOrPast && (max == null || next < max));
			availabilityRepo.save(slot);
		});
	}
}
