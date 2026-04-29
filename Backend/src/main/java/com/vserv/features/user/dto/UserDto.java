package com.vserv.features.user.dto;

import com.vserv.entity.AppUser;
import com.vserv.entity.ServiceAdvisor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class UserDto {
	private Integer userId;
	private String fullName;
	private String email;
	private String phone;
	private String gender;
	private String role;
	private String status;
	private LocalDateTime lastLogin;
	private LocalDateTime createdAt;
	private String specialization;
	private String availabilityStatus;
	private BigDecimal overtimeRate;
	private Long completedServicesCount;
	private Boolean loyaltyEligible;
	private Boolean canToggleStatus;
	private String statusToggleReason;

	public static UserDto from(AppUser u) {
		UserDto dto = new UserDto();
		dto.userId = u.getUserId();
		dto.fullName = u.getFullName();
		dto.email = u.getEmail();
		dto.phone = u.getPhone();
		dto.gender = u.getGender() != null ? u.getGender().name() : null;
		dto.role = u.getRole() != null ? u.getRole().getRoleName().name() : null;
		dto.status = u.getStatus() != null ? u.getStatus().name() : null;
		dto.lastLogin = u.getLastLogin();
		dto.createdAt = u.getCreatedAt();
		dto.loyaltyEligible = Boolean.TRUE.equals(u.getLoyaltyBadge());
		return dto;
	}

	public static UserDto from(AppUser u, ServiceAdvisor advisor) {
		UserDto dto = from(u);
		if (advisor != null) {
			dto.specialization = advisor.getSpecialization();
			dto.availabilityStatus = advisor.getAvailabilityStatus() != null ? advisor.getAvailabilityStatus().name()
					: null;
			dto.overtimeRate = advisor.getOvertimeRate();
		}
		return dto;
	}

	public Integer getUserId() {
		return userId;
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

	public String getGender() {
		return gender;
	}

	public String getRole() {
		return role;
	}

	public String getStatus() {
		return status;
	}

	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getSpecialization() {
		return specialization;
	}

	public String getAvailabilityStatus() {
		return availabilityStatus;
	}

	public BigDecimal getOvertimeRate() {
		return overtimeRate;
	}

	public Long getCompletedServicesCount() {
		return completedServicesCount;
	}

	public void setCompletedServicesCount(Long completedServicesCount) {
		this.completedServicesCount = completedServicesCount;
	}

	public Boolean getLoyaltyEligible() {
		return loyaltyEligible;
	}

	public void setLoyaltyEligible(Boolean loyaltyEligible) {
		this.loyaltyEligible = loyaltyEligible;
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
