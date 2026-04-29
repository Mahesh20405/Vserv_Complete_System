package com.vserv.features.vehicle.mapper;

import com.vserv.entity.Vehicle;
import com.vserv.features.vehicle.dto.VehicleDto;

public final class VehicleMapper {
	private VehicleMapper() {
	}

	public static VehicleDto toDto(Vehicle vehicle) {
		return VehicleDto.from(vehicle);
	}
}
