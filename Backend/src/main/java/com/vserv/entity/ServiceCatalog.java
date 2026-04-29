package com.vserv.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "service_catalog")
public class ServiceCatalog {
	public static final BigDecimal DEFAULT_BOOKING_CHARGE = new BigDecimal("299.00");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "catalog_id")
	private Integer catalogId;

	@Column(name = "service_name", nullable = false, length = 100)
	private String serviceName;

	@Enumerated(EnumType.STRING)
	@Column(name = "service_type", nullable = false)
	private ServiceType serviceType;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "base_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal basePrice;

	@Column(name = "booking_charge", nullable = false, precision = 10, scale = 2)
	private BigDecimal bookingCharge = DEFAULT_BOOKING_CHARGE;

	@Enumerated(EnumType.STRING)
	@Column(name = "car_type")
	private CatalogCarType carType = CatalogCarType.ALL;

	@Column(name = "duration_hours", precision = 4, scale = 2)
	private BigDecimal durationHours = new BigDecimal("2.0");

	@Column(name = "is_active")
	private Boolean isActive = true;

	public ServiceCatalog() {
	}

	public Integer getCatalogId() {
		return catalogId;
	}

	public void setCatalogId(Integer catalogId) {
		this.catalogId = catalogId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}

	public BigDecimal getBookingCharge() {
		return bookingCharge;
	}

	public void setBookingCharge(BigDecimal bookingCharge) {
		this.bookingCharge = bookingCharge;
	}

	public CatalogCarType getCarType() {
		return carType;
	}

	public void setCarType(CatalogCarType carType) {
		this.carType = carType;
	}

	public BigDecimal getDurationHours() {
		return durationHours;
	}

	public void setDurationHours(BigDecimal durationHours) {
		this.durationHours = durationHours;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public enum ServiceType {
		SERVICING, REPAIR, INSPECTION, MAINTENANCE
	}

	public enum CatalogCarType {
		SEDAN, SUV, HATCHBACK, COUPE, CONVERTIBLE, WAGON, MINIVAN, ALL
	}
}
