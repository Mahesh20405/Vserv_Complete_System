package com.vserv.features.feedback.service;

import com.vserv.entity.AppUser;
import com.vserv.entity.ServiceFeedback;
import com.vserv.features.feedback.dto.ServiceFeedbackStatusDto;

import java.util.List;

public interface ServiceFeedbackService {
	List<ServiceFeedback> findAll();

	ServiceFeedbackStatusDto getStatus(Integer serviceId, AppUser actor, boolean isAdmin);

	ServiceFeedback create(Integer serviceId, Integer rating, String feedbackText, AppUser customer);
}
