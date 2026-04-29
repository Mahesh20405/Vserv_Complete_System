package com.vserv.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "service_item")
public class ServiceItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "item_id")
	private Integer itemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id", nullable = false)
	@JsonIgnore
	private ServiceRecord serviceRecord;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "work_item_id", nullable = false)
	private WorkItemCatalog workItem;

	private Integer quantity = 1;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "total_price", precision = 10, scale = 2, insertable = false, updatable = false)
	private BigDecimal totalPrice;

	public ServiceItem() {
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public ServiceRecord getServiceRecord() {
		return serviceRecord;
	}

	public void setServiceRecord(ServiceRecord serviceRecord) {
		this.serviceRecord = serviceRecord;
	}

	public WorkItemCatalog getWorkItem() {
		return workItem;
	}

	public void setWorkItem(WorkItemCatalog workItem) {
		this.workItem = workItem;
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

	public BigDecimal getTotalPrice() {
		if (totalPrice != null)
			return totalPrice;
		if (unitPrice == null || quantity == null)
			return BigDecimal.ZERO;
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}
}
