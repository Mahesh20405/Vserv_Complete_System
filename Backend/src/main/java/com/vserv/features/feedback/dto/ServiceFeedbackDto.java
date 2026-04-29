package com.vserv.features.feedback.dto;

import com.vserv.entity.ServiceFeedback;

import java.time.LocalDateTime;

public class ServiceFeedbackDto {
	private Integer feedbackId;
	private Integer serviceId;
	private Integer customerId;
	private String customerName;
	private Integer rating;
	private String feedbackText;
	private LocalDateTime submittedAt;

	public static ServiceFeedbackDto from(ServiceFeedback feedback) {
		ServiceFeedbackDto dto = new ServiceFeedbackDto();
		dto.feedbackId = feedback.getFeedbackId();
		dto.serviceId = feedback.getServiceRecord() != null ? feedback.getServiceRecord().getServiceId() : null;
		dto.customerId = feedback.getCustomer() != null ? feedback.getCustomer().getUserId() : null;
		dto.customerName = feedback.getCustomer() != null ? feedback.getCustomer().getFullName() : null;
		dto.rating = feedback.getRating();
		dto.feedbackText = feedback.getFeedbackText();
		dto.submittedAt = feedback.getSubmittedAt();
		return dto;
	}

	public Integer getFeedbackId() {
		return feedbackId;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public Integer getCustomerId() {
		return customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public Integer getRating() {
		return rating;
	}

	public String getFeedbackText() {
		return feedbackText;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}
}
