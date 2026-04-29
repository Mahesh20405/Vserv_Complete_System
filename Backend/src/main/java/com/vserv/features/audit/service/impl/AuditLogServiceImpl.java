package com.vserv.features.audit.service.impl;

import com.vserv.features.audit.mapper.AuditLogMapper;

import com.vserv.features.audit.repository.BookingHistoryRepository;

import com.vserv.features.audit.service.AuditLogService;

import com.vserv.entity.BookingHistory;
import com.vserv.features.audit.dto.AuditLogDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDate;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {
	private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

	private final BookingHistoryRepository historyRepo;

	public AuditLogServiceImpl(BookingHistoryRepository historyRepo) {
		this.historyRepo = historyRepo;
	}

	// log omitted – read-only list
	public List<AuditLogDto> findAll(String actionType, String bookingQuery, LocalDate dateFrom, LocalDate dateTo,
			String sort) {
		log.info("Fetching audit logs actionType={} dateFrom={} dateTo={} sort={}", actionType, dateFrom, dateTo, sort);
		boolean oldestFirst = "oldest".equalsIgnoreCase(sort);
		BookingHistory.ActionType parsedAction = parseActionType(actionType);
		String normalizedQuery = bookingQuery != null ? bookingQuery.trim().toLowerCase() : "";

		List<AuditLogDto> logs = historyRepo.findFiltered(parsedAction, dateFrom, dateTo).stream()
				.map(AuditLogMapper::toDto).filter(log -> matchesBooking(log, normalizedQuery)).toList();
		if (!oldestFirst) {
			return logs;
		}
		List<AuditLogDto> oldestFirstLogs = new ArrayList<>(logs);
		Collections.reverse(oldestFirstLogs);
		return oldestFirstLogs;
	}

	private BookingHistory.ActionType parseActionType(String actionType) {
		if (actionType == null || actionType.isBlank()) {
			return null;
		}
		return BookingHistory.ActionType.valueOf(actionType.trim().toUpperCase());
	}

	private boolean matchesBooking(AuditLogDto log, String bookingQuery) {
		if (bookingQuery == null || bookingQuery.isBlank()) {
			return true;
		}
		String bookingId = log.getBookingId() != null ? String.valueOf(log.getBookingId()) : "";
		String bookingNumber = log.getBookingNumber() != null ? log.getBookingNumber().toLowerCase() : "";
		return bookingId.contains(bookingQuery) || bookingNumber.contains(bookingQuery);
	}
}
