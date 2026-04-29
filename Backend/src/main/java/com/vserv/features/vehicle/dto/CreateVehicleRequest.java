package com.vserv.features.vehicle.dto;

import com.vserv.core.util.ValidationPatterns;
import com.vserv.entity.Vehicle;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public class CreateVehicleRequest {
	@NotNull(message = "User is required")
	private Integer userId;
	@NotBlank(message = "Brand is required")
	@Size(max = 50, message = "Brand must be at most 50 characters")
	private String brand;
	@NotBlank(message = "Model is required")
	@Size(max = 50, message = "Model must be at most 50 characters")
	private String model;
	@NotBlank(message = "Registration number is required")
	@Size(max = 50, message = "Registration number must be at most 50 characters")
	@Pattern(regexp = ValidationPatterns.REGISTRATION_NUMBER, message = "Registration number format is invalid")
	private String registrationNumber;
	@NotNull(message = "Manufacture year is required")
	@Min(value = 1900, message = "Manufacture year must be 1900 or later")
	@Max(value = 9999, message = "Manufacture year must be valid")
	private Integer manufactureYear;
	@NotNull(message = "Car type is required")
	private Vehicle.CarType carType;
	@NotNull(message = "Mileage is required")
	@PositiveOrZero(message = "Mileage must be 0 or greater")
	private Integer mileage;
	@Min(value = 1000, message = "Service interval must be at least 1000 km")
	private Integer serviceIntervalKm;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer v) {
		this.userId = v;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String v) {
		this.brand = v;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String v) {
		this.model = v;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String v) {
		this.registrationNumber = v;
	}

	public Integer getManufactureYear() {
		return manufactureYear;
	}

	public void setManufactureYear(Integer v) {
		this.manufactureYear = v;
	}

	public Vehicle.CarType getCarType() {
		return carType;
	}

	public void setCarType(Vehicle.CarType v) {
		this.carType = v;
	}

	public Integer getMileage() {
		return mileage;
	}

	public void setMileage(Integer v) {
		this.mileage = v;
	}

	public Integer getServiceIntervalKm() {
		return serviceIntervalKm;
	}

	public void setServiceIntervalKm(Integer v) {
		this.serviceIntervalKm = v;
	}
}
