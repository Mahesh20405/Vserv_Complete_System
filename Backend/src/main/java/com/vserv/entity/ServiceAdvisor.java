package com.vserv.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_advisor")
public class ServiceAdvisor {

	@Id
	@Column(name = "advisor_id")
	private Integer advisorId;

	@OneToOne
	@MapsId
	@JoinColumn(name = "advisor_id")
	private AppUser user;

	@Column(length = 100)
	private String specialization;

	@Column(name = "overtime_rate", precision = 10, scale = 2)
	private BigDecimal overtimeRate;

	@Enumerated(EnumType.STRING)
	@Column(name = "availability_status")
	private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

	@Column(name = "current_load")
	private Integer currentLoad = 0;

	@Column(name = "last_assigned_at")
	private LocalDateTime lastAssignedAt;

	public ServiceAdvisor() {
	}

	public Integer getAdvisorId() {
		return advisorId;
	}

	public void setAdvisorId(Integer advisorId) {
		this.advisorId = advisorId;
	}

	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser user) {
		this.user = user;
	}

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

	public AvailabilityStatus getAvailabilityStatus() {
		return availabilityStatus;
	}

	public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
	}

	public Integer getCurrentLoad() {
		return currentLoad;
	}

	public void setCurrentLoad(Integer currentLoad) {
		this.currentLoad = currentLoad;
	}

	public LocalDateTime getLastAssignedAt() {
		return lastAssignedAt;
	}

	public void setLastAssignedAt(LocalDateTime lastAssignedAt) {
		this.lastAssignedAt = lastAssignedAt;
	}

	public enum AvailabilityStatus {
		AVAILABLE, ASSIGNED, ON_LEAVE, RESIGNED
	}
}
