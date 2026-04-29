package com.vserv.features.vehicle.service.impl;

import com.vserv.core.status.StatusToggleGuard;
import com.vserv.core.status.StatusToggleGuardService;
import com.vserv.features.vehicle.repository.VehicleRepository;

import com.vserv.features.vehicle.service.VehicleService;

import com.vserv.entity.Vehicle;
import com.vserv.entity.AppUser;
import com.vserv.core.exception.BusinessException;
import com.vserv.entity.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements VehicleService {
	private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);

	private final VehicleRepository vehicleRepo;
	private final StatusToggleGuardService statusToggleGuardService;

	public VehicleServiceImpl(VehicleRepository vehicleRepo, StatusToggleGuardService statusToggleGuardService) {
		this.vehicleRepo = vehicleRepo;
		this.statusToggleGuardService = statusToggleGuardService;
	}

	public List<Vehicle> findByUser(AppUser user) {
		return vehicleRepo.findByUserIdAndIsDeletedFalse(user.getUserId());
	}

	public List<Vehicle> findAll() {
		return vehicleRepo.findByIsDeletedFalse();
	}

	public long countActiveVehicles() {
		return vehicleRepo.countByIsDeletedFalseAndIsActiveTrue();
	}

	public Optional<Vehicle> findById(Integer id) {
		return vehicleRepo.findByVehicleIdAndIsDeletedFalse(id);
	}

	public boolean regExists(String reg) {
		return vehicleRepo.existsByRegistrationNumber(reg);
	}

	public boolean regExistsForOther(String reg, Integer id) {
		return vehicleRepo.existsByRegistrationNumberAndVehicleIdNot(reg, id);
	}

	public List<Vehicle> search(String q) {
		return vehicleRepo.search(q);
	}

	private String normalizeRegistration(String reg) {
		return reg == null ? null : reg.toUpperCase().replaceAll("\\s+", "").trim();
	}

	private void validateVehicleFields(AppUser user, String brand, String model, String reg, Integer year,
			Vehicle.CarType carType, Integer mileage, Integer intervalKm) {
		if (user == null || user.getRole() == null || user.getRole().getRoleName() != Role.RoleName.CUSTOMER) {
			throw new BusinessException("Vehicles can only be assigned to customers.");
		}
		if (brand == null || brand.trim().isEmpty())
			throw new BusinessException("Brand cannot be empty.");
		if (model == null || model.trim().isEmpty())
			throw new BusinessException("Model cannot be empty.");
		String normalizedReg = normalizeRegistration(reg);
		if (normalizedReg == null || normalizedReg.isEmpty())
			throw new BusinessException("Registration number cannot be empty.");
		log.warn("Invalid registration number format: {}", reg);
		if (!normalizedReg.matches("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$")) {
			throw new BusinessException("Registration number format is invalid.");
		}
		int currentYear = java.time.LocalDate.now().getYear();
		if (year == null || year < 1900 || year > currentYear) {
			throw new BusinessException("Manufacture year must be between 1900 and " + currentYear + ".");
		}
		if (carType == null)
			throw new BusinessException("Car type is required.");
		if (mileage == null || mileage < 0)
			throw new BusinessException("Mileage must be 0 or greater.");
		if (intervalKm != null && intervalKm < 1000) {
			throw new BusinessException("Service interval must be at least 1000 km.");
		}
	}

	@Transactional
	public Vehicle addVehicle(AppUser user, String brand, String model, String reg, Integer year,
			Vehicle.CarType carType, Integer mileage, Integer intervalKm) {
		validateVehicleFields(user, brand, model, reg, year, carType, mileage, intervalKm);
		Vehicle v = new Vehicle();
		v.setUser(user);
		v.setBrand(brand);
		v.setModel(model);
		v.setRegistrationNumber(normalizeRegistration(reg));
		v.setManufactureYear(year);
		v.setCarType(carType);
		v.setMileage(mileage);
		v.setServiceIntervalKm(intervalKm != null ? intervalKm : 10000);
		log.info("Vehicle added reg={} userId={}", v.getRegistrationNumber(), user.getUserId());
		return vehicleRepo.save(v);
	}

	@Transactional
	public Vehicle updateVehicle(Integer vehicleId, String brand, String model, String reg, Integer year,
			Vehicle.CarType carType, Integer mileage, Integer intervalKm) {
		Vehicle v = requireActiveVehicle(vehicleId);
		validateVehicleFields(v.getUser(), v.getBrand(), v.getModel(), v.getRegistrationNumber(),
				v.getManufactureYear(), v.getCarType(), mileage, intervalKm);
		v.setMileage(mileage);
		v.setServiceIntervalKm(intervalKm != null ? intervalKm : v.getServiceIntervalKm());
		log.info("Vehicle updated vehicleId={}", vehicleId);
		return vehicleRepo.save(v);
	}

	@Transactional
	public Vehicle toggleStatus(Integer vehicleId) {
		log.info("Toggling active status for vehicleId={}", vehicleId);
		Vehicle v = requireActiveVehicle(vehicleId);
		if (Boolean.TRUE.equals(v.getIsActive())) {
			StatusToggleGuard guard = statusToggleGuardService.evaluateVehicleToggle(v);
			if (!guard.isAllowed()) {
				throw new BusinessException(guard.getReason());
			}
			v.setIsActive(false);
		} else {
			v.setIsActive(true);
		}
		return vehicleRepo.save(v);
	}

	private Vehicle requireActiveVehicle(Integer vehicleId) {
		return vehicleRepo.findByVehicleIdAndIsDeletedFalse(vehicleId)
				.orElseThrow(() -> new BusinessException("Vehicle not found."));
	}
}
