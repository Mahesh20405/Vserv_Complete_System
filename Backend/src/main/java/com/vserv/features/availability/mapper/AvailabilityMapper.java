package com.vserv.features.availability.mapper;

import com.vserv.entity.ServiceAvailability;
import com.vserv.features.availability.dto.AvailabilityDto;

public final class AvailabilityMapper {
	private AvailabilityMapper() {
	}

	public static AvailabilityDto toDto(ServiceAvailability availability) {
		return AvailabilityDto.from(availability);
	}
}
