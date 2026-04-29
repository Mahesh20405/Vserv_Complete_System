package com.vserv.features.servicerecord.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class CompleteServiceRequest {
	@NotNull(message = "Actual hours is required")
	@DecimalMin(value = "0.1", inclusive = true, message = "Actual hours must be at least 0.1")
	@DecimalMax(value = "24.0", inclusive = true, message = "Actual hours must be at most 24")
	private BigDecimal actualHours;
	@Size(max = 2000, message = "Remarks must be at most 2000 characters")
	private String remarks;
	@Valid
	private List<ServiceItemRequest> items;

	public BigDecimal getActualHours() {
		return actualHours;
	}

	public void setActualHours(BigDecimal v) {
		this.actualHours = v;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String v) {
		this.remarks = v;
	}

	public List<ServiceItemRequest> getItems() {
		return items;
	}

	public void setItems(List<ServiceItemRequest> v) {
		this.items = v;
	}
}
