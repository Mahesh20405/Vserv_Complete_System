package com.vserv.features.booking.dto;

import jakarta.validation.constraints.Positive;

public class ConfirmBookingRequest {
	@Positive(message = "Advisor id must be greater than 0")
	private Integer advisorId;

	public Integer getAdvisorId() {
		return advisorId;
	}

	public void setAdvisorId(Integer v) {
		this.advisorId = v;
	}
}
