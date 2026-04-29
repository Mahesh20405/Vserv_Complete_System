package com.vserv.features.feedback.controller;

import com.vserv.features.feedback.mapper.ServiceFeedbackMapper;

import com.vserv.features.feedback.service.ServiceFeedbackService;

import com.vserv.core.util.SecurityUtils;
import com.vserv.entity.AppUser;
import com.vserv.features.feedback.dto.CreateServiceFeedbackRequest;
import com.vserv.features.feedback.dto.ServiceFeedbackDto;
import com.vserv.features.feedback.dto.ServiceFeedbackStatusDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-feedback")
public class ServiceFeedbackController {

	private final ServiceFeedbackService feedbackService;
	private final SecurityUtils securityUtils;

	public ServiceFeedbackController(ServiceFeedbackService feedbackService, SecurityUtils securityUtils) {
		this.feedbackService = feedbackService;
		this.securityUtils = securityUtils;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ServiceFeedbackDto>> listServiceFeedback() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(feedbackService.findAll().stream().map(ServiceFeedbackMapper::toDto).toList());
	}

	@GetMapping("/service/{serviceId}")
	@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
	public ResponseEntity<ServiceFeedbackStatusDto> getServiceFeedbackStatus(
			@PathVariable Integer serviceId) {
		AppUser actor = securityUtils.requireCurrentUser();
		return ResponseEntity.status(HttpStatus.OK)
				.body(feedbackService.getStatus(serviceId, actor, securityUtils.isAdmin()));
	}

	@PostMapping("/service/{serviceId}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ServiceFeedbackDto> createServiceFeedback(@PathVariable Integer serviceId,
			@Valid @RequestBody CreateServiceFeedbackRequest request) {
		AppUser customer = securityUtils.requireCurrentUser();
		return ResponseEntity.status(HttpStatus.CREATED).body(ServiceFeedbackMapper
				.toDto(feedbackService.create(serviceId, request.getRating(), request.getFeedbackText(), customer)));
	}
}
