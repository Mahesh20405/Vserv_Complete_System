package com.vserv.features.audit.service;

import com.vserv.features.audit.dto.AuditLogDto;

import java.time.LocalDate;
import java.util.List;

public interface AuditLogService {
	List<AuditLogDto> findAll(String actionType, String bookingQuery, LocalDate dateFrom, LocalDate dateTo,
			String sort);
}
