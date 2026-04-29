package com.vserv.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle")
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vehicle_id")
	private Integer vehicleId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(name = "car_type", nullable = false)
	private CarType carType;

	@Column(length = 50)
	private String brand;

	@Column(length = 50)
	private String model;

	@Column(name = "registration_number", unique = true, nullable = false, length = 50)
	private String registrationNumber;

	@Column(name = "manufacture_year")
	private Integer manufactureYear;

	private Integer mileage;

	@Column(name = "last_service_date")
	private LocalDate lastServiceDate;

	@Column(name = "next_service_due")
	private LocalDate nextServiceDue;

	@Column(name = "service_interval_km")
	private Integer serviceIntervalKm = 10000;

	@Column(name = "is_active")
	private Boolean isActive = true;

	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public Vehicle() {
	}

	public Integer getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Integer vehicleId) {
		this.vehicleId = vehicleId;
	}

	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser user) {
		this.user = user;
	}

	public CarType getCarType() {
		return carType;
	}

	public void setCarType(CarType carType) {
		this.carType = carType;
	}

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

	public Integer getMileage() {
		return mileage;
	}

	public void setMileage(Integer mileage) {
		this.mileage = mileage;
	}

	public LocalDate getLastServiceDate() {
		return lastServiceDate;
	}

	public void setLastServiceDate(LocalDate lastServiceDate) {
		this.lastServiceDate = lastServiceDate;
	}

	public LocalDate getNextServiceDue() {
		return nextServiceDue;
	}

	public void setNextServiceDue(LocalDate nextServiceDue) {
		this.nextServiceDue = nextServiceDue;
	}

	public Integer getServiceIntervalKm() {
		return serviceIntervalKm;
	}

	public void setServiceIntervalKm(Integer serviceIntervalKm) {
		this.serviceIntervalKm = serviceIntervalKm;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public enum CarType {
		SEDAN, SUV, HATCHBACK, COUPE, CONVERTIBLE, WAGON, MINIVAN
	}
}
