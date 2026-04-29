package com.vserv.features.vehicle.service;

import com.vserv.entity.AppUser;
import com.vserv.entity.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleService {
	List<Vehicle> findByUser(AppUser user);

	List<Vehicle> findAll();

	long countActiveVehicles();

	Optional<Vehicle> findById(Integer id);

	boolean regExists(String reg);

	boolean regExistsForOther(String reg, Integer id);

	List<Vehicle> search(String q);

	Vehicle addVehicle(AppUser user, String brand, String model, String reg, Integer year, Vehicle.CarType carType,
			Integer mileage, Integer intervalKm);

	Vehicle updateVehicle(Integer vehicleId, String brand, String model, String reg, Integer year,
			Vehicle.CarType carType, Integer mileage, Integer intervalKm);

	Vehicle toggleStatus(Integer vehicleId);
}
