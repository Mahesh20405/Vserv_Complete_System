package com.vserv.features.dashboard.service.impl;

import com.vserv.features.dashboard.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vserv.features.advisor.service.AdvisorService;
import com.vserv.features.booking.service.BookingService;
import com.vserv.entity.ServiceBooking;
import com.vserv.features.dashboard.dto.DashboardStatsDto;
import com.vserv.features.invoice.repository.InvoiceRepository;
import com.vserv.features.invoice.service.InvoiceService;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.servicerecord.service.ServiceRecordService;
import com.vserv.features.user.service.UserService;
import com.vserv.features.vehicle.service.VehicleService;

@Service
public class DashboardServiceImpl implements DashboardService {
	private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

	private final BookingService bookingService;
	private final InvoiceService invoiceService;
	private final InvoiceRepository invoiceRepository;
	private final UserService userService;
	private final VehicleService vehicleService;
	private final AdvisorService advisorService;
	private final ServiceRecordService recordService;

	public DashboardServiceImpl(BookingService bookingService, InvoiceService invoiceService,
			InvoiceRepository invoiceRepository, UserService userService, VehicleService vehicleService,
			AdvisorService advisorService, ServiceRecordService recordService) {
		this.bookingService = bookingService;
		this.invoiceService = invoiceService;
		this.invoiceRepository = invoiceRepository;
		this.userService = userService;
		this.vehicleService = vehicleService;
		this.advisorService = advisorService;
		this.recordService = recordService;
	}

	public DashboardStatsDto getStats() {
		log.info("Building dashboard stats");
		DashboardStatsDto dto = new DashboardStatsDto();

		Map<String, Long> bookingCounts = new LinkedHashMap<>();
		for (Object[] row : bookingService.countByStatusGrouped()) {
			ServiceBooking.BookingStatus status = (ServiceBooking.BookingStatus) row[0];
			Long count = (Long) row[1];
			bookingCounts.put(status.name(), count);
		}
		long pending = bookingCounts.getOrDefault(ServiceBooking.BookingStatus.PENDING.name(), 0L);
		long confirmed = bookingCounts.getOrDefault(ServiceBooking.BookingStatus.CONFIRMED.name(), 0L);
		long rescheduled = bookingCounts.getOrDefault(ServiceBooking.BookingStatus.RESCHEDULED.name(), 0L);
		long completed = bookingCounts.getOrDefault(ServiceBooking.BookingStatus.COMPLETED.name(), 0L);
		long cancelled = bookingCounts.getOrDefault(ServiceBooking.BookingStatus.CANCELLED.name(), 0L);

		dto.setTotalBookings(pending + confirmed + rescheduled + completed + cancelled);
		dto.setPendingBookings(pending);
		dto.setConfirmedBookings(confirmed);
		dto.setCompletedBookings(completed);
		dto.setCancelledBookings(cancelled);
		dto.setTotalUsers(userService.countActiveUsers());
		dto.setTotalVehicles(vehicleService.countActiveVehicles());
		dto.setPendingInvoices(invoiceService.countPending());
		dto.setInProgressServices(recordService.countByStatus(ServiceRecord.ServiceStatus.IN_PROGRESS));

		BigDecimal revenue = invoiceService.totalRevenue();
		dto.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

		// Booking status breakdown (for donut chart)
		Map<String, Long> statusBreakdown = new LinkedHashMap<>();
		statusBreakdown.put("PENDING", pending);
		statusBreakdown.put("CONFIRMED", confirmed);
		statusBreakdown.put("RESCHEDULED", rescheduled);
		statusBreakdown.put("COMPLETED", completed);
		statusBreakdown.put("CANCELLED", cancelled);
		dto.setBookingStatusBreakdown(statusBreakdown);

		// Booking activity by month (bar chart), last 6 months
		Map<String, Long> byMonth = new LinkedHashMap<>();
		LocalDate today = LocalDate.now();
		LocalDateTime sixMonthsAgo = today.minusMonths(5).withDayOfMonth(1).atStartOfDay();
		Map<String, Long> dbMonthCounts = new LinkedHashMap<>();
		for (Object[] row : bookingService.countCreatedByMonthSince(sixMonthsAgo)) {
			Integer year = (Integer) row[0];
			Integer month = (Integer) row[1];
			Long count = (Long) row[2];
			LocalDate monthDate = LocalDate.of(year, month, 1);
			String label = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year;
			dbMonthCounts.put(label, count);
		}
		for (int i = 5; i >= 0; i--) {
			LocalDate d = today.minusMonths(i);
			String label = d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + d.getYear();
			byMonth.put(label, dbMonthCounts.getOrDefault(label, 0L));
		}
		List<Map<String, Object>> activityList = new ArrayList<>();
		byMonth.forEach((month, count) -> activityList.add(Map.of("month", month, "count", count)));
		dto.setBookingActivityByMonth(activityList);

		Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();
		for (int i = 5; i >= 0; i--) {
			LocalDate d = today.minusMonths(i);
			String label = d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + d.getYear();
			revenueByMonth.put(label, BigDecimal.ZERO);
		}
		LocalDate revenueSince = today.minusMonths(5).withDayOfMonth(1);
		for (Object[] row : invoiceRepository.sumPaidByMonthSince(revenueSince)) {
			Integer year = (Integer) row[0];
			Integer month = (Integer) row[1];
			BigDecimal amount = row[2] instanceof BigDecimal ? (BigDecimal) row[2] : BigDecimal.ZERO;
			LocalDate monthDate = LocalDate.of(year, month, 1);
			String label = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " "
					+ monthDate.getYear();
			revenueByMonth.computeIfPresent(label, (key, current) -> current.add(amount));
		}
		List<Map<String, Object>> revenueList = new ArrayList<>();
		revenueByMonth.forEach((month, amount) -> {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("month", month);
			row.put("amount", amount);
			revenueList.add(row);
		});
		dto.setRevenueByMonth(revenueList);

		// Recent bookings (last 10)
		List<Map<String, Object>> recentList = bookingService.findRecent().stream().map(b -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("bookingId", b.getBookingId());
			m.put("status", b.getBookingStatus() != null ? b.getBookingStatus().name() : null);
			m.put("serviceDate", b.getServiceDate());
			m.put("timeSlot", b.getTimeSlot());
			if (b.getVehicle() != null) {
				m.put("vehicleInfo", b.getVehicle().getBrand() + " " + b.getVehicle().getModel());
				if (b.getVehicle().getUser() != null)
					m.put("ownerName", b.getVehicle().getUser().getFullName());
			}
			if (b.getCatalog() != null)
				m.put("serviceName", b.getCatalog().getServiceName());
			return m;
		}).toList();
		dto.setRecentBookings(recentList);

		// Active advisors sidebar (top 5 by load)
		List<Map<String, Object>> advisorList = advisorService.findTopActive().stream().map(a -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("advisorId", a.getAdvisorId());
			m.put("fullName", a.getUser() != null ? a.getUser().getFullName() : "");
			m.put("status", a.getAvailabilityStatus() != null ? a.getAvailabilityStatus().name() : null);
			m.put("currentLoad", a.getCurrentLoad());
			m.put("specialization", a.getSpecialization());
			return m;
		}).toList();
		dto.setActiveAdvisors(advisorList);

		// Service mix (by catalog service type)
		Map<String, Long> serviceMix = new LinkedHashMap<>();
		for (Object[] row : bookingService.countByServiceTypeGrouped()) {
			Enum<?> serviceType = (Enum<?>) row[0];
			Long count = (Long) row[1];
			serviceMix.put(serviceType.name(), count);
		}
		dto.setServiceMix(serviceMix);

		return dto;
	}
}
