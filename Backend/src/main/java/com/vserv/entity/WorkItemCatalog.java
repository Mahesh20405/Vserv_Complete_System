package com.vserv.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "work_item_catalog")
public class WorkItemCatalog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "work_item_id")
	private Integer workItemId;

	@Column(name = "item_name", nullable = false, length = 100)
	private String itemName;

	@Enumerated(EnumType.STRING)
	@Column(name = "item_type", nullable = false)
	private ItemType itemType;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Enumerated(EnumType.STRING)
	@Column(name = "car_type")
	private ItemCarType carType = ItemCarType.ALL;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "is_active")
	private Boolean isActive = true;

	public WorkItemCatalog() {
	}

	public Integer getWorkItemId() {
		return workItemId;
	}

	public void setWorkItemId(Integer workItemId) {
		this.workItemId = workItemId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public ItemCarType getCarType() {
		return carType;
	}

	public void setCarType(ItemCarType carType) {
		this.carType = carType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public enum ItemType {
		PART, LABOR, CONSUMABLE
	}

	public enum ItemCarType {
		SEDAN, SUV, HATCHBACK, COUPE, CONVERTIBLE, WAGON, MINIVAN, ALL
	}
}
