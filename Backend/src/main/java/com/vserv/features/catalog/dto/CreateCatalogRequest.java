package com.vserv.features.catalog.dto;

import com.vserv.entity.ServiceCatalog;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateCatalogRequest {
	@NotBlank(message = "Service name is required")
	@Size(max = 100, message = "Service name must be at most 100 characters")
	private String serviceName;

	@NotNull(message = "Service type is required")
	private ServiceCatalog.ServiceType serviceType;

	@Size(max = 2000, message = "Description must be at most 2000 characters")
	private String description;

	@NotNull(message = "Base price is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Base price must be 0 or greater")
	private BigDecimal basePrice;

	@NotNull(message = "Car type is required")
	private ServiceCatalog.CatalogCarType carType;

	@DecimalMin(value = "0.0", inclusive = true, message = "Duration hours must be 0 or greater")
	private BigDecimal durationHours;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public ServiceCatalog.ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceCatalog.ServiceType serviceType) {
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

	public ServiceCatalog.CatalogCarType getCarType() {
		return carType;
	}

	public void setCarType(ServiceCatalog.CatalogCarType carType) {
		this.carType = carType;
	}

	public BigDecimal getDurationHours() {
		return durationHours;
	}

	public void setDurationHours(BigDecimal durationHours) {
		this.durationHours = durationHours;
	}
}
