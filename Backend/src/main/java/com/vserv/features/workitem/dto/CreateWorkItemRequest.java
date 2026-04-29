package com.vserv.features.workitem.dto;

import com.vserv.entity.WorkItemCatalog;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateWorkItemRequest {
	@NotBlank(message = "Item name is required")
	@Size(max = 100, message = "Item name must be at most 100 characters")
	private String itemName;

	@NotNull(message = "Item type is required")
	private WorkItemCatalog.ItemType itemType;

	@NotNull(message = "Car type is required")
	private WorkItemCatalog.ItemCarType carType;

	@NotNull(message = "Unit price is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be 0 or greater")
	private BigDecimal unitPrice;

	@Size(max = 500, message = "Description must be at most 500 characters")
	private String description;

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public WorkItemCatalog.ItemType getItemType() {
		return itemType;
	}

	public void setItemType(WorkItemCatalog.ItemType itemType) {
		this.itemType = itemType;
	}

	public WorkItemCatalog.ItemCarType getCarType() {
		return carType;
	}

	public void setCarType(WorkItemCatalog.ItemCarType carType) {
		this.carType = carType;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
