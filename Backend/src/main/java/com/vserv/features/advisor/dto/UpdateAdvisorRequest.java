package com.vserv.features.advisor.dto;

import com.vserv.entity.ServiceAdvisor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateAdvisorRequest {
	@NotBlank(message = "Specialization must not be blank")
	@Size(max = 100, message = "Specialization must be at most 100 characters")
	private String specialization;

	@DecimalMin(value = "0.0", inclusive = true, message = "Overtime rate must be 0 or greater")
	private BigDecimal overtimeRate;

	private ServiceAdvisor.AvailabilityStatus availabilityStatus;

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public BigDecimal getOvertimeRate() {
		return overtimeRate;
	}

	public void setOvertimeRate(BigDecimal overtimeRate) {
		this.overtimeRate = overtimeRate;
	}

	public ServiceAdvisor.AvailabilityStatus getAvailabilityStatus() {
		return availabilityStatus;
	}

	public void setAvailabilityStatus(ServiceAdvisor.AvailabilityStatus availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
	}
}
