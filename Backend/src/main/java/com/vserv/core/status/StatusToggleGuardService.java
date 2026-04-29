package com.vserv.core.status;

import com.vserv.entity.AppUser;
import com.vserv.entity.Invoice;
import com.vserv.entity.Role;
import com.vserv.entity.ServiceAdvisor;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.Vehicle;
import com.vserv.features.advisor.repository.ServiceAdvisorRepository;
import com.vserv.features.booking.repository.ServiceBookingRepository;
import com.vserv.features.invoice.repository.InvoiceRepository;
import com.vserv.features.servicerecord.repository.ServiceRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusToggleGuardService {

	private final ServiceBookingRepository bookingRepository;
	private final InvoiceRepository invoiceRepository;
	private final ServiceRecordRepository serviceRecordRepository;
	private final ServiceAdvisorRepository advisorRepository;

	public StatusToggleGuardService(ServiceBookingRepository bookingRepository, InvoiceRepository invoiceRepository,
			ServiceRecordRepository serviceRecordRepository, ServiceAdvisorRepository advisorRepository) {
		this.bookingRepository = bookingRepository;
		this.invoiceRepository = invoiceRepository;
		this.serviceRecordRepository = serviceRecordRepository;
		this.advisorRepository = advisorRepository;
	}

	public StatusToggleGuard evaluateUserToggle(AppUser user) {
		if (user == null) {
			return StatusToggleGuard.blocked("User not found.");
		}
		if (user.getStatus() != AppUser.Status.ACTIVE) {
			return StatusToggleGuard.allowed();
		}

		Role.RoleName roleName = user.getRole() != null ? user.getRole().getRoleName() : null;
		if (roleName == Role.RoleName.CUSTOMER) {
			if (bookingRepository.existsOpenBookingsForCustomer(user.getUserId(), openStatuses())) {
				return StatusToggleGuard
						.blocked("This customer still has active bookings. Resolve them before deactivation.");
			}
			if (invoiceRepository.existsUnsettledInvoicesForCustomer(user.getUserId(), unpaidStatuses())) {
				return StatusToggleGuard
						.blocked("This customer still has pending payments. Settle them before deactivation.");
			}
			return StatusToggleGuard.allowed();
		}

		if (roleName == Role.RoleName.ADVISOR) {
			ServiceAdvisor advisor = advisorRepository.findByUserUserId(user.getUserId()).orElse(null);
			if (advisor != null) {
				return evaluateAdvisorToggle(advisor);
			}
		}

		return StatusToggleGuard.allowed();
	}

	public StatusToggleGuard evaluateAdvisorToggle(ServiceAdvisor advisor) {
		if (advisor == null || advisor.getUser() == null) {
			return StatusToggleGuard.blocked("Advisor not found.");
		}
		if (advisor.getUser().getStatus() != AppUser.Status.ACTIVE) {
			return StatusToggleGuard.allowed();
		}
		if (serviceRecordRepository.existsOpenAssignmentsForAdvisorUser(advisor.getUser().getUserId())) {
			return StatusToggleGuard
					.blocked("This advisor still has active assignments. Complete or reassign them first.");
		}
		if ((advisor.getCurrentLoad() != null ? advisor.getCurrentLoad() : 0) > 0) {
			return StatusToggleGuard
					.blocked("This advisor still has active assignments. Complete or reassign them first.");
		}
		return StatusToggleGuard.allowed();
	}

	public StatusToggleGuard evaluateVehicleToggle(Vehicle vehicle) {
		if (vehicle == null) {
			return StatusToggleGuard.blocked("Vehicle not found.");
		}
		if (Boolean.FALSE.equals(vehicle.getIsActive())) {
			if (vehicle.getUser() != null && vehicle.getUser().getStatus() != AppUser.Status.ACTIVE) {
				return StatusToggleGuard.blocked("This vehicle's owner account is inactive.");
			}
			return StatusToggleGuard.allowed();
		}
		if (bookingRepository.existsOpenBookingsForVehicle(vehicle.getVehicleId(), openStatuses())) {
			return StatusToggleGuard.blocked("This vehicle still has active bookings.");
		}
		if (invoiceRepository.existsUnsettledInvoicesForVehicle(vehicle.getVehicleId(), unpaidStatuses())) {
			return StatusToggleGuard.blocked("This vehicle still has pending payments.");
		}
		return StatusToggleGuard.allowed();
	}

	private List<ServiceBooking.BookingStatus> openStatuses() {
		return List.of(ServiceBooking.BookingStatus.PENDING, ServiceBooking.BookingStatus.CONFIRMED,
				ServiceBooking.BookingStatus.RESCHEDULED);
	}

	private List<Invoice.PaymentStatus> unpaidStatuses() {
		return List.of(Invoice.PaymentStatus.PENDING, Invoice.PaymentStatus.PARTIALLY_PAID);
	}
}
