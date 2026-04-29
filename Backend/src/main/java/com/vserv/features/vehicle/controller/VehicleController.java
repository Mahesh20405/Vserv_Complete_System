package com.vserv.features.vehicle.controller;

import com.vserv.core.status.StatusToggleGuard;
import com.vserv.core.status.StatusToggleGuardService;
import com.vserv.features.vehicle.mapper.VehicleMapper;

import com.vserv.features.vehicle.service.VehicleService;

import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.NotFoundException;
import com.vserv.core.pagination.PaginationUtils;
import com.vserv.core.util.SecurityUtils;
import com.vserv.entity.AppUser;
import com.vserv.entity.Vehicle;
import com.vserv.features.user.service.UserService;
import com.vserv.features.vehicle.dto.CreateVehicleRequest;
import com.vserv.features.vehicle.dto.UpdateVehicleRequest;
import com.vserv.features.vehicle.dto.VehicleDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

	private final VehicleService vehicleService;
	private final UserService userService;
	private final SecurityUtils securityUtils;
	private final StatusToggleGuardService statusToggleGuardService;

	public VehicleController(VehicleService vehicleService, UserService userService, SecurityUtils securityUtils,
			StatusToggleGuardService statusToggleGuardService) {
		this.vehicleService = vehicleService;
		this.userService = userService;
		this.securityUtils = securityUtils;
		this.statusToggleGuardService = statusToggleGuardService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> listVehicles(@RequestParam(required = false) String q,
			@RequestParam(required = false) Vehicle.CarType carType,
			@RequestParam(required = false) String serviceStatus,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		List<Vehicle> vehicles = (q != null && !q.isBlank()) ? vehicleService.search(q) : vehicleService.findAll();
		List<VehicleDto> items = vehicles.stream().map(this::toVehicleDto)
				.filter(dto -> carType == null || carType.name().equalsIgnoreCase(dto.getCarType()))
				.filter(dto -> serviceStatus == null || serviceStatus.isBlank()
						|| matchesServiceStatus(dto, serviceStatus))
				.sorted(vehicleComparator(sort)).toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	@GetMapping("/{id}")
	public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Integer id) {
		Vehicle vehicle = vehicleService.findById(id).orElseThrow(() -> new NotFoundException("Vehicle not found."));
		securityUtils.requireVehicleReadAccess(vehicle);
		return ResponseEntity.status(HttpStatus.OK).body(toVehicleDto(vehicle));
	}

	@GetMapping("/by-user/{userId}")
	public ResponseEntity<?> listVehiclesByUser(@PathVariable Integer userId,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Vehicle.CarType carType,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		AppUser user = userService.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));
		securityUtils.requireCustomerSelfAccess(userId);
		List<VehicleDto> items = vehicleService.findByUser(user).stream().map(this::toVehicleDto)
				.filter(dto -> q == null || q.isBlank() || matchesVehicleQuery(dto, q))
				.filter(dto -> carType == null || carType.name().equalsIgnoreCase(dto.getCarType()))
				.sorted(vehicleComparator(sort)).toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	@PostMapping
	public ResponseEntity<VehicleDto> createVehicle(@Valid @RequestBody CreateVehicleRequest req) {
		securityUtils.requireCurrentUser();
		securityUtils.requireCustomerSelfAccess(req.getUserId());
		AppUser owner = userService.findById(req.getUserId())
				.orElseThrow(() -> new NotFoundException("User not found."));
		if (vehicleService.regExists(req.getRegistrationNumber()))
			throw new ConflictException("Registration number already exists.");
		Vehicle v = vehicleService.addVehicle(owner, req.getBrand(), req.getModel(), req.getRegistrationNumber(),
				req.getManufactureYear(), req.getCarType(), req.getMileage(), req.getServiceIntervalKm());
		return ResponseEntity.status(HttpStatus.CREATED).body(toVehicleDto(v));
	}

	@PutMapping("/{id}")
	public ResponseEntity<VehicleDto> updateVehicle(@PathVariable Integer id,
			@Valid @RequestBody UpdateVehicleRequest req) {
		Vehicle existing = vehicleService.findById(id).orElseThrow(() -> new NotFoundException("Vehicle not found."));
		securityUtils.requireVehicleWriteAccess(existing);
		if (vehicleService.regExistsForOther(req.getRegistrationNumber(), id))
			throw new ConflictException("Registration number already in use.");
		Vehicle v = vehicleService.updateVehicle(id, req.getBrand(), req.getModel(), req.getRegistrationNumber(),
				req.getManufactureYear(), req.getCarType(), req.getMileage(), req.getServiceIntervalKm());
		return ResponseEntity.status(HttpStatus.OK).body(toVehicleDto(v));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Map<String, Object>> toggleVehicleStatus(@PathVariable Integer id) {
		Vehicle vehicle = vehicleService.findById(id).orElseThrow(() -> new NotFoundException("Vehicle not found."));
		securityUtils.requireVehicleWriteAccess(vehicle);
		Vehicle updated = vehicleService.toggleStatus(id);
		boolean active = Boolean.TRUE.equals(updated.getIsActive());
		return ResponseEntity.status(HttpStatus.OK)
				.body(Map.of("message", active ? "Vehicle activated successfully." : "Vehicle deactivated successfully.",
						"isActive", active));
	}

	private boolean matchesVehicleQuery(VehicleDto dto, String query) {
		String normalized = query == null ? "" : query.trim().toLowerCase();
		if (normalized.isBlank()) {
			return true;
		}
		String haystack = String.join(" ", dto.getOwnerName() == null ? "" : dto.getOwnerName(),
				dto.getBrand() == null ? "" : dto.getBrand(), dto.getModel() == null ? "" : dto.getModel(),
				dto.getRegistrationNumber() == null ? "" : dto.getRegistrationNumber()).toLowerCase();
		return haystack.contains(normalized);
	}

	private boolean matchesServiceStatus(VehicleDto dto, String serviceStatus) {
		String next = classifyServiceStatus(dto);
		return next.equalsIgnoreCase(serviceStatus);
	}

	private String classifyServiceStatus(VehicleDto dto) {
		if (dto.getNextServiceDue() == null) {
			return "OK";
		}
		LocalDate today = LocalDate.now();
		long diff = dto.getNextServiceDue().toEpochDay() - today.toEpochDay();
		if (diff < 0) {
			return "OVERDUE";
		}
		if (diff <= 30) {
			return "DUE";
		}
		return "OK";
	}

	private Comparator<VehicleDto> vehicleComparator(String sort) {
		return switch (sort == null ? "newest" : sort) {
		case "brand" -> Comparator.comparing(dto -> String.join(" ", dto.getBrand() == null ? "" : dto.getBrand(),
				dto.getModel() == null ? "" : dto.getModel()), String.CASE_INSENSITIVE_ORDER);
		case "mileage" -> Comparator.comparing(dto -> dto.getMileage() == null ? 0 : dto.getMileage());
		case "recent" ->
			Comparator.comparing(VehicleDto::getVehicleId, Comparator.nullsLast(Comparator.reverseOrder()));
		case "oldest" ->
			Comparator.comparing(VehicleDto::getVehicleId, Comparator.nullsLast(Comparator.naturalOrder()));
		default -> Comparator.comparing(VehicleDto::getVehicleId, Comparator.nullsLast(Comparator.reverseOrder()));
		};
	}

	private VehicleDto toVehicleDto(Vehicle vehicle) {
		VehicleDto dto = VehicleMapper.toDto(vehicle);
		StatusToggleGuard guard = statusToggleGuardService.evaluateVehicleToggle(vehicle);
		dto.setCanToggleStatus(guard.isAllowed());
		dto.setStatusToggleReason(guard.getReason());
		return dto;
	}
}
