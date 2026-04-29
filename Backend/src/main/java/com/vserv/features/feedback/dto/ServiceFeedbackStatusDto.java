package com.vserv.features.feedback.dto;

public class ServiceFeedbackStatusDto {
	private final Integer serviceId;
	private final String serviceStatus;
	private final String paymentStatus;
	private final boolean eligible;
	private final boolean alreadySubmitted;
	private final ServiceFeedbackDto feedback;

	public ServiceFeedbackStatusDto(Integer serviceId, String serviceStatus, String paymentStatus, boolean eligible,
			boolean alreadySubmitted, ServiceFeedbackDto feedback) {
		this.serviceId = serviceId;
		this.serviceStatus = serviceStatus;
		this.paymentStatus = paymentStatus;
		this.eligible = eligible;
		this.alreadySubmitted = alreadySubmitted;
		this.feedback = feedback;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public String getServiceStatus() {
		return serviceStatus;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public boolean isEligible() {
		return eligible;
	}

	public boolean isAlreadySubmitted() {
		return alreadySubmitted;
	}

	public ServiceFeedbackDto getFeedback() {
		return feedback;
	}
}
