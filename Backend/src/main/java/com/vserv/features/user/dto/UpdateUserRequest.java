package com.vserv.features.user.dto;

import com.vserv.core.util.ValidationPatterns;
import com.vserv.entity.AppUser;
import com.vserv.entity.Role;
import com.vserv.entity.ServiceAdvisor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateUserRequest {
	@NotBlank(message = "Full name is required")
	@Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
	@Pattern(regexp = ValidationPatterns.NAME, message = "Full name can only contain letters, spaces, hyphens, apostrophes, and dots")
	private String fullName;
	@NotBlank(message = "Phone is required")
	@Pattern(regexp = ValidationPatterns.PHONE, message = "Enter a valid Indian mobile number")
	private String phone;
	private AppUser.Gender gender;
	@NotNull(message = "Role is required")
	private Role.RoleName roleName;
	@Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
	@Pattern(regexp = ValidationPatterns.PASSWORD, message = "Password must include uppercase, lowercase, digit, and special character")
	private String password;
	@Size(max = 100, message = "Specialization must be at most 100 characters")
	private String specialization;
	private ServiceAdvisor.AvailabilityStatus availabilityStatus;
	@DecimalMin(value = "0.0", inclusive = true, message = "Overtime rate must be 0 or greater")
	private BigDecimal overtimeRate;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String v) {
		this.fullName = v;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String v) {
		this.phone = v;
	}

	public AppUser.Gender getGender() {
		return gender;
	}

	public void setGender(AppUser.Gender v) {
		this.gender = v;
	}

	public Role.RoleName getRoleName() {
		return roleName;
	}

	public void setRoleName(Role.RoleName v) {
		this.roleName = v;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String v) {
		this.password = v;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String v) {
		this.specialization = v;
	}

	public ServiceAdvisor.AvailabilityStatus getAvailabilityStatus() {
		return availabilityStatus;
	}

	public void setAvailabilityStatus(ServiceAdvisor.AvailabilityStatus v) {
		this.availabilityStatus = v;
	}

	public BigDecimal getOvertimeRate() {
		return overtimeRate;
	}

	public void setOvertimeRate(BigDecimal v) {
		this.overtimeRate = v;
	}
}
