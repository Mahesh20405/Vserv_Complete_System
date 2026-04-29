package com.vserv.features.audit.mapper;

import com.vserv.entity.BookingHistory;
import com.vserv.features.audit.dto.AuditLogDto;

public final class AuditLogMapper {
	private AuditLogMapper() {
	}

	public static AuditLogDto toDto(BookingHistory history) {
		return AuditLogDto.from(history);
	}
}
