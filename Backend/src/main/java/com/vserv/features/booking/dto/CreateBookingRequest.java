package com.vserv.features.booking.dto;

import com.vserv.core.util.ValidationPatterns;
import com.vserv.entity.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateBookingRequest {
	@NotNull(message = "Vehicle is required")
	private Integer vehicleId;
	@NotNull(message = "Service catalog is required")
	private Integer catalogId;
	@NotNull(message = "Service date is required")
	private LocalDate serviceDate;
	@NotBlank(message = "Time slot is required")
	@Size(max = 20, message = "Time slot must be at most 20 characters")
	@Pattern(regexp = ValidationPatterns.SLOT, message = "Time slot contains unsupported characters")
	private String timeSlot;
	@Size(max = 300, message = "Notes must be at most 300 characters")
	@Pattern(regexp = "^[a-zA-Z0-9\\s.,!?'\\-\\/():]+$", message = "Notes contain unsupported characters")
	private String notes;
	private Payment.PaymentMethod paymentMethod;
	@Size(max = 100, message = "Transaction reference must be at most 100 characters")
	@Pattern(regexp = ValidationPatterns.TRANSACTION_REFERENCE, message = "Transaction reference contains unsupported characters")
	private String transactionRef;
	private Boolean waiveBookingCharge;

	public Integer getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Integer v) {
		this.vehicleId = v;
	}

	public Integer getCatalogId() {
		return catalogId;
	}

	public void setCatalogId(Integer v) {
		this.catalogId = v;
	}

	public LocalDate getServiceDate() {
		return serviceDate;
	}

	public void setServiceDate(LocalDate v) {
		this.serviceDate = v;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String v) {
		this.timeSlot = v;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String v) {
		this.notes = v;
	}

	public Payment.PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(Payment.PaymentMethod v) {
		this.paymentMethod = v;
	}

	public String getTransactionRef() {
		return transactionRef;
	}

	public void setTransactionRef(String v) {
		this.transactionRef = (v != null && v.isBlank()) ? null : v;
	}

	public Boolean getWaiveBookingCharge() {
		return waiveBookingCharge;
	}

	public void setWaiveBookingCharge(Boolean waiveBookingCharge) {
		this.waiveBookingCharge = waiveBookingCharge;
	}
}
