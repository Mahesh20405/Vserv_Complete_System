package com.vserv.features.booking.repository;

import com.vserv.entity.ServiceBooking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Integer> {
	List<ServiceBooking> findAllByOrderByCreatedAtDesc();

	List<ServiceBooking> findAllByBookingStatusOrderByCreatedAtDesc(ServiceBooking.BookingStatus status);

	@Query("SELECT b FROM ServiceBooking b JOIN b.vehicle v WHERE v.user.userId = :userId ORDER BY b.createdAt DESC")
	List<ServiceBooking> findByCustomer(@Param("userId") Integer userId);

	@Query("""
			SELECT b
			FROM ServiceBooking b
			JOIN b.vehicle v
			WHERE v.user.userId = :userId
				AND b.bookingStatus = :status
			ORDER BY b.createdAt DESC
			""")
	List<ServiceBooking> findByCustomerAndStatus(@Param("userId") Integer userId,
			@Param("status") ServiceBooking.BookingStatus status);

	@Query("""
			SELECT r.booking
			FROM ServiceRecord r
			WHERE r.advisor.user.userId = :advisorUserId
			ORDER BY r.booking.createdAt DESC
			""")
	List<ServiceBooking> findByAdvisor(@Param("advisorUserId") Integer advisorUserId);

	@Query("""
			SELECT r.booking
			FROM ServiceRecord r
			WHERE r.advisor.user.userId = :advisorUserId
				AND r.booking.bookingStatus = :status
			ORDER BY r.booking.createdAt DESC
			""")
	List<ServiceBooking> findByAdvisorAndStatus(@Param("advisorUserId") Integer advisorUserId,
			@Param("status") ServiceBooking.BookingStatus status);

	long countByBookingStatus(ServiceBooking.BookingStatus status);

	List<ServiceBooking> findTop10ByOrderByCreatedAtDesc();

	@Query("""
			SELECT b.bookingStatus, COUNT(b)
			FROM ServiceBooking b
			GROUP BY b.bookingStatus
			""")
	List<Object[]> countGroupedByBookingStatus();

	@Query("""
			SELECT YEAR(b.createdAt), MONTH(b.createdAt), COUNT(b)
			FROM ServiceBooking b
			WHERE b.createdAt >= :from
			GROUP BY YEAR(b.createdAt), MONTH(b.createdAt)
			ORDER BY YEAR(b.createdAt), MONTH(b.createdAt)
			""")
	List<Object[]> countCreatedByMonthSince(@Param("from") LocalDateTime from);

	@Query("""
			SELECT b.catalog.serviceType, COUNT(b)
			FROM ServiceBooking b
			WHERE b.catalog IS NOT NULL AND b.catalog.serviceType IS NOT NULL
			GROUP BY b.catalog.serviceType
			""")
	List<Object[]> countGroupedByServiceType();

	@Query("SELECT COUNT(b) FROM ServiceBooking b WHERE b.vehicle.vehicleId=:vId AND b.serviceDate=:date AND b.bookingStatus NOT IN ('CANCELLED','COMPLETED')")
	long countActiveForVehicleOnDate(@Param("vId") Integer vId, @Param("date") LocalDate date);

	@Query("SELECT COUNT(b) FROM ServiceBooking b WHERE b.vehicle.vehicleId=:vId AND b.serviceDate=:date AND b.timeSlot=:slot AND b.bookingStatus NOT IN ('CANCELLED','COMPLETED')")
	long countActiveForVehicleOnDateAndSlot(@Param("vId") Integer vId, @Param("date") LocalDate date,
			@Param("slot") String slot);

	@Query("""
			SELECT COUNT(b) > 0
			FROM ServiceBooking b
			WHERE b.vehicle.vehicleId = :vehicleId
			AND b.bookingStatus IN :statuses
			""")
	boolean existsOpenBookingsForVehicle(@Param("vehicleId") Integer vehicleId,
			@Param("statuses") Collection<ServiceBooking.BookingStatus> statuses);

	@Query("""
			SELECT COUNT(b) > 0
			FROM ServiceBooking b
			JOIN b.vehicle v
			WHERE v.user.userId = :userId
			AND b.bookingStatus IN :statuses
			""")
	boolean existsOpenBookingsForCustomer(@Param("userId") Integer userId,
			@Param("statuses") Collection<ServiceBooking.BookingStatus> statuses);

	@Query("""
			SELECT b FROM ServiceBooking b
			JOIN ServiceRecord r ON r.booking = b
			WHERE b.bookingStatus = 'CONFIRMED'
			  AND r.status = 'PENDING'
			  AND r.serviceStartDate IS NULL
			  AND b.serviceDate < :today
			ORDER BY b.serviceDate ASC, b.timeSlot ASC
			""")
	List<ServiceBooking> findOverdueConfirmedBookings(@Param("today") LocalDate today);
}
