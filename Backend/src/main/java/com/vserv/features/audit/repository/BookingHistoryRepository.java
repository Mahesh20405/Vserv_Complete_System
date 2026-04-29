package com.vserv.features.audit.repository;

import com.vserv.entity.BookingHistory;
import com.vserv.entity.ServiceBooking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Integer> {
	List<BookingHistory> findByBookingOrderByActionDateDesc(ServiceBooking booking);

	@Query("""
			SELECT h
			FROM BookingHistory h
			JOIN FETCH h.booking b
			LEFT JOIN FETCH b.vehicle v
			LEFT JOIN FETCH b.catalog c
			LEFT JOIN FETCH v.user owner
			LEFT JOIN FETCH h.actionBy actor
			WHERE (:actionType IS NULL OR h.actionType = :actionType)
			AND (:dateFrom IS NULL OR FUNCTION('DATE', h.actionDate) >= :dateFrom)
			AND (:dateTo IS NULL OR FUNCTION('DATE', h.actionDate) <= :dateTo)
			ORDER BY h.actionDate DESC
			""")
	List<BookingHistory> findFiltered(@Param("actionType") BookingHistory.ActionType actionType,
			@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
}
