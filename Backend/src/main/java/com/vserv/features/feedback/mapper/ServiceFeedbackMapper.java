package com.vserv.features.feedback.mapper;

import com.vserv.entity.ServiceFeedback;
import com.vserv.features.feedback.dto.ServiceFeedbackDto;

public final class ServiceFeedbackMapper {
	private ServiceFeedbackMapper() {
	}

	public static ServiceFeedbackDto toDto(ServiceFeedback feedback) {
		return ServiceFeedbackDto.from(feedback);
	}
}
