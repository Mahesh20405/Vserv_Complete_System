package com.vserv.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "invoice")
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "invoice_id")
	private Integer invoiceId;

	@OneToOne
	@JoinColumn(name = "service_id", unique = true, nullable = false)
	private ServiceRecord serviceRecord;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "items_total", precision = 10, scale = 2)
	private BigDecimal itemsTotal;

	@Column(name = "overtime_charge", precision = 10, scale = 2)
	private BigDecimal overtimeCharge;

	@Column(name = "base_service_price", precision = 10, scale = 2)
	private BigDecimal baseServicePrice = BigDecimal.ZERO;

	@Column(name = "booking_charge", precision = 10, scale = 2)
	private BigDecimal bookingCharge = BigDecimal.ZERO;

	@Column(name = "advance_paid")
	private Boolean advancePaid = false;

	@Column(name = "advance_amount", precision = 10, scale = 2)
	private BigDecimal advanceAmount = BigDecimal.ZERO;

	@Column(name = "invoice_date", nullable = false)
	private LocalDate invoiceDate = LocalDate.now();

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status")
	private PaymentStatus paymentStatus = PaymentStatus.PENDING;

	@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
	private List<Payment> payments;

	public Invoice() {
	}

	public Integer getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Integer invoiceId) {
		this.invoiceId = invoiceId;
	}

	public ServiceRecord getServiceRecord() {
		return serviceRecord;
	}

	public void setServiceRecord(ServiceRecord serviceRecord) {
		this.serviceRecord = serviceRecord;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getItemsTotal() {
		return itemsTotal;
	}

	public void setItemsTotal(BigDecimal itemsTotal) {
		this.itemsTotal = itemsTotal;
	}

	public BigDecimal getOvertimeCharge() {
		return overtimeCharge;
	}

	public void setOvertimeCharge(BigDecimal overtimeCharge) {
		this.overtimeCharge = overtimeCharge;
	}

	public BigDecimal getBaseServicePrice() {
		return baseServicePrice;
	}

	public void setBaseServicePrice(BigDecimal baseServicePrice) {
		this.baseServicePrice = baseServicePrice;
	}

	public BigDecimal getBookingCharge() {
		return bookingCharge;
	}

	public void setBookingCharge(BigDecimal bookingCharge) {
		this.bookingCharge = bookingCharge;
	}

	public Boolean getAdvancePaid() {
		return advancePaid;
	}

	public void setAdvancePaid(Boolean advancePaid) {
		this.advancePaid = advancePaid;
	}

	public BigDecimal getAdvanceAmount() {
		return advanceAmount;
	}

	public void setAdvanceAmount(BigDecimal advanceAmount) {
		this.advanceAmount = advanceAmount;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

	public enum PaymentStatus {
		PENDING, PAID, PARTIALLY_PAID
	}
}
