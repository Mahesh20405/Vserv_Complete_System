package com.vserv.features.paymentgateway.dto;

import com.vserv.entity.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CheckoutOrderRequest {
	@NotNull(message = "Payment method is required")
	private Payment.PaymentMethod paymentMethod;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
	private BigDecimal amount;

	private String description;

	public Payment.PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(Payment.PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
