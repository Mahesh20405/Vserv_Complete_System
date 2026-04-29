package com.vserv.features.booking.dto;

import com.vserv.entity.BookingHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingHistoryDto {
	private Integer historyId;
	private String actionType;
	private LocalDate oldServiceDate;
	private LocalDate newServiceDate;
	private String oldTimeSlot;
	private String newTimeSlot;
	private String reason;
	private String actionByName;
	private LocalDateTime actionDate;

	public static BookingHistoryDto from(BookingHistory history) {
		BookingHistoryDto dto = new BookingHistoryDto();
		dto.historyId = history.getHistoryId();
		dto.actionType = history.getActionType() != null ? history.getActionType().name() : null;
		dto.oldServiceDate = history.getOldServiceDate();
		dto.newServiceDate = history.getNewServiceDate();
		dto.oldTimeSlot = history.getOldTimeSlot();
		dto.newTimeSlot = history.getNewTimeSlot();
		dto.reason = history.getReason();
		dto.actionDate = history.getActionDate();
		if (history.getActionBy() != null) {
			dto.actionByName = history.getActionBy().getFullName();
		}
		return dto;
	}

	public Integer getHistoryId() {
		return historyId;
	}

	public String getActionType() {
		return actionType;
	}

	public LocalDate getOldServiceDate() {
		return oldServiceDate;
	}

	public LocalDate getNewServiceDate() {
		return newServiceDate;
	}

	public String getOldTimeSlot() {
		return oldTimeSlot;
	}

	public String getNewTimeSlot() {
		return newTimeSlot;
	}

	public String getReason() {
		return reason;
	}

	public String getActionByName() {
		return actionByName;
	}

	public LocalDateTime getActionDate() {
		return actionDate;
	}
}
