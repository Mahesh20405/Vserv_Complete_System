package com.vserv.features.servicerecord.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ServiceItemRequest {
	@NotNull(message = "Work item is required")
	private Integer workItemId;

	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be greater than 0")
	@Max(value = 99, message = "Quantity must be at most 99")
	private Integer quantity;

	@NotNull(message = "Unit price is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be 0 or greater")
	private BigDecimal unitPrice;

	public Integer getWorkItemId() {
		return workItemId;
	}

	public void setWorkItemId(Integer workItemId) {
		this.workItemId = workItemId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}
}
