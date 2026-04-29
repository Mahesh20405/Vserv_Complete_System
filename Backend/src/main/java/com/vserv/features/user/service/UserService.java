package com.vserv.features.user.service;

import com.vserv.entity.AppUser;
import com.vserv.entity.Role;
import com.vserv.entity.ServiceAdvisor;
import com.vserv.features.user.dto.UpdateUserRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
	Optional<AppUser> findById(Integer id);

	Optional<AppUser> findActiveByEmail(String email);

	boolean emailExists(String email);

	boolean phoneExists(String phone);

	List<AppUser> findAllActive();

	long countActiveUsers();

	List<AppUser> searchActive(String q);

	List<AppUser> searchActiveByRole(String q, Role.RoleName roleName);

	Optional<ServiceAdvisor> findAdvisorByUserId(Integer userId);

	Map<Integer, ServiceAdvisor> findAdvisorsByUserIds(List<Integer> userIds);

	long countCompletedServices(Integer userId);

	Map<Integer, Long> countCompletedServices(List<Integer> userIds);

	boolean refreshLoyaltyBadge(Integer userId);

	boolean isLoyalCustomer(Integer userId);

	int getLoyaltyThreshold();

	List<AppUser> findByRole(Role.RoleName roleName);

	AppUser registerCustomer(String fullName, String email, String rawPassword, String phone, AppUser.Gender gender);

	AppUser createUser(String fullName, String email, String rawPassword, String phone, AppUser.Gender gender,
			Role.RoleName roleName, String specialization, ServiceAdvisor.AvailabilityStatus availabilityStatus,
			BigDecimal overtimeRate);

	AppUser updateAdminUser(Integer userId, UpdateUserRequest req);

	AppUser updateProfile(Integer userId, String fullName, String phone);

	boolean changePassword(Integer userId, String currentRaw, String newRaw);

	AppUser.Status toggleStatus(Integer userId);

	AppUser updateLastLogin(String email);

	AppUser save(AppUser user);

	AppUser requireActiveUserByEmail(String email);
}
