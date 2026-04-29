package com.vserv.features.invoice.dto;

import com.vserv.entity.Invoice;
import com.vserv.entity.Payment;
import com.vserv.entity.ServiceItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class InvoiceDto {
	private Integer invoiceId;
	private String invoiceNumber;
	private Integer serviceId;
	private Integer bookingId;
	private String serviceStatus;
	private String bookingStatus;
	private String vehicleInfo;
	private String customerName;
	private String serviceName;
	private BigDecimal baseServicePrice;
	private BigDecimal bookingCharge;
	private BigDecimal itemsTotal;
	private BigDecimal overtimeCharge;
	private BigDecimal totalAmount;
	private BigDecimal advanceAmount;
	private Boolean advancePaid;
	private BigDecimal bookingChargePaidAmount;
	private BigDecimal finalInvoicePaidAmount;
	private BigDecimal totalPaidAmount;
	private BigDecimal remainingBalance;
	private BigDecimal netAmountAfterAdvance;
	private String paymentStatus;
	private String paymentMethod;
	private String transactionReference;
	private String bookingChargePaymentMethod;
	private String bookingChargeTransactionReference;
	private LocalDate invoiceDate;
	private LocalDateTime paidAt;
	private LocalDateTime bookingChargePaidAt;
	private List<InvoiceLineItemDto> items;

	public static InvoiceDto from(Invoice inv, BigDecimal remainingBalance) {
		InvoiceDto dto = new InvoiceDto();
		dto.invoiceId = inv.getInvoiceId();
		dto.invoiceNumber = inv.getInvoiceId() != null ? String.format("INV-%04d", inv.getInvoiceId()) : null;
		dto.baseServicePrice = inv.getBaseServicePrice();
		dto.bookingCharge = inv.getBookingCharge();
		dto.itemsTotal = inv.getItemsTotal();
		dto.overtimeCharge = inv.getOvertimeCharge();
		dto.totalAmount = inv.getTotalAmount();
		dto.advanceAmount = inv.getAdvanceAmount();
		dto.advancePaid = inv.getAdvancePaid();
		dto.remainingBalance = remainingBalance;
		dto.netAmountAfterAdvance = (dto.totalAmount != null ? dto.totalAmount : BigDecimal.ZERO)
				.subtract(dto.advanceAmount != null ? dto.advanceAmount : BigDecimal.ZERO).max(BigDecimal.ZERO);
		dto.paymentStatus = inv.getPaymentStatus() != null ? inv.getPaymentStatus().name() : null;
		dto.invoiceDate = inv.getInvoiceDate();

		Payment latestPayment = inv.getPayments() == null ? null
				: inv.getPayments().stream()
						.filter(payment -> payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
						.filter(payment -> payment.getPaymentPurpose() != Payment.PaymentPurpose.BOOKING_CHARGE)
						.max(Comparator.comparing(Payment::getPaymentDate)).orElse(null);
		Payment bookingChargePayment = inv.getPayments() == null ? null
				: inv.getPayments().stream()
						.filter(payment -> payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
						.filter(payment -> payment.getPaymentPurpose() == Payment.PaymentPurpose.BOOKING_CHARGE)
						.max(Comparator.comparing(Payment::getPaymentDate)).orElse(null);
		if (latestPayment != null) {
			dto.paymentMethod = latestPayment.getPaymentMethod() != null ? latestPayment.getPaymentMethod().name()
					: null;
			dto.transactionReference = latestPayment.getTransactionReference();
			dto.paidAt = latestPayment.getPaymentDate();
		}
		if (bookingChargePayment != null) {
			dto.bookingChargePaymentMethod = bookingChargePayment.getPaymentMethod() != null
					? bookingChargePayment.getPaymentMethod().name()
					: null;
			dto.bookingChargeTransactionReference = bookingChargePayment.getTransactionReference();
			dto.bookingChargePaidAt = bookingChargePayment.getPaymentDate();
			if (dto.paymentMethod == null) {
				dto.paymentMethod = dto.bookingChargePaymentMethod;
				dto.transactionReference = dto.bookingChargeTransactionReference;
				dto.paidAt = dto.bookingChargePaidAt;
			}
		}
		dto.bookingChargePaidAmount = inv.getPayments() == null ? BigDecimal.ZERO
				: inv.getPayments().stream()
						.filter(payment -> payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
						.filter(payment -> payment.getPaymentPurpose() == Payment.PaymentPurpose.BOOKING_CHARGE)
						.map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		dto.finalInvoicePaidAmount = inv.getPayments() == null ? BigDecimal.ZERO
				: inv.getPayments().stream()
						.filter(payment -> payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
						.filter(payment -> payment.getPaymentPurpose() == Payment.PaymentPurpose.FINAL_INVOICE)
						.map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		dto.totalPaidAmount = dto.bookingChargePaidAmount.add(dto.finalInvoicePaidAmount);

		if (inv.getServiceRecord() != null) {
			dto.serviceId = inv.getServiceRecord().getServiceId();
			dto.serviceStatus = inv.getServiceRecord().getStatus() != null ? inv.getServiceRecord().getStatus().name()
					: null;
			dto.items = inv.getServiceRecord().getItems() == null ? List.of()
					: inv.getServiceRecord().getItems().stream().map(InvoiceLineItemDto::from).toList();

			if (inv.getServiceRecord().getBooking() != null) {
				var booking = inv.getServiceRecord().getBooking();
				dto.bookingId = booking.getBookingId();
				dto.bookingStatus = booking.getBookingStatus() != null ? booking.getBookingStatus().name() : null;
				if (booking.getVehicle() != null) {
					dto.vehicleInfo = booking.getVehicle().getBrand() + " " + booking.getVehicle().getModel() + " ("
							+ booking.getVehicle().getRegistrationNumber() + ")";
					if (booking.getVehicle().getUser() != null) {
						dto.customerName = booking.getVehicle().getUser().getFullName();
					}
				}
				if (booking.getCatalog() != null) {
					dto.serviceName = booking.getCatalog().getServiceName();
				}
			}
		}
		if (dto.items == null) {
			dto.items = List.of();
		}
		return dto;
	}

	public Integer getInvoiceId() {
		return invoiceId;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public Integer getBookingId() {
		return bookingId;
	}

	public String getServiceStatus() {
		return serviceStatus;
	}

	public String getBookingStatus() {
		return bookingStatus;
	}

	public String getVehicleInfo() {
		return vehicleInfo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public BigDecimal getBaseServicePrice() {
		return baseServicePrice;
	}

	public BigDecimal getBookingCharge() {
		return bookingCharge;
	}

	public BigDecimal getItemsTotal() {
		return itemsTotal;
	}

	public BigDecimal getOvertimeCharge() {
		return overtimeCharge;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public BigDecimal getAdvanceAmount() {
		return advanceAmount;
	}

	public Boolean getAdvancePaid() {
		return advancePaid;
	}

	public BigDecimal getRemainingBalance() {
		return remainingBalance;
	}

	public BigDecimal getNetAmountAfterAdvance() {
		return netAmountAfterAdvance;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public String getBookingChargePaymentMethod() {
		return bookingChargePaymentMethod;
	}

	public String getBookingChargeTransactionReference() {
		return bookingChargeTransactionReference;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public LocalDateTime getBookingChargePaidAt() {
		return bookingChargePaidAt;
	}

	public BigDecimal getBookingChargePaidAmount() {
		return bookingChargePaidAmount;
	}

	public BigDecimal getFinalInvoicePaidAmount() {
		return finalInvoicePaidAmount;
	}

	public BigDecimal getTotalPaidAmount() {
		return totalPaidAmount;
	}

	public List<InvoiceLineItemDto> getItems() {
		return items;
	}

	public static class InvoiceLineItemDto {
		private Integer itemId;
		private String itemName;
		private Integer quantity;
		private BigDecimal unitPrice;
		private BigDecimal total;

		public static InvoiceLineItemDto from(ServiceItem item) {
			InvoiceLineItemDto dto = new InvoiceLineItemDto();
			dto.itemId = item.getItemId();
			dto.itemName = item.getWorkItem() != null ? item.getWorkItem().getItemName() : "Item";
			dto.quantity = item.getQuantity();
			dto.unitPrice = item.getUnitPrice();
			dto.total = item.getTotalPrice();
			return dto;
		}

		public Integer getItemId() {
			return itemId;
		}

		public String getItemName() {
			return itemName;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public BigDecimal getUnitPrice() {
			return unitPrice;
		}

		public BigDecimal getTotal() {
			return total;
		}
	}
}
