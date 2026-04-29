package com.vserv.features.user.service.impl;

import com.vserv.core.status.StatusToggleGuard;
import com.vserv.core.status.StatusToggleGuardService;
import com.vserv.features.user.repository.RoleRepository;

import com.vserv.features.user.repository.UserRepository;

import com.vserv.features.user.service.UserService;

import com.vserv.entity.AppUser;
import com.vserv.entity.Role;
import com.vserv.entity.ServiceAdvisor;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.advisor.repository.ServiceAdvisorRepository;
import com.vserv.features.servicerecord.repository.ServiceRecordRepository;
import com.vserv.core.exception.BusinessException;
import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.NotFoundException;
import com.vserv.core.util.ValidationPatterns;
import com.vserv.features.advisor.service.AdvisorService;
import com.vserv.features.user.dto.UpdateUserRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	private static final int LOYALTY_COMPLETED_SERVICE_THRESHOLD = 20;
	private static final Pattern NAME_RE = Pattern.compile(ValidationPatterns.NAME);
	private static final Pattern PHONE_RE = Pattern.compile(ValidationPatterns.PHONE);
	private static final Pattern PASSWORD_RE = Pattern.compile(ValidationPatterns.PASSWORD);

	private final UserRepository userRepo;
	private final RoleRepository roleRepo;
	private final ServiceAdvisorRepository advisorRepo;
	private final ServiceRecordRepository serviceRecordRepo;
	private final PasswordEncoder passwordEncoder;
	private final StatusToggleGuardService statusToggleGuardService;

	public UserServiceImpl(UserRepository userRepo, RoleRepository roleRepo, ServiceAdvisorRepository advisorRepo,
			ServiceRecordRepository serviceRecordRepo, PasswordEncoder passwordEncoder,
			StatusToggleGuardService statusToggleGuardService) {
		this.userRepo = userRepo;
		this.roleRepo = roleRepo;
		this.advisorRepo = advisorRepo;
		this.serviceRecordRepo = serviceRecordRepo;
		this.passwordEncoder = passwordEncoder;
		this.statusToggleGuardService = statusToggleGuardService;
	}

	public Optional<AppUser> findById(Integer id) {
		return userRepo.findByUserIdAndIsDeletedFalse(id);
	}

	public Optional<AppUser> findActiveByEmail(String email) {
		return userRepo.findByEmailAndIsDeletedFalse(normalizeEmail(email));
	}

	public boolean emailExists(String email) {
		return userRepo.existsByEmailAndIsDeletedFalse(normalizeEmail(email));
	}

	public boolean phoneExists(String phone) {
		return phone != null && userRepo.existsByPhoneAndIsDeletedFalse(phone);
	}

	public List<AppUser> findAllActive() {
		return userRepo.findByIsDeletedFalse();
	}

	public long countActiveUsers() {
		return userRepo.countByIsDeletedFalse();
	}

	public List<AppUser> searchActive(String q) {
		return userRepo.searchActive(q);
	}

	public List<AppUser> searchActiveByRole(String q, Role.RoleName roleName) {
		return roleRepo.findByRoleName(roleName).map(role -> userRepo.searchActiveByRole(q, role)).orElse(List.of());
	}

	public Optional<ServiceAdvisor> findAdvisorByUserId(Integer userId) {
		return advisorRepo.findByUserUserId(userId);
	}

	public Map<Integer, ServiceAdvisor> findAdvisorsByUserIds(List<Integer> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Map.of();
		}
		return advisorRepo.findByUserUserIdIn(userIds.stream().distinct().toList()).stream()
				.filter(advisor -> advisor.getUser() != null && advisor.getUser().getUserId() != null)
				.collect(Collectors.toMap(advisor -> advisor.getUser().getUserId(), Function.identity()));
	}

	public long countCompletedServices(Integer userId) {
		if (userId == null) {
			return 0L;
		}
		return serviceRecordRepo.countByBookingVehicleUserUserIdAndStatus(userId,
				ServiceRecord.ServiceStatus.COMPLETED);
	}

	public Map<Integer, Long> countCompletedServices(List<Integer> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Map.of();
		}
		List<Integer> distinctUserIds = userIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
		if (distinctUserIds.isEmpty()) {
			return Map.of();
		}
		Map<Integer, Long> completedCounts = serviceRecordRepo
				.countByCustomerIdsAndStatus(distinctUserIds, ServiceRecord.ServiceStatus.COMPLETED).stream()
				.collect(Collectors.toMap(row -> (Integer) row[0], row -> ((Number) row[1]).longValue()));
		return distinctUserIds.stream()
				.collect(Collectors.toMap(Function.identity(), userId -> completedCounts.getOrDefault(userId, 0L)));
	}

	@Transactional
	public boolean refreshLoyaltyBadge(Integer userId) {
		log.info("Refreshing loyalty badge for userId={}", userId);
		AppUser user = requireActiveUser(userId);
		boolean eligible = countCompletedServices(userId) >= LOYALTY_COMPLETED_SERVICE_THRESHOLD;
		if (!java.util.Objects.equals(user.getLoyaltyBadge(), eligible)) {
			user.setLoyaltyBadge(eligible);
			userRepo.save(user);
		}
		return eligible;
	}

	public boolean isLoyalCustomer(Integer userId) {
		return refreshLoyaltyBadge(userId);
	}

	public int getLoyaltyThreshold() {
		return LOYALTY_COMPLETED_SERVICE_THRESHOLD;
	}

	public List<AppUser> findByRole(Role.RoleName roleName) {
		return roleRepo.findByRoleName(roleName).map(userRepo::findByRoleAndIsDeletedFalse).orElse(List.of());
	}

	@Transactional
	public AppUser registerCustomer(String fullName, String email, String rawPassword, String phone,
			AppUser.Gender gender) {
		validateName(fullName);
		validatePassword(rawPassword);
		String normalizedPhone = trimToNull(phone);
		if (normalizedPhone != null) {
			validatePhone(normalizedPhone, null);
		}
		Role role = roleRepo.findByRoleName(Role.RoleName.CUSTOMER)
				.orElseThrow(() -> new RuntimeException("CUSTOMER role not found"));
		AppUser user = new AppUser();
		user.setFullName(fullName.trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setPhone(normalizedPhone);
		user.setGender(gender);
		user.setRole(role);
		log.info("Customer registered email={}", email);
		return userRepo.save(user);
	}

	@Transactional
	public AppUser createUser(String fullName, String email, String rawPassword, String phone, AppUser.Gender gender,
			Role.RoleName roleName, String specialization, ServiceAdvisor.AvailabilityStatus availabilityStatus,
			BigDecimal overtimeRate) {
		validateName(fullName);
		validatePhone(phone, null);
		validatePassword(rawPassword);
		Role role = roleRepo.findByRoleName(roleName).orElseThrow(() -> new RuntimeException("Role not found"));
		AppUser user = new AppUser();
		user.setFullName(fullName.trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setPhone(phone.trim());
		user.setGender(gender);
		user.setRole(role);
		log.info("User created email={} role={}", email, roleName);
		AppUser saved = userRepo.save(user);
		if (roleName == Role.RoleName.ADVISOR) {
			ServiceAdvisor advisor = new ServiceAdvisor();
			advisor.setUser(saved);
			advisor.setSpecialization(trimToNull(specialization));
			advisor.setAvailabilityStatus(
					availabilityStatus != null ? availabilityStatus : ServiceAdvisor.AvailabilityStatus.AVAILABLE);
			advisor.setOvertimeRate(overtimeRate != null ? overtimeRate : AdvisorService.DEFAULT_OVERTIME_RATE);
			advisorRepo.save(advisor);
		}
		return saved;
	}

	@Transactional
	public AppUser updateAdminUser(Integer userId, UpdateUserRequest req) {
		AppUser user = requireActiveUser(userId);

		validateName(req.getFullName());
		validatePhone(req.getPhone(), userId);
		if (req.getPassword() != null && !req.getPassword().isBlank()) {
			validatePassword(req.getPassword());
			user.setPassword(passwordEncoder.encode(req.getPassword().trim()));
		}

		user.setFullName(req.getFullName().trim());
		user.setPhone(req.getPhone().trim());
		user.setGender(req.getGender());

		Role.RoleName roleName = req.getRoleName() != null ? req.getRoleName() : user.getRole().getRoleName();
		user.setRole(roleRepo.findByRoleName(roleName).orElseThrow(() -> new RuntimeException("Role not found")));

		syncAdvisorFields(user, roleName, req);
		return userRepo.save(user);
	}

	@Transactional
	public AppUser updateProfile(Integer userId, String fullName, String phone) {
		AppUser user = requireActiveUser(userId);
		validateName(fullName);
		user.setFullName(fullName.trim());
		String normalizedPhone = trimToNull(phone);
		if (normalizedPhone != null) {
			validatePhone(normalizedPhone, userId);
			user.setPhone(normalizedPhone);
		}
		return userRepo.save(user);
	}

	@Transactional
	public boolean changePassword(Integer userId, String currentRaw, String newRaw) {
		AppUser user = requireActiveUser(userId);
		if (!passwordEncoder.matches(currentRaw, user.getPassword())) {
			log.warn("Change password failed – wrong current password for userId={}", userId);
			return false;
		}
		validatePassword(newRaw);
		if (passwordEncoder.matches(newRaw, user.getPassword()))
			throw new BusinessException("New password must be different from the current password.");
		log.info("Password changed for userId={}", userId);
		user.setPassword(passwordEncoder.encode(newRaw));
		userRepo.save(user);
		return true;
	}

	@Transactional
	public AppUser.Status toggleStatus(Integer userId) {
		log.info("Toggling status for userId={}", userId);
		AppUser user = requireActiveUser(userId);
		if (user.getStatus() == AppUser.Status.ACTIVE) {
			StatusToggleGuard guard = statusToggleGuardService.evaluateUserToggle(user);
			if (!guard.isAllowed()) {
				throw new BusinessException(guard.getReason());
			}
		}
		user.setStatus(user.getStatus() == AppUser.Status.ACTIVE ? AppUser.Status.INACTIVE : AppUser.Status.ACTIVE);
		userRepo.save(user);
		return user.getStatus();
	}

	@Transactional
	public AppUser updateLastLogin(String email) {
		AppUser user = requireActiveUserByEmail(email);
		user.setLastLogin(LocalDateTime.now());
		return userRepo.save(user);
	}

	@Transactional
	public AppUser save(AppUser user) {
		return userRepo.save(user);
	}

	private AppUser requireActiveUser(Integer userId) {
		return userRepo.findByUserIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new NotFoundException("User not found."));
	}

	public AppUser requireActiveUserByEmail(String email) {
		return userRepo.findByEmailAndIsDeletedFalse(normalizeEmail(email))
				.orElseThrow(() -> new NotFoundException("User not found."));
	}

	private void syncAdvisorFields(AppUser user, Role.RoleName roleName, UpdateUserRequest req) {
		ServiceAdvisor advisor = advisorRepo.findByUserUserId(user.getUserId()).orElse(null);
		if (roleName == Role.RoleName.ADVISOR) {
			if (advisor == null) {
				advisor = new ServiceAdvisor();
				advisor.setUser(user);
			}
			advisor.setSpecialization(trimToNull(req.getSpecialization()));
			advisor.setAvailabilityStatus(advisor.getAvailabilityStatus() != null ? advisor.getAvailabilityStatus()
					: ServiceAdvisor.AvailabilityStatus.AVAILABLE);
			advisor.setOvertimeRate(
					req.getOvertimeRate() != null ? req.getOvertimeRate() : AdvisorService.DEFAULT_OVERTIME_RATE);
			advisorRepo.save(advisor);
			return;
		}
		if (advisor != null) {
			advisor.setSpecialization(null);
			advisor.setAvailabilityStatus(ServiceAdvisor.AvailabilityStatus.RESIGNED);
			advisor.setOvertimeRate(null);
			advisorRepo.save(advisor);
		}
	}

	private void validateName(String fullName) {
		if (fullName == null || fullName.trim().isBlank()) {
			throw new BusinessException("Full name cannot be empty.");
		}
		String normalized = fullName.trim();
		if (normalized.length() < 2) {
			throw new BusinessException("Name must be at least 2 characters.");
		}
		if (!NAME_RE.matcher(normalized).matches()) {
			throw new BusinessException("Name can only contain letters, spaces, hyphens, apostrophes, and dots.");
		}
	}

	private void validatePhone(String phone, Integer editingUserId) {
		if (phone == null || phone.trim().isBlank()) {
			throw new BusinessException("Mobile number cannot be empty.");
		}
		String normalized = phone.trim();
		if (!PHONE_RE.matcher(normalized).matches()) {
			throw new BusinessException("Enter a valid Indian mobile number starting with 6, 7, 8, or 9.");
		}
		boolean exists = editingUserId == null ? userRepo.existsByPhoneAndIsDeletedFalse(normalized)
				: userRepo.existsByPhoneAndUserIdNotAndIsDeletedFalse(normalized, editingUserId);
		if (exists) {
			throw new ConflictException("Mobile number is already registered.");
		}
	}

	private void validatePassword(String rawPassword) {
		if (rawPassword == null || rawPassword.isBlank()) {
			throw new BusinessException("Password is required.");
		}
		if (!PASSWORD_RE.matcher(rawPassword).matches()) {
			throw new BusinessException("Password must include uppercase, lowercase, digit, and special character.");
		}
	}

	private String trimToNull(String value) {
		if (value == null)
			return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		String trimmed = email.trim();
		return trimmed.isEmpty() ? null : trimmed.toLowerCase();
	}
}
