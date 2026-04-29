package com.vserv.core.util;

import com.vserv.core.config.VservUserDetails;
import com.vserv.core.exception.ForbiddenException;
import com.vserv.core.exception.NotFoundException;
import com.vserv.entity.AppUser;
import com.vserv.entity.Invoice;
import com.vserv.entity.Role;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.ServiceRecord;
import com.vserv.entity.Vehicle;
import com.vserv.features.user.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class SecurityUtils {

    private final UserRepository userRepo;
    private AppUser currentUser;
    private boolean currentUserResolved;

    public SecurityUtils(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public AppUser currentUser() {
        if (currentUserResolved) {
            return currentUser;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            currentUserResolved = true;
            return null;
        }
        if (auth.getPrincipal() instanceof VservUserDetails ud) {
            currentUser = userRepo.findByUserIdAndIsDeletedFalse(ud.getUserId()).orElse(null);
        }
        currentUserResolved = true;
        return currentUser;
    }

    public AppUser requireCurrentUser() {
        AppUser user = currentUser();
        if (user == null) {
            throw new ForbiddenException("No authenticated user.");
        }
        return user;
    }

    public boolean isAdmin() {
        return hasRole(Role.RoleName.ADMIN);
    }

    public boolean isAdvisor() {
        return hasRole(Role.RoleName.ADVISOR);
    }

    public boolean isCustomer() {
        return hasRole(Role.RoleName.CUSTOMER);
    }

    public boolean isCurrentUserId(Integer userId) {
        AppUser user = currentUser();
        return userId != null && user != null && userId.equals(user.getUserId());
    }

    public void requireCustomerSelfAccess(Integer userId) {
        if (isCustomer() && !isCurrentUserId(userId)) {
            throw new ForbiddenException("Access denied.");
        }
    }

    public void requireVehicleReadAccess(Vehicle vehicle) {
        if (vehicle == null) {
            throw new NotFoundException("Vehicle not found.");
        }
        if (isAdmin() || isAdvisor()) {
            return;
        }
        requireVehicleOwnership(vehicle);
    }

    public void requireVehicleWriteAccess(Vehicle vehicle) {
        if (vehicle == null) {
            throw new NotFoundException("Vehicle not found.");
        }
        if (isAdmin()) {
            return;
        }
        requireVehicleOwnership(vehicle);
    }

    public void requireServiceRecordAccess(ServiceRecord record) {
        if (isAdmin()) {
            return;
        }
        Integer advisorId = record != null && record.getAdvisor() != null && record.getAdvisor().getUser() != null
                ? record.getAdvisor().getUser().getUserId()
                : null;
        if (!isAdvisor() || !isCurrentUserId(advisorId)) {
            throw new ForbiddenException("Access denied.");
        }
    }

    public void requireBookingAccess(ServiceBooking booking, ServiceRecord record) {
        if (isAdmin()) {
            return;
        }
        if (isAdvisor()) {
            Integer advisorId = record != null && record.getAdvisor() != null && record.getAdvisor().getUser() != null
                    ? record.getAdvisor().getUser().getUserId()
                    : null;
            if (!isCurrentUserId(advisorId)) {
                throw new ForbiddenException("Access denied.");
            }
            return;
        }
        Integer ownerId = booking != null && booking.getVehicle() != null && booking.getVehicle().getUser() != null
                ? booking.getVehicle().getUser().getUserId()
                : booking != null ? booking.getArchivedOwnerId() : null;
        if (!isCurrentUserId(ownerId)) {
            throw new ForbiddenException("Access denied.");
        }
    }

    public void requireInvoiceAccess(Invoice invoice) {
        if (isAdmin()) {
            return;
        }
        Integer ownerId = invoice != null && invoice.getServiceRecord() != null
                && invoice.getServiceRecord().getBooking() != null
                && invoice.getServiceRecord().getBooking().getVehicle() != null
                && invoice.getServiceRecord().getBooking().getVehicle().getUser() != null
                ? invoice.getServiceRecord().getBooking().getVehicle().getUser().getUserId()
                : null;
        Integer advisorId = invoice != null && invoice.getServiceRecord() != null
                && invoice.getServiceRecord().getAdvisor() != null
                && invoice.getServiceRecord().getAdvisor().getUser() != null
                ? invoice.getServiceRecord().getAdvisor().getUser().getUserId()
                : null;
        if (isAdvisor()) {
            if (!isCurrentUserId(advisorId)) {
                throw new ForbiddenException("You are not authorized to access this invoice.");
            }
            return;
        }
        if (!isCurrentUserId(ownerId)) {
            throw new ForbiddenException("You are not authorized to access this invoice.");
        }
    }

    private boolean hasRole(Role.RoleName roleName) {
        AppUser user = currentUser();
        return user != null && user.getRole() != null && user.getRole().getRoleName() == roleName;
    }

    private void requireVehicleOwnership(Vehicle vehicle) {
        Integer ownerId = vehicle.getUser() != null ? vehicle.getUser().getUserId() : null;
        if (!isCustomer() || !isCurrentUserId(ownerId)) {
            throw new ForbiddenException("Access denied.");
        }
    }
}
