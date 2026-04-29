package com.vserv.features.user.controller;

import com.vserv.core.status.StatusToggleGuard;
import com.vserv.core.status.StatusToggleGuardService;
import com.vserv.features.user.service.UserService;

import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.NotFoundException;
import com.vserv.core.pagination.PaginationUtils;
import com.vserv.entity.AppUser;
import com.vserv.entity.Role;
import com.vserv.entity.ServiceAdvisor;
import com.vserv.features.user.dto.CreateUserRequest;
import com.vserv.features.user.dto.UpdateUserRequest;
import com.vserv.features.user.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

	private final UserService userService;
	private final StatusToggleGuardService statusToggleGuardService;

	public UserController(UserService userService, StatusToggleGuardService statusToggleGuardService) {
		this.userService = userService;
		this.statusToggleGuardService = statusToggleGuardService;
	}

	@GetMapping
	public ResponseEntity<?> listUsers(@RequestParam(required = false) String q,
			@RequestParam(required = false) Role.RoleName role,
			@RequestParam(required = false) AppUser.Status status,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		Role.RoleName roleName = role;
		List<AppUser> users = (q != null && !q.isBlank())
				? (roleName != null ? userService.searchActiveByRole(q, roleName) : userService.searchActive(q))
				: (roleName != null ? userService.findByRole(roleName) : userService.findAllActive());
		Map<Integer, ServiceAdvisor> advisorsByUserId = userService
				.findAdvisorsByUserIds(users.stream().map(AppUser::getUserId).toList());
		Map<Integer, Long> completedServicesByUserId = userService
				.countCompletedServices(users.stream().map(AppUser::getUserId).toList());
		List<UserDto> items = users.stream().map(u -> {
			UserDto dto = UserDto.from(u, advisorsByUserId.get(u.getUserId()));
			boolean loyaltyEligible = Boolean.TRUE.equals(u.getLoyaltyBadge());
			applyLoyalty(dto, completedServicesByUserId.getOrDefault(u.getUserId(), 0L), loyaltyEligible);
			applyToggleGuard(dto, u);
			return dto;
		}).filter(dto -> status == null || status.name().equalsIgnoreCase(dto.getStatus())).sorted(userComparator(sort))
				.toList();

		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
		AppUser user = userService.findById(id).orElseThrow(() -> new NotFoundException("User not found."));
		UserDto dto = UserDto.from(user, userService.findAdvisorByUserId(id).orElse(null));
		boolean loyaltyEligible = Boolean.TRUE.equals(user.getLoyaltyBadge());
		applyLoyalty(dto, userService.countCompletedServices(id), loyaltyEligible);
		applyToggleGuard(dto, user);
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}

	@PostMapping
	public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest req) {
		if (userService.emailExists(req.getEmail()))
			throw new ConflictException("Email is already registered.");
		if (userService.phoneExists(req.getPhone()))
			throw new ConflictException("Mobile number is already registered.");

		AppUser user = userService.createUser(req.getFullName().trim(), req.getEmail().trim().toLowerCase(),
				req.getPassword(), req.getPhone(), req.getGender(), req.getRoleName(), req.getSpecialization(),
				req.getAvailabilityStatus(), req.getOvertimeRate());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(UserDto.from(user, userService.findAdvisorByUserId(user.getUserId()).orElse(null)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserDto> updateUser(@PathVariable Integer id,
			@Valid @RequestBody UpdateUserRequest body) {
		AppUser updated = userService.updateAdminUser(id, body);
		return ResponseEntity.status(HttpStatus.OK)
				.body(UserDto.from(updated, userService.findAdvisorByUserId(id).orElse(null)));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable Integer id) {
		AppUser.Status status = userService.toggleStatus(id);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message",
				status == AppUser.Status.ACTIVE ? "User activated successfully." : "User deactivated successfully.",
				"status", status.name()));
	}

	private void applyLoyalty(UserDto dto, long completedServices, boolean loyaltyEligible) {
		dto.setCompletedServicesCount(completedServices);
		dto.setLoyaltyEligible(loyaltyEligible);
	}

	private void applyToggleGuard(UserDto dto, AppUser user) {
		StatusToggleGuard guard = statusToggleGuardService.evaluateUserToggle(user);
		dto.setCanToggleStatus(guard.isAllowed());
		dto.setStatusToggleReason(guard.getReason());
	}

	private Comparator<UserDto> userComparator(String sort) {
		return switch (sort == null ? "newest" : sort) {
		case "oldest" -> Comparator.comparing(UserDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
		case "name-az" -> Comparator.comparing(dto -> dto.getFullName() == null ? "" : dto.getFullName(),
				String.CASE_INSENSITIVE_ORDER);
		case "name-za" -> Comparator.comparing((UserDto dto) -> dto.getFullName() == null ? "" : dto.getFullName(),
				String.CASE_INSENSITIVE_ORDER).reversed();
		default -> Comparator.comparing(UserDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
		};
	}
}
