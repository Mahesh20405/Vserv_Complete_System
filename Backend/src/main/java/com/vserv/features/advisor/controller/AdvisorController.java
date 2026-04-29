package com.vserv.features.advisor.controller;

import com.vserv.features.advisor.mapper.AdvisorMapper;

import com.vserv.core.status.StatusToggleGuard;
import com.vserv.core.status.StatusToggleGuardService;
import com.vserv.core.pagination.PaginationUtils;
import com.vserv.features.advisor.service.AdvisorService;

import com.vserv.core.exception.NotFoundException;
import com.vserv.entity.ServiceAdvisor;
import com.vserv.features.advisor.dto.AdvisorDto;
import com.vserv.features.advisor.dto.UpdateAdvisorRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advisors")
public class AdvisorController {

	private final AdvisorService advisorService;
	private final StatusToggleGuardService statusToggleGuardService;

	public AdvisorController(AdvisorService advisorService, StatusToggleGuardService statusToggleGuardService) {
		this.advisorService = advisorService;
		this.statusToggleGuardService = statusToggleGuardService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> listAdvisors(@RequestParam(required = false) String q,
			@RequestParam(required = false) String availabilityStatus,
			@RequestParam(required = false) String specialization,
			@RequestParam(required = false, defaultValue = "load-desc") String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		List<AdvisorDto> items = advisorService.findAll().stream().map(this::toAdvisorDto)
				.filter(dto -> matchesAdvisorQuery(dto, q))
				.filter(dto -> availabilityStatus == null || availabilityStatus.isBlank()
						|| availabilityStatus.equalsIgnoreCase(dto.getAvailabilityStatus()))
				.filter(dto -> specialization == null || specialization.isBlank()
						|| specialization.equalsIgnoreCase(dto.getSpecialization()))
				.sorted(advisorComparator(sort)).toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	@GetMapping("/available")
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<List<AdvisorDto>> listAvailableAdvisors() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(advisorService.findAvailable().stream().map(this::toAdvisorDto).toList());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AdvisorDto> getAdvisorById(@PathVariable Integer id) {
		return ResponseEntity.status(HttpStatus.OK).body(toAdvisorDto(
				advisorService.findById(id).orElseThrow(() -> new NotFoundException("Advisor not found."))));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AdvisorDto> updateAdvisor(@PathVariable Integer id,
			@Valid @RequestBody UpdateAdvisorRequest req) {
		ServiceAdvisor updated = advisorService.update(id, req.getSpecialization(), req.getOvertimeRate(),
				req.getAvailabilityStatus());
		return ResponseEntity.status(HttpStatus.OK).body(toAdvisorDto(updated));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable Integer id) {
		var status = advisorService.toggleUserStatus(id);
		String message = status == com.vserv.entity.AppUser.Status.ACTIVE ? "Advisor activated successfully."
				: "Advisor deactivated successfully.";
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", message, "status", status.name()));
	}

	private boolean matchesAdvisorQuery(AdvisorDto dto, String q) {
		String normalized = q == null ? "" : q.trim().toLowerCase();
		if (normalized.isBlank()) {
			return true;
		}
		String haystack = String.join(" ", dto.getFullName() == null ? "" : dto.getFullName(),
				dto.getEmail() == null ? "" : dto.getEmail(),
				dto.getSpecialization() == null ? "" : dto.getSpecialization()).toLowerCase();
		return haystack.contains(normalized);
	}

	private Comparator<AdvisorDto> advisorComparator(String sort) {
		return switch (sort == null ? "load-desc" : sort) {
		case "load-asc" -> Comparator.comparing(dto -> dto.getCurrentLoad() == null ? 0 : dto.getCurrentLoad());
		default -> Comparator.comparing((AdvisorDto dto) -> dto.getCurrentLoad() == null ? 0 : dto.getCurrentLoad())
				.reversed();
		};
	}

	private AdvisorDto toAdvisorDto(ServiceAdvisor advisor) {
		AdvisorDto dto = AdvisorMapper.toDto(advisor);
		StatusToggleGuard guard = statusToggleGuardService.evaluateAdvisorToggle(advisor);
		dto.setCanToggleStatus(guard.isAllowed());
		dto.setStatusToggleReason(guard.getReason());
		return dto;
	}
}
