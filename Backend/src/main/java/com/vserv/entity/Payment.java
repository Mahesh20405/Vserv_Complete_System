package com.vserv.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private Integer paymentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invoice_id", nullable = false)
	@JsonIgnore
	private Invoice invoice;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false)
	private PaymentMethod paymentMethod;

	@Column(name = "transaction_reference", length = 100)
	private String transactionReference;

	@Column(name = "gateway_provider", length = 50)
	private String gatewayProvider;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "payment_date")
	private LocalDateTime paymentDate = LocalDateTime.now();

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status")
	private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_purpose")
	private PaymentPurpose paymentPurpose = PaymentPurpose.FINAL_INVOICE;

	public Payment() {
	}

	public Integer getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(Integer paymentId) {
		this.paymentId = paymentId;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(String transactionReference) {
		this.transactionReference = transactionReference;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getGatewayProvider() {
		return gatewayProvider;
	}

	public void setGatewayProvider(String gatewayProvider) {
		this.gatewayProvider = gatewayProvider;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public PaymentPurpose getPaymentPurpose() {
		return paymentPurpose;
	}

	public void setPaymentPurpose(PaymentPurpose paymentPurpose) {
		this.paymentPurpose = paymentPurpose;
	}

	public enum PaymentMethod {
		UPI, CARD, NET_BANKING, CASH
	}

	public enum PaymentStatus {
		SUCCESS, FAILED, PENDING
	}

	public enum PaymentPurpose {
		BOOKING_CHARGE, FINAL_INVOICE
	}
}
