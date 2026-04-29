package com.vserv.features.paymentgateway.dto;

public class CheckoutOrderResponse {
	private final String keyId;
	private final String orderId;
	private final int amount;
	private final String currency;
	private final String name;
	private final String description;

	public CheckoutOrderResponse(String keyId, String orderId, int amount, String currency, String name,
			String description) {
		this.keyId = keyId;
		this.orderId = orderId;
		this.amount = amount;
		this.currency = currency;
		this.name = name;
		this.description = description;
	}

	public String getKeyId() {
		return keyId;
	}

	public String getOrderId() {
		return orderId;
	}

	public int getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
