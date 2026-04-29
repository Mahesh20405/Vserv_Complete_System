package com.vserv.features.vehicle.dto;

import com.vserv.core.util.ValidationPatterns;
import com.vserv.entity.Vehicle;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateVehicleRequest {
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
	@Min(value = 0, message = "Mileage must be 0 or greater")
	private Integer mileage;

	@Min(value = 1000, message = "Service interval must be at least 1000 km")
	private Integer serviceIntervalKm;

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public Integer getManufactureYear() {
		return manufactureYear;
	}

	public void setManufactureYear(Integer manufactureYear) {
		this.manufactureYear = manufactureYear;
	}

	public Vehicle.CarType getCarType() {
		return carType;
	}

	public void setCarType(Vehicle.CarType carType) {
		this.carType = carType;
	}

	public Integer getMileage() {
		return mileage;
	}

	public void setMileage(Integer mileage) {
		this.mileage = mileage;
	}

	public Integer getServiceIntervalKm() {
		return serviceIntervalKm;
	}

	public void setServiceIntervalKm(Integer serviceIntervalKm) {
		this.serviceIntervalKm = serviceIntervalKm;
	}
}
