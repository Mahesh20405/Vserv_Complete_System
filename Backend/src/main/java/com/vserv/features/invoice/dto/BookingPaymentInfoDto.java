package com.vserv.features.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingPaymentInfoDto {
	private final String paymentStatus;
	private final BigDecimal bookingChargeAmount;
	private final String bookingChargePaymentMethod;
	private final String bookingChargeTransactionReference;
	private final LocalDateTime bookingChargePaidAt;

	public BookingPaymentInfoDto(String paymentStatus, BigDecimal bookingChargeAmount,
			String bookingChargePaymentMethod, String bookingChargeTransactionReference,
			LocalDateTime bookingChargePaidAt) {
		this.paymentStatus = paymentStatus;
		this.bookingChargeAmount = bookingChargeAmount;
		this.bookingChargePaymentMethod = bookingChargePaymentMethod;
		this.bookingChargeTransactionReference = bookingChargeTransactionReference;
		this.bookingChargePaidAt = bookingChargePaidAt;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public BigDecimal getBookingChargeAmount() {
		return bookingChargeAmount;
	}

	public String getBookingChargePaymentMethod() {
		return bookingChargePaymentMethod;
	}

	public String getBookingChargeTransactionReference() {
		return bookingChargeTransactionReference;
	}

	public LocalDateTime getBookingChargePaidAt() {
		return bookingChargePaidAt;
	}
}
