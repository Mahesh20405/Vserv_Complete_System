package com.vserv.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "service_record")
public class ServiceRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_id")
	private Integer serviceId;

	@OneToOne
	@JoinColumn(name = "booking_id", unique = true, nullable = false)
	private ServiceBooking booking;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "advisor_id", nullable = true)
	private ServiceAdvisor advisor;

	@Column(name = "service_start_date")
	private LocalDateTime serviceStartDate;

	@Column(name = "service_end_date")
	private LocalDateTime serviceEndDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ServiceStatus status = ServiceStatus.PENDING;

	@Column(name = "estimated_hours", precision = 4, scale = 2)
	private BigDecimal estimatedHours;

	@Column(name = "actual_hours", precision = 4, scale = 2)
	private BigDecimal actualHours;

	@Column(columnDefinition = "TEXT")
	private String remarks;

	@OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.ALL)
	private List<ServiceItem> items;

	public ServiceRecord() {
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	public ServiceBooking getBooking() {
		return booking;
	}

	public void setBooking(ServiceBooking booking) {
		this.booking = booking;
	}

	public ServiceAdvisor getAdvisor() {
		return advisor;
	}

	public void setAdvisor(ServiceAdvisor advisor) {
		this.advisor = advisor;
	}

	public LocalDateTime getServiceStartDate() {
		return serviceStartDate;
	}

	public void setServiceStartDate(LocalDateTime serviceStartDate) {
		this.serviceStartDate = serviceStartDate;
	}

	public LocalDateTime getServiceEndDate() {
		return serviceEndDate;
	}

	public void setServiceEndDate(LocalDateTime serviceEndDate) {
		this.serviceEndDate = serviceEndDate;
	}

	public ServiceStatus getStatus() {
		return status;
	}

	public void setStatus(ServiceStatus status) {
		this.status = status;
	}

	public BigDecimal getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(BigDecimal estimatedHours) {
		this.estimatedHours = estimatedHours;
	}

	public BigDecimal getActualHours() {
		return actualHours;
	}

	public void setActualHours(BigDecimal actualHours) {
		this.actualHours = actualHours;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public List<ServiceItem> getItems() {
		return items;
	}

	public void setItems(List<ServiceItem> items) {
		this.items = items;
	}

	public enum ServiceStatus {
		PENDING, IN_PROGRESS, COMPLETED
	}
}
