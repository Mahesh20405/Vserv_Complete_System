package com.vserv.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_history")
public class BookingHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "history_id")
	private Integer historyId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", nullable = false)
	@JsonIgnore
	private ServiceBooking booking;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false)
	private ActionType actionType;

	@Column(name = "old_service_date")
	private LocalDate oldServiceDate;

	@Column(name = "new_service_date")
	private LocalDate newServiceDate;

	@Column(name = "old_time_slot", length = 20)
	private String oldTimeSlot;

	@Column(name = "new_time_slot", length = 20)
	private String newTimeSlot;

	@Column(columnDefinition = "TEXT")
	private String reason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "action_by", nullable = false)
	private AppUser actionBy;

	@Column(name = "action_date")
	private LocalDateTime actionDate = LocalDateTime.now();

	public BookingHistory() {
	}

	public Integer getHistoryId() {
		return historyId;
	}

	public void setHistoryId(Integer historyId) {
		this.historyId = historyId;
	}

	public ServiceBooking getBooking() {
		return booking;
	}

	public void setBooking(ServiceBooking booking) {
		this.booking = booking;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public LocalDate getOldServiceDate() {
		return oldServiceDate;
	}

	public void setOldServiceDate(LocalDate oldServiceDate) {
		this.oldServiceDate = oldServiceDate;
	}

	public LocalDate getNewServiceDate() {
		return newServiceDate;
	}

	public void setNewServiceDate(LocalDate newServiceDate) {
		this.newServiceDate = newServiceDate;
	}

	public String getOldTimeSlot() {
		return oldTimeSlot;
	}

	public void setOldTimeSlot(String oldTimeSlot) {
		this.oldTimeSlot = oldTimeSlot;
	}

	public String getNewTimeSlot() {
		return newTimeSlot;
	}

	public void setNewTimeSlot(String newTimeSlot) {
		this.newTimeSlot = newTimeSlot;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public AppUser getActionBy() {
		return actionBy;
	}

	public void setActionBy(AppUser actionBy) {
		this.actionBy = actionBy;
	}

	public LocalDateTime getActionDate() {
		return actionDate;
	}

	public void setActionDate(LocalDateTime actionDate) {
		this.actionDate = actionDate;
	}

	public enum ActionType {
		CREATED, RESCHEDULED, CANCELLED, CONFIRMED, COMPLETED
	}
}
