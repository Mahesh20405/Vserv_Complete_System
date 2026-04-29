package com.vserv.features.paymentgateway.service.impl;

import com.vserv.core.exception.BusinessException;
import com.vserv.core.exception.ConflictException;
import com.vserv.entity.Payment;
import com.vserv.features.paymentgateway.service.VerifiedGatewayPaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryVerifiedGatewayPaymentService implements VerifiedGatewayPaymentService {
	private static final long TTL_SECONDS = 30 * 60;
	private final Map<String, VerifiedPayment> verifiedPayments = new ConcurrentHashMap<>();

	@Override
	public void storeVerifiedBookingCharge(String paymentId, String orderId, Payment.PaymentMethod paymentMethod,
			BigDecimal amount) {
		purgeExpired();
		verifiedPayments.put(paymentId,
				new VerifiedPayment(orderId, paymentMethod, amount, Instant.now().plusSeconds(TTL_SECONDS)));
	}

	@Override
	public void consumeVerifiedBookingCharge(String paymentId, Payment.PaymentMethod paymentMethod, BigDecimal amount) {
		purgeExpired();
		VerifiedPayment verifiedPayment = verifiedPayments.remove(paymentId);
		if (verifiedPayment == null) {
			throw new ConflictException("Booking payment verification was not found or has expired. Please pay again.");
		}
		if (verifiedPayment.paymentMethod() != paymentMethod) {
			throw new BusinessException("Booking payment method does not match the verified Razorpay payment.");
		}
		if (verifiedPayment.amount() == null || amount == null || verifiedPayment.amount().compareTo(amount) != 0) {
			throw new BusinessException("Booking payment amount does not match the verified Razorpay payment.");
		}
	}

	private void purgeExpired() {
		Instant now = Instant.now();
		verifiedPayments.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
	}

	private record VerifiedPayment(String orderId, Payment.PaymentMethod paymentMethod, BigDecimal amount,
			Instant expiresAt) {
	}
}
