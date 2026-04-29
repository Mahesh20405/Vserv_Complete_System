package com.vserv.features.catalog.dto;

import com.vserv.entity.ServiceCatalog;
import java.math.BigDecimal;

public class CatalogDto {
	private Integer catalogId;
	private String serviceName;
	private String serviceType;
	private String description;
	private BigDecimal basePrice;
	private BigDecimal bookingCharge;
	private String carType;
	private BigDecimal durationHours;
	private Boolean isActive;

	public static CatalogDto from(ServiceCatalog s) {
		CatalogDto dto = new CatalogDto();
		dto.catalogId = s.getCatalogId();
		dto.serviceName = s.getServiceName();
		dto.serviceType = s.getServiceType() != null ? s.getServiceType().name() : null;
		dto.description = s.getDescription();
		dto.basePrice = s.getBasePrice();
		dto.bookingCharge = ServiceCatalog.DEFAULT_BOOKING_CHARGE;
		dto.carType = s.getCarType() != null ? s.getCarType().name() : null;
		dto.durationHours = s.getDurationHours();
		dto.isActive = s.getIsActive();
		return dto;
	}

	public Integer getCatalogId() {
		return catalogId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getServiceType() {
		return serviceType;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public BigDecimal getBookingCharge() {
		return bookingCharge;
	}

	public String getCarType() {
		return carType;
	}

	public BigDecimal getDurationHours() {
		return durationHours;
	}

	public Boolean getIsActive() {
		return isActive;
	}
}
