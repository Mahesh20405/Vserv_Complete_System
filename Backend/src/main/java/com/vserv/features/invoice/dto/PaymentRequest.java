package com.vserv.features.invoice.dto;

import com.vserv.core.util.ValidationPatterns;
import com.vserv.entity.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PaymentRequest {
	@NotNull(message = "Payment method is required")
	private Payment.PaymentMethod paymentMethod;
	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
	private BigDecimal amount;
	@Size(max = 100, message = "Transaction reference must be at most 100 characters")
	@Pattern(regexp = ValidationPatterns.TRANSACTION_REFERENCE, message = "Transaction reference contains unsupported characters")
	private String transactionReference;

	public Payment.PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(Payment.PaymentMethod v) {
		this.paymentMethod = v;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal v) {
		this.amount = v;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(String v) {
		this.transactionReference = (v == null || v.isBlank()) ? null : v.trim();
	}
}
