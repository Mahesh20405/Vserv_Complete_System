package com.vserv.features.advisor.dto;

import com.vserv.entity.ServiceAdvisor;
import java.math.BigDecimal;

public class AdvisorDto {
	private Integer advisorId;
	private String fullName;
	private String email;
	private String phone;
	private String userStatus;
	private Boolean isDeleted;
	private String specialization;
	private BigDecimal overtimeRate;
	private String availabilityStatus;
	private Integer currentLoad;
	private Boolean canToggleStatus;
	private String statusToggleReason;

	public static AdvisorDto from(ServiceAdvisor a) {
		AdvisorDto dto = new AdvisorDto();
		dto.advisorId = a.getAdvisorId();
		dto.specialization = a.getSpecialization();
		dto.overtimeRate = a.getOvertimeRate();
		dto.availabilityStatus = a.getAvailabilityStatus() != null ? a.getAvailabilityStatus().name() : null;
		dto.currentLoad = a.getCurrentLoad();
		if (a.getUser() != null) {
			dto.fullName = a.getUser().getFullName();
			dto.email = a.getUser().getEmail();
			dto.phone = a.getUser().getPhone();
			dto.userStatus = a.getUser().getStatus() != null ? a.getUser().getStatus().name() : null;
			dto.isDeleted = a.getUser().getIsDeleted();
		}
		return dto;
	}

	public Integer getAdvisorId() {
		return advisorId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public String getSpecialization() {
		return specialization;
	}

	public BigDecimal getOvertimeRate() {
		return overtimeRate;
	}

	public String getAvailabilityStatus() {
		return availabilityStatus;
	}

	public Integer getCurrentLoad() {
		return currentLoad;
	}

	public Boolean getCanToggleStatus() {
		return canToggleStatus;
	}

	public void setCanToggleStatus(Boolean canToggleStatus) {
		this.canToggleStatus = canToggleStatus;
	}

	public String getStatusToggleReason() {
		return statusToggleReason;
	}

	public void setStatusToggleReason(String statusToggleReason) {
		this.statusToggleReason = statusToggleReason;
	}
}
