package com.vserv.features.paymentgateway.service;

import com.vserv.entity.Payment;

import java.math.BigDecimal;

public interface VerifiedGatewayPaymentService {
	void storeVerifiedBookingCharge(String paymentId, String orderId, Payment.PaymentMethod paymentMethod,
			BigDecimal amount);

	void consumeVerifiedBookingCharge(String paymentId, Payment.PaymentMethod paymentMethod, BigDecimal amount);
}
