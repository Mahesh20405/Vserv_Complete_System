package com.vserv.features.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

import com.vserv.entity.Invoice;
import com.vserv.entity.Payment;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.ServiceRecord;

public class BookingDto {
	private Integer bookingId;
	private Integer vehicleId;
	private String vehicleInfo;
	private Integer ownerId;
	private String ownerName;
	private Integer catalogId;
	private String serviceName;
	private LocalDate serviceDate;
	private String timeSlot;
	private String bookingStatus;
	private String serviceStatus;
	private String paymentStatus;
	private String bookingChargePaymentMethod;
	private String bookingChargeTransactionReference;
	private LocalDateTime bookingChargePaidAt;
	private java.math.BigDecimal bookingChargeAmount;
	private String bookingNotes;
	private LocalDateTime createdAt;
	private Integer advisorId;
	private String advisorName;

	public static BookingDto from(ServiceBooking b, ServiceRecord record, Invoice invoice) {
		return from(b, record, invoice, null);
	}

	public static BookingDto from(ServiceBooking b, ServiceRecord record, Invoice invoice,
			Payment bookingChargePayment) {
		BookingDto dto = new BookingDto();
		dto.bookingId = b.getBookingId();
		dto.serviceDate = b.getServiceDate();
		dto.timeSlot = b.getTimeSlot();
		dto.bookingStatus = b.getBookingStatus() != null ? b.getBookingStatus().name() : null;
		dto.bookingNotes = b.getBookingNotes();
		dto.createdAt = b.getCreatedAt();
		if (b.getVehicle() != null) {
			dto.vehicleId = b.getVehicle().getVehicleId();
			dto.vehicleInfo = b.getVehicle().getBrand() + " " + b.getVehicle().getModel() + " ("
					+ b.getVehicle().getRegistrationNumber() + ")";
			if (b.getVehicle().getUser() != null) {
				dto.ownerId = b.getVehicle().getUser().getUserId();
				dto.ownerName = b.getVehicle().getUser().getFullName();
			}
		}
		if (b.getCatalog() != null) {
			dto.catalogId = b.getCatalog().getCatalogId();
			dto.serviceName = b.getCatalog().getServiceName();
		}
		if (b.getAdvisor() != null) {
			dto.advisorId = b.getAdvisor().getUserId();
			dto.advisorName = b.getAdvisor().getFullName();
		}
		if (record != null) {
			if (record.getAdvisor() != null) {
				dto.advisorId = record.getAdvisor().getAdvisorId();
				dto.advisorName = record.getAdvisor().getUser() != null ? record.getAdvisor().getUser().getFullName()
						: null;
			}
			dto.serviceStatus = record.getStatus() != null ? record.getStatus().name() : null;
		}
		if (invoice != null) {
			dto.bookingChargeAmount = invoice.getBookingCharge();
			dto.paymentStatus = resolveEffectivePaymentStatus(invoice);
		}
		Payment latestBookingChargePayment = bookingChargePayment != null ? bookingChargePayment
				: invoice != null && invoice.getPayments() != null ? invoice.getPayments().stream()
						.filter(payment -> payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
						.filter(payment -> payment.getPaymentPurpose() == Payment.PaymentPurpose.BOOKING_CHARGE)
						.max(Comparator.comparing(Payment::getPaymentDate)).orElse(null) : null;
		if (latestBookingChargePayment != null) {
			dto.bookingChargePaymentMethod = latestBookingChargePayment.getPaymentMethod() != null
					? latestBookingChargePayment.getPaymentMethod().name()
					: null;
			dto.bookingChargeTransactionReference = latestBookingChargePayment.getTransactionReference();
			dto.bookingChargePaidAt = latestBookingChargePayment.getPaymentDate();
			if (dto.paymentStatus == null) {
				dto.paymentStatus = "PARTIALLY_PAID";
			}
		}
		return dto;
	}

	private static String resolveEffectivePaymentStatus(Invoice invoice) {
		if (invoice == null)
			return null;

		BigDecimal totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
		BigDecimal successfulPaid = invoice.getPayments() == null ? BigDecimal.ZERO
				: invoice.getPayments().stream().filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
						.map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		if (successfulPaid.compareTo(BigDecimal.ZERO) <= 0) {
			return invoice.getPaymentStatus() != null ? invoice.getPaymentStatus().name() : "PENDING";
		}
		if (totalAmount.compareTo(BigDecimal.ZERO) > 0 && successfulPaid.compareTo(totalAmount) >= 0) {
			String dbStatus = invoice.getPaymentStatus() != null ? invoice.getPaymentStatus().name() : "PARTIALLY_PAID";
			return "PAID".equals(dbStatus) ? "PAID" : "PARTIALLY_PAID";
		}
		return "PARTIALLY_PAID";
	}

	public Integer getBookingId() {
		return bookingId;
	}

	public String getBookingNumber() {
		return bookingId != null ? String.format("BK-%04d", bookingId) : null;
	}

	public Integer getVehicleId() {
		return vehicleId;
	}

	public String getVehicleInfo() {
		return vehicleInfo;
	}

	public Integer getOwnerId() {
		return ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public Integer getCatalogId() {
		return catalogId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public LocalDate getServiceDate() {
		return serviceDate;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public String getBookingStatus() {
		return bookingStatus;
	}

	public String getServiceStatus() {
		return serviceStatus;
	}

	public String getPaymentStatus() {
		return paymentStatus;
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

	public java.math.BigDecimal getBookingChargeAmount() {
		return bookingChargeAmount;
	}

	public String getBookingNotes() {
		return bookingNotes;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Integer getAdvisorId() {
		return advisorId;
	}

	public String getAdvisorName() {
		return advisorName;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public void setBookingChargePaymentMethod(String bookingChargePaymentMethod) {
		this.bookingChargePaymentMethod = bookingChargePaymentMethod;
	}

	public void setBookingChargeTransactionReference(String bookingChargeTransactionReference) {
		this.bookingChargeTransactionReference = bookingChargeTransactionReference;
	}

	public void setBookingChargePaidAt(LocalDateTime bookingChargePaidAt) {
		this.bookingChargePaidAt = bookingChargePaidAt;
	}

	public void setBookingChargeAmount(java.math.BigDecimal bookingChargeAmount) {
		this.bookingChargeAmount = bookingChargeAmount;
	}
}
