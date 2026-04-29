package com.vserv.features.servicerecord.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SaveItemsRequest {
	@Valid
	@NotNull(message = "Items list is required")
	private List<ServiceItemRequest> items;

	public List<ServiceItemRequest> getItems() {
		return items;
	}

	public void setItems(List<ServiceItemRequest> v) {
		this.items = v;
	}
}
