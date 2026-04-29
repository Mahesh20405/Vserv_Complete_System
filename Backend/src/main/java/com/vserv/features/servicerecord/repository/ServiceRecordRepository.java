package com.vserv.features.servicerecord.repository;

import com.vserv.entity.ServiceRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Integer> {
	List<ServiceRecord> findByAdvisorUserUserIdOrderByServiceStartDateDesc(Integer advisorUserId);

	List<ServiceRecord> findByAdvisorUserUserIdAndStatusOrderByServiceStartDateDesc(Integer advisorUserId,
			ServiceRecord.ServiceStatus status);

	Optional<ServiceRecord> findByBookingBookingId(Integer bookingId);

	List<ServiceRecord> findByBookingBookingIdIn(List<Integer> bookingIds);

	long countByStatus(ServiceRecord.ServiceStatus status);

	long countByBookingVehicleUserUserIdAndStatus(Integer userId, ServiceRecord.ServiceStatus status);

	@Query("""
			SELECT sr.booking.vehicle.user.userId, COUNT(sr)
			FROM ServiceRecord sr
			WHERE sr.booking.vehicle.user.userId IN :userIds
			  AND sr.status = :status
			GROUP BY sr.booking.vehicle.user.userId
			""")
	List<Object[]> countByCustomerIdsAndStatus(@Param("userIds") List<Integer> userIds,
			@Param("status") ServiceRecord.ServiceStatus status);

	@Query("""
			SELECT COUNT(sr) > 0
			FROM ServiceRecord sr
			WHERE sr.advisor.advisorId = :advisorId
			  AND sr.booking.serviceDate = :serviceDate
			  AND sr.booking.timeSlot = :timeSlot
			  AND sr.booking.bookingStatus <> :cancelledStatus
			  AND sr.status <> :completedStatus
			  AND (:excludedBookingId IS NULL OR sr.booking.bookingId <> :excludedBookingId)
			""")
	boolean existsActiveAssignmentForAdvisorAtSlot(@Param("advisorId") Integer advisorId,
			@Param("serviceDate") java.time.LocalDate serviceDate, @Param("timeSlot") String timeSlot,
			@Param("excludedBookingId") Integer excludedBookingId,
			@Param("cancelledStatus") com.vserv.entity.ServiceBooking.BookingStatus cancelledStatus,
			@Param("completedStatus") ServiceRecord.ServiceStatus completedStatus);

	@Query("""
			SELECT COUNT(sr) > 0
			FROM ServiceRecord sr
			WHERE sr.advisor.user.userId = :userId
			AND sr.status <> com.vserv.entity.ServiceRecord.ServiceStatus.COMPLETED
			AND sr.booking.bookingStatus <> com.vserv.entity.ServiceBooking.BookingStatus.CANCELLED
			""")
	boolean existsOpenAssignmentsForAdvisorUser(@Param("userId") Integer userId);

	List<ServiceRecord> findAllByOrderByServiceStartDateDesc();

	List<ServiceRecord> findAllByStatusOrderByServiceStartDateDesc(ServiceRecord.ServiceStatus status);
}
