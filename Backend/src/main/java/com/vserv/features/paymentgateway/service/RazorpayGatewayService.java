package com.vserv.features.paymentgateway.service;

import com.vserv.features.paymentgateway.dto.CheckoutOrderResponse;

import java.math.BigDecimal;
import java.util.Map;

public interface RazorpayGatewayService {
	CheckoutOrderResponse createOrder(BigDecimal amount, String description, String receipt, Map<String, String> notes);

	void verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
}
