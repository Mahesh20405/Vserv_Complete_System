package com.vserv.features.paymentgateway.controller;

import com.vserv.core.exception.ForbiddenException;
import com.vserv.core.util.SecurityUtils;
import com.vserv.entity.Payment;
import com.vserv.features.paymentgateway.dto.CheckoutOrderRequest;
import com.vserv.features.paymentgateway.dto.CheckoutOrderResponse;
import com.vserv.features.paymentgateway.dto.VerifyPaymentRequest;
import com.vserv.features.paymentgateway.service.RazorpayGatewayService;
import com.vserv.features.paymentgateway.service.VerifiedGatewayPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentGatewayController {
	private final RazorpayGatewayService razorpayGatewayService;
	private final VerifiedGatewayPaymentService verifiedGatewayPaymentService;
	private final SecurityUtils securityUtils;

	public PaymentGatewayController(RazorpayGatewayService razorpayGatewayService,
			VerifiedGatewayPaymentService verifiedGatewayPaymentService, SecurityUtils securityUtils) {
		this.razorpayGatewayService = razorpayGatewayService;
		this.verifiedGatewayPaymentService = verifiedGatewayPaymentService;
		this.securityUtils = securityUtils;
	}

	@PostMapping("/booking-charge/order")
	public ResponseEntity<CheckoutOrderResponse> createBookingChargeOrder(
			@Valid @RequestBody CheckoutOrderRequest req) {
		var me = securityUtils.requireCurrentUser();
		if (!securityUtils.isCustomer() && !securityUtils.isAdmin()) {
			throw new ForbiddenException("Only admins or customers can create booking charge payments.");
		}
		if (req.getPaymentMethod() == Payment.PaymentMethod.CASH) {
			throw new ForbiddenException("Booking charge payments must use a digital payment method.");
		}
		String description = req.getDescription() != null && !req.getDescription().isBlank()
				? req.getDescription().trim()
				: "Booking Charge";
		String receipt = "booking-charge-" + me.getUserId() + "-" + Instant.now().toEpochMilli();
		return ResponseEntity.status(HttpStatus.OK).body(razorpayGatewayService.createOrder(req.getAmount(),
				description, receipt, Map.of("purpose", "BOOKING_CHARGE", "userId", String.valueOf(me.getUserId()))));
	}

	@PostMapping("/booking-charge/verify")
	public ResponseEntity<Map<String, String>> verifyBookingCharge(@Valid @RequestBody VerifyPaymentRequest req) {
		securityUtils.requireCurrentUser();
		if (req.getPaymentMethod() == Payment.PaymentMethod.CASH) {
			throw new ForbiddenException("Booking charge payments must use a digital payment method.");
		}
		razorpayGatewayService.verifySignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(),
				req.getRazorpaySignature());
		verifiedGatewayPaymentService.storeVerifiedBookingCharge(req.getRazorpayPaymentId(), req.getRazorpayOrderId(),
				req.getPaymentMethod(), req.getAmount());
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message",
				"Booking charge payment verified successfully.", "transactionReference", req.getRazorpayPaymentId()));
	}
}
