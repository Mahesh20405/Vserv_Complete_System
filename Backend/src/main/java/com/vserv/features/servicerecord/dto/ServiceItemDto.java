package com.vserv.features.servicerecord.dto;

import com.vserv.entity.ServiceItem;

import java.math.BigDecimal;
import java.util.Map;

public class ServiceItemDto {
	private Integer itemId;
	private Integer workItemId;
	private String itemName;
	private String itemType;
	private BigDecimal unitPrice;
	private Integer quantity;
	private BigDecimal totalPrice;
	private Map<String, Object> workItem;

	public static ServiceItemDto from(ServiceItem item) {
		ServiceItemDto dto = new ServiceItemDto();
		dto.itemId = item.getItemId();
		dto.unitPrice = item.getUnitPrice();
		dto.quantity = item.getQuantity();
		dto.totalPrice = item.getTotalPrice();

		if (item.getWorkItem() != null) {
			dto.workItemId = item.getWorkItem().getWorkItemId();
			dto.itemName = item.getWorkItem().getItemName();
			dto.itemType = item.getWorkItem().getItemType() != null ? item.getWorkItem().getItemType().name() : null;
			dto.workItem = Map.of("workItemId", dto.workItemId, "itemName", dto.itemName != null ? dto.itemName : "",
					"itemType", dto.itemType != null ? dto.itemType : "");
		}
		return dto;
	}

	public Integer getItemId() {
		return itemId;
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

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public Map<String, Object> getWorkItem() {
		return workItem;
	}
}
