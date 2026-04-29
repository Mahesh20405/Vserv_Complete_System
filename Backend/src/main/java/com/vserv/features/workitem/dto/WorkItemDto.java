package com.vserv.features.workitem.dto;

import com.vserv.entity.WorkItemCatalog;
import java.math.BigDecimal;

public class WorkItemDto {
	private Integer workItemId;
	private String itemName;
	private String itemType;
	private String carType;
	private BigDecimal unitPrice;
	private String description;
	private Boolean isActive;

	public static WorkItemDto from(WorkItemCatalog w) {
		WorkItemDto dto = new WorkItemDto();
		dto.workItemId = w.getWorkItemId();
		dto.itemName = w.getItemName();
		dto.itemType = w.getItemType() != null ? w.getItemType().name() : null;
		dto.carType = w.getCarType() != null ? w.getCarType().name() : null;
		dto.unitPrice = w.getUnitPrice();
		dto.description = w.getDescription();
		dto.isActive = w.getIsActive();
		return dto;
	}

	public Integer getWorkItemId() {
		return workItemId;
	}

	public String getItemName() {
		return itemName;
	}

	public String getItemType() {
		return itemType;
	}

	public String getCarType() {
		return carType;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getIsActive() {
		return isActive;
	}
}
