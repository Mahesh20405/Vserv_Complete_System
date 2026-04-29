package com.vserv.features.advisor.mapper;

import com.vserv.entity.ServiceAdvisor;
import com.vserv.features.advisor.dto.AdvisorDto;

public final class AdvisorMapper {
	private AdvisorMapper() {
	}

	public static AdvisorDto toDto(ServiceAdvisor advisor) {
		return AdvisorDto.from(advisor);
	}
}
