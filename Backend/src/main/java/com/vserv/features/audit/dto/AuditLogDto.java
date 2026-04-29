package com.vserv.features.audit.dto;

import com.vserv.entity.BookingHistory;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.Vehicle;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AuditLogDto {
	private Integer id;
	private Integer bookingId;
	private String bookingNumber;
	private String actionType;
	private LocalDate oldDate;
	private String oldSlot;
	private LocalDate newDate;
	private String newSlot;
	private Integer actionByUserId;
	private String actionByName;
	private LocalDateTime actionDate;
	private String reason;
	private String vehicleInfo;
	private String serviceName;

	public static AuditLogDto from(BookingHistory history) {
		AuditLogDto dto = new AuditLogDto();
		dto.id = history.getHistoryId();
		ServiceBooking booking = history.getBooking();
		dto.bookingId = booking != null ? booking.getBookingId() : null;
		dto.bookingNumber = buildBookingNumber(booking);
		dto.actionType = history.getActionType() != null ? history.getActionType().name() : null;
		dto.oldDate = history.getOldServiceDate();
		dto.oldSlot = history.getOldTimeSlot();
		dto.newDate = history.getNewServiceDate();
		dto.newSlot = history.getNewTimeSlot();
		dto.reason = history.getReason();
		dto.actionDate = history.getActionDate();
		if (history.getActionBy() != null) {
			dto.actionByUserId = history.getActionBy().getUserId();
			dto.actionByName = history.getActionBy().getFullName();
		}
		if (booking != null) {
			Vehicle vehicle = booking.getVehicle();
			if (vehicle != null) {
				String registration = vehicle.getRegistrationNumber() != null
						? " (" + vehicle.getRegistrationNumber() + ")"
						: "";
				dto.vehicleInfo = ((vehicle.getBrand() != null ? vehicle.getBrand() : "") + " "
						+ (vehicle.getModel() != null ? vehicle.getModel() : "")).trim() + registration;
				if (dto.vehicleInfo.isBlank()) {
					dto.vehicleInfo = booking.getArchivedVehicleInfo();
				}
			} else {
				dto.vehicleInfo = booking.getArchivedVehicleInfo();
			}
			if (booking.getCatalog() != null) {
				dto.serviceName = booking.getCatalog().getServiceName();
			}
		}
		return dto;
	}

	private static String buildBookingNumber(ServiceBooking booking) {
		if (booking == null || booking.getBookingId() == null) {
			return null;
		}
		int year = booking.getCreatedAt() != null ? booking.getCreatedAt().getYear() : LocalDate.now().getYear();
		return "BK-" + year + "-" + String.format("%04d", booking.getBookingId());
	}

	public Integer getId() {
		return id;
	}

	public Integer getBookingId() {
		return bookingId;
	}

	public String getBookingNumber() {
		return bookingNumber;
	}

	public String getActionType() {
		return actionType;
	}

	public LocalDate getOldDate() {
		return oldDate;
	}

	public String getOldSlot() {
		return oldSlot;
	}

	public LocalDate getNewDate() {
		return newDate;
	}

	public String getNewSlot() {
		return newSlot;
	}

	public Integer getActionByUserId() {
		return actionByUserId;
	}

	public String getActionByName() {
		return actionByName;
	}

	public LocalDateTime getActionDate() {
		return actionDate;
	}

	public String getReason() {
		return reason;
	}

	public String getVehicleInfo() {
		return vehicleInfo;
	}

	public String getServiceName() {
		return serviceName;
	}
}
