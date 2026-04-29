package com.vserv.features.servicerecord.dto;

import com.vserv.entity.ServiceRecord;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceRecordDto {
	private Integer serviceId;
	private Integer bookingId;
	private String vehicleInfo;
	private String ownerName;
	private String serviceName;
	private Integer advisorId;
	private String advisorName;
	private String status;
	private LocalDateTime serviceStartDate;
	private LocalDateTime serviceEndDate;
	private BigDecimal estimatedHours;
	private BigDecimal actualHours;
	private String remarks;

	public static ServiceRecordDto from(ServiceRecord r) {
		ServiceRecordDto dto = new ServiceRecordDto();
		dto.serviceId = r.getServiceId();
		dto.status = r.getStatus() != null ? r.getStatus().name() : null;
		dto.serviceStartDate = r.getServiceStartDate();
		dto.serviceEndDate = r.getServiceEndDate();
		dto.estimatedHours = r.getEstimatedHours();
		dto.actualHours = r.getActualHours();
		dto.remarks = r.getRemarks();
		if (r.getBooking() != null) {
			dto.bookingId = r.getBooking().getBookingId();
			if (r.getBooking().getVehicle() != null) {
				dto.vehicleInfo = r.getBooking().getVehicle().getBrand() + " " + r.getBooking().getVehicle().getModel()
						+ " (" + r.getBooking().getVehicle().getRegistrationNumber() + ")";
				if (r.getBooking().getVehicle().getUser() != null)
					dto.ownerName = r.getBooking().getVehicle().getUser().getFullName();
			}
			if (r.getBooking().getCatalog() != null)
				dto.serviceName = r.getBooking().getCatalog().getServiceName();
		}
		if (r.getAdvisor() != null) {
			dto.advisorId = r.getAdvisor().getAdvisorId(); // same numeric value as AppUser.userId via @MapsId
		}
		if (r.getAdvisor() != null && r.getAdvisor().getUser() != null)
			dto.advisorName = r.getAdvisor().getUser().getFullName();
		return dto;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public Integer getBookingId() {
		return bookingId;
	}

	public String getVehicleInfo() {
		return vehicleInfo;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Integer getAdvisorId() {
		return advisorId;
	}

	public String getAdvisorName() {
		return advisorName;
	}

	public String getStatus() {
		return status;
	}

	public LocalDateTime getServiceStartDate() {
		return serviceStartDate;
	}

	public LocalDateTime getServiceEndDate() {
		return serviceEndDate;
	}

	public BigDecimal getEstimatedHours() {
		return estimatedHours;
	}

	public BigDecimal getActualHours() {
		return actualHours;
	}

	public String getRemarks() {
		return remarks;
	}
}
