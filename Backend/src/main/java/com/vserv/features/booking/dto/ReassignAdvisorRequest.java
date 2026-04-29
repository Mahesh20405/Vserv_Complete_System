package com.vserv.features.booking.dto;

import jakarta.validation.constraints.NotNull;

public class ReassignAdvisorRequest {
	@NotNull(message = "Advisor ID is required")
	private Integer advisorId;

	public Integer getAdvisorId() {
		return advisorId;
	}

	public void setAdvisorId(Integer advisorId) {
		this.advisorId = advisorId;
	}
}
