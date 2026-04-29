package com.vserv.features.vehicle.dto;

import com.vserv.entity.Vehicle;
import java.time.LocalDate;

public class VehicleDto {
	private Integer vehicleId;
	private Integer ownerId;
	private String ownerName;
	private String brand;
	private String model;
	private String registrationNumber;
	private Integer manufactureYear;
	private String carType;
	private Integer mileage;
	private Integer serviceIntervalKm;
	private LocalDate lastServiceDate;
	private LocalDate nextServiceDue;
	private Boolean isActive;
	private Boolean canToggleStatus;
	private String statusToggleReason;

	public static VehicleDto from(Vehicle v) {
		VehicleDto dto = new VehicleDto();
		dto.vehicleId = v.getVehicleId();
		dto.brand = v.getBrand();
		dto.model = v.getModel();
		dto.registrationNumber = v.getRegistrationNumber();
		dto.manufactureYear = v.getManufactureYear();
		dto.carType = v.getCarType() != null ? v.getCarType().name() : null;
		dto.mileage = v.getMileage();
		dto.serviceIntervalKm = v.getServiceIntervalKm();
		dto.lastServiceDate = v.getLastServiceDate();
		dto.nextServiceDue = v.getNextServiceDue();
		dto.isActive = v.getIsActive();
		if (v.getUser() != null) {
			dto.ownerId = v.getUser().getUserId();
			dto.ownerName = v.getUser().getFullName();
		}
		return dto;
	}

	public Integer getVehicleId() {
		return vehicleId;
	}

	public Integer getOwnerId() {
		return ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public String getBrand() {
		return brand;
	}

	public String getModel() {
		return model;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public Integer getManufactureYear() {
		return manufactureYear;
	}

	public String getCarType() {
		return carType;
	}

	public Integer getMileage() {
		return mileage;
	}

	public Integer getServiceIntervalKm() {
		return serviceIntervalKm;
	}

	public LocalDate getLastServiceDate() {
		return lastServiceDate;
	}

	public LocalDate getNextServiceDue() {
		return nextServiceDue;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public Boolean getCanToggleStatus() {
		return canToggleStatus;
	}

	public void setCanToggleStatus(Boolean canToggleStatus) {
		this.canToggleStatus = canToggleStatus;
	}

	public String getStatusToggleReason() {
		return statusToggleReason;
	}

	public void setStatusToggleReason(String statusToggleReason) {
		this.statusToggleReason = statusToggleReason;
	}
}
