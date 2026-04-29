package com.vserv.features.booking.service;

import com.vserv.entity.AppUser;
import com.vserv.entity.BookingHistory;
import com.vserv.entity.Payment;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.ServiceCatalog;
import com.vserv.entity.ServiceRecord;
import com.vserv.entity.Vehicle;
import com.vserv.features.booking.dto.BookingDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {
	List<ServiceBooking> findAll(ServiceBooking.BookingStatus status);

	Optional<ServiceBooking> findById(Integer id);

	List<ServiceBooking> findByCustomer(AppUser user, ServiceBooking.BookingStatus status);

	List<ServiceBooking> findByAdvisor(AppUser advisor, ServiceBooking.BookingStatus status);

	List<ServiceBooking> findOverdueConfirmedBookings();

	BookingDto toDto(ServiceBooking booking);

	Optional<ServiceRecord> findRecordByBookingId(Integer bookingId);

	List<BookingDto> toDtos(List<ServiceBooking> bookings);

	long countByStatus(ServiceBooking.BookingStatus status);

	List<Object[]> countByStatusGrouped();

	List<Object[]> countCreatedByMonthSince(LocalDateTime from);

	List<Object[]> countByServiceTypeGrouped();

	List<ServiceBooking> findRecent();

	ServiceBooking create(Vehicle vehicle, ServiceCatalog catalog, LocalDate date, String slot, String notes,
			AppUser actor, Payment.PaymentMethod paymentMethod, String txRef, Boolean waiveBookingCharge);

	void confirm(Integer bookingId, AppUser actor);

	void cancel(Integer bookingId, String reason, AppUser actor);

	void reschedule(Integer bookingId, LocalDate newDate, String newSlot, String reason, AppUser actor);

	List<BookingHistory> getHistory(ServiceBooking booking);

	void confirmWithAdvisor(Integer bookingId, AppUser advisor, AppUser actor);

	void reassignAdvisor(Integer bookingId, AppUser newAdvisor, AppUser actor);
}
