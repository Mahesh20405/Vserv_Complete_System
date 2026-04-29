package com.vserv.features.paymentgateway.dto;

import com.vserv.entity.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class VerifyPaymentRequest {
	@NotNull(message = "Payment method is required")
	private Payment.PaymentMethod paymentMethod;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
	private BigDecimal amount;

	@NotBlank(message = "Razorpay order id is required")
	private String razorpayOrderId;

	@NotBlank(message = "Razorpay payment id is required")
	private String razorpayPaymentId;

	@NotBlank(message = "Razorpay signature is required")
	private String razorpaySignature;

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

	public String getRazorpayOrderId() {
		return razorpayOrderId;
	}

	public void setRazorpayOrderId(String razorpayOrderId) {
		this.razorpayOrderId = razorpayOrderId;
	}

	public String getRazorpayPaymentId() {
		return razorpayPaymentId;
	}

	public void setRazorpayPaymentId(String razorpayPaymentId) {
		this.razorpayPaymentId = razorpayPaymentId;
	}

	public String getRazorpaySignature() {
		return razorpaySignature;
	}

	public void setRazorpaySignature(String razorpaySignature) {
		this.razorpaySignature = razorpaySignature;
	}
}
