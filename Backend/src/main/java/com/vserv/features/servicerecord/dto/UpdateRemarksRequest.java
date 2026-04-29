package com.vserv.features.servicerecord.dto;

import jakarta.validation.constraints.Size;

public class UpdateRemarksRequest {
	@Size(max = 2000, message = "Remarks must be at most 2000 characters")
	private String remarks;

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
