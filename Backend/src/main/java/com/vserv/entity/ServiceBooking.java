package com.vserv.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_booking")
// Uniqueness for active bookings is enforced at the DB level via uq_active_vehicle_datetime
// on the generated column `active_slot` (see migration fix_booking_unique_constraint.sql).
// The old status-unaware constraint has been dropped so cancelled/completed bookings
// do not block re-booking the same vehicle + date + slot.
public class ServiceBooking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "booking_id")
	private Integer bookingId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catalog_id", nullable = false)
	private ServiceCatalog catalog;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "advisor_id")
	private AppUser advisor;

	@Column(name = "service_date", nullable = false)
	private LocalDate serviceDate;

	@Column(name = "time_slot", length = 20)
	private String timeSlot;

	@Enumerated(EnumType.STRING)
	@Column(name = "booking_status")
	private BookingStatus bookingStatus = BookingStatus.PENDING;

	@Column(name = "booking_notes", columnDefinition = "TEXT")
	private String bookingNotes;

	@Column(name = "archived_vehicle_info", length = 255)
	private String archivedVehicleInfo;

	@Column(name = "archived_owner_id")
	private Integer archivedOwnerId;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	public ServiceBooking() {
	}

	public Integer getBookingId() {
		return bookingId;
	}

	public void setBookingId(Integer bookingId) {
		this.bookingId = bookingId;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public ServiceCatalog getCatalog() {
		return catalog;
	}

	public void setCatalog(ServiceCatalog catalog) {
		this.catalog = catalog;
	}

	public AppUser getAdvisor() {
		return advisor;
	}

	public void setAdvisor(AppUser advisor) {
		this.advisor = advisor;
	}

	public LocalDate getServiceDate() {
		return serviceDate;
	}

	public void setServiceDate(LocalDate serviceDate) {
		this.serviceDate = serviceDate;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public BookingStatus getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(BookingStatus bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public String getBookingNotes() {
		return bookingNotes;
	}

	public void setBookingNotes(String bookingNotes) {
		this.bookingNotes = bookingNotes;
	}

	public String getArchivedVehicleInfo() {
		return archivedVehicleInfo;
	}

	public void setArchivedVehicleInfo(String archivedVehicleInfo) {
		this.archivedVehicleInfo = archivedVehicleInfo;
	}

	public Integer getArchivedOwnerId() {
		return archivedOwnerId;
	}

	public void setArchivedOwnerId(Integer archivedOwnerId) {
		this.archivedOwnerId = archivedOwnerId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public enum BookingStatus {
		PENDING, CONFIRMED, CANCELLED, RESCHEDULED, COMPLETED
	}
}
