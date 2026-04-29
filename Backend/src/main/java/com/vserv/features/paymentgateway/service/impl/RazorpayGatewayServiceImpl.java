package com.vserv.features.paymentgateway.service.impl;

import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.vserv.core.config.RazorpayProperties;
import com.vserv.core.exception.BusinessException;
import com.vserv.features.paymentgateway.dto.CheckoutOrderResponse;
import com.vserv.features.paymentgateway.service.RazorpayGatewayService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class RazorpayGatewayServiceImpl implements RazorpayGatewayService {
	private static final Logger log = LoggerFactory.getLogger(RazorpayGatewayServiceImpl.class);
	private final RazorpayProperties properties;

	public RazorpayGatewayServiceImpl(RazorpayProperties properties) {
		this.properties = properties;
	}

	@Override
	public CheckoutOrderResponse createOrder(BigDecimal amount, String description, String receipt,
			Map<String, String> notes) {
		validateConfigured();
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException("Payment amount must be greater than 0.");
		}
		try {
			JSONObject request = new JSONObject();
			int amountInSubunits = amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP)
					.intValueExact();
			request.put("amount", amountInSubunits);
			request.put("currency", properties.getCurrency());
			request.put("receipt", receipt);
			request.put("payment_capture", 1);
			if (notes != null && !notes.isEmpty()) {
				request.put("notes", new JSONObject(notes));
			}

			RazorpayClient client = new RazorpayClient(properties.getKeyId(), properties.getKeySecret());
			com.razorpay.Order order = client.orders.create(request);
			return new CheckoutOrderResponse(properties.getKeyId(), order.get("id"), order.get("amount"),
					order.get("currency"), properties.getCompanyName(),
					StringUtils.hasText(description) ? description : "VServ Payment");
		} catch (Exception ex) {
			log.error("Razorpay order creation failed keyId={} currency={} receipt={} cause={} message={}",
					maskKeyId(properties.getKeyId()), properties.getCurrency(), receipt, ex.getClass().getName(),
					ex.getMessage(), ex);
			throw new BusinessException(resolveCreateOrderMessage(ex));
		}
	}

	@Override
	public void verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
		validateConfigured();
		try {
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", razorpayOrderId);
			attributes.put("razorpay_payment_id", razorpayPaymentId);
			attributes.put("razorpay_signature", razorpaySignature);
			if (!Utils.verifyPaymentSignature(attributes, properties.getKeySecret())) {
				throw new BusinessException("Unable to verify Razorpay payment signature.");
			}
		} catch (BusinessException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Razorpay signature verification failed orderId={} paymentId={} cause={} message={}",
					razorpayOrderId, razorpayPaymentId, ex.getClass().getName(), ex.getMessage(), ex);
			throw new BusinessException("Unable to verify Razorpay payment signature.");
		}
	}

	private void validateConfigured() {
		if (!properties.isEnabled() || !StringUtils.hasText(properties.getKeyId())
				|| !StringUtils.hasText(properties.getKeySecret())) {
			throw new BusinessException("Razorpay is not configured for this environment.");
		}
	}

	private String resolveCreateOrderMessage(Exception ex) {
		String message = ex.getMessage() == null ? "" : ex.getMessage().trim();
		String lowerMessage = message.toLowerCase();
		if (lowerMessage.contains("authentication") || lowerMessage.contains("api key")
				|| lowerMessage.contains("unauthorized")) {
			return "Razorpay authentication failed. Check the configured key id and key secret.";
		}
		if (lowerMessage.contains("unknownhost") || lowerMessage.contains("timed out")
				|| lowerMessage.contains("connection")) {
			return "Unable to reach Razorpay right now. Check internet access and firewall settings.";
		}
		return "Unable to create Razorpay order right now.";
	}

	private String maskKeyId(String keyId) {
		if (!StringUtils.hasText(keyId) || keyId.length() <= 6) {
			return keyId;
		}
		return keyId.substring(0, 6) + "..." + keyId.substring(keyId.length() - 4);
	}
}
