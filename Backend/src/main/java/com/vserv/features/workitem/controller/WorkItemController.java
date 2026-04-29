package com.vserv.features.workitem.controller;

import com.vserv.features.workitem.mapper.WorkItemMapper;

import com.vserv.features.workitem.service.WorkItemService;

import com.vserv.core.exception.NotFoundException;
import com.vserv.core.pagination.PaginationUtils;
import com.vserv.entity.WorkItemCatalog;
import com.vserv.features.workitem.dto.CreateWorkItemRequest;
import com.vserv.features.workitem.dto.UpdateWorkItemRequest;
import com.vserv.features.workitem.dto.WorkItemDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-items")
public class WorkItemController {

	private final WorkItemService workItemService;

	public WorkItemController(WorkItemService workItemService) {
		this.workItemService = workItemService;
	}

	/** GET /api/work-items?activeOnly=false */
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<?> listWorkItems(
			@RequestParam(defaultValue = "false") boolean activeOnly,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) WorkItemCatalog.ItemType itemType,
			@RequestParam(required = false) WorkItemCatalog.ItemCarType carType,
			@RequestParam(required = false) String statusFilter,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		List<WorkItemDto> items = (activeOnly ? workItemService.findActive() : workItemService.findAll()).stream()
				.map(WorkItemMapper::toDto).filter(dto -> q == null || q.isBlank() || matchesWorkItemQuery(dto, q))
				.filter(dto -> itemType == null || itemType.name().equalsIgnoreCase(dto.getItemType()))
				.filter(dto -> carType == null || carType.name().equalsIgnoreCase(dto.getCarType()))
				.filter(dto -> statusFilter == null || statusFilter.isBlank()
						|| ("ACTIVE".equalsIgnoreCase(statusFilter) && Boolean.TRUE.equals(dto.getIsActive()))
						|| ("INACTIVE".equalsIgnoreCase(statusFilter) && !Boolean.TRUE.equals(dto.getIsActive())))
				.sorted(workItemComparator(sort)).toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	/** GET /api/work-items/{id} */
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<WorkItemDto> getWorkItemById(@PathVariable Integer id) {
		return ResponseEntity.status(HttpStatus.OK).body(WorkItemMapper
				.toDto(workItemService.findById(id).orElseThrow(() -> new NotFoundException("Work item not found."))));
	}

	/** POST /api/work-items */
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WorkItemDto> createWorkItem(@Valid @RequestBody CreateWorkItemRequest body) {
		WorkItemCatalog w = workItemService.create(body.getItemName(), body.getItemType(), body.getCarType(),
				body.getUnitPrice(), body.getDescription());
		return ResponseEntity.status(HttpStatus.CREATED).body(WorkItemMapper.toDto(w));
	}

	/** PUT /api/work-items/{id} */
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WorkItemDto> updateWorkItem(@PathVariable Integer id,
			@Valid @RequestBody UpdateWorkItemRequest body) {
		WorkItemCatalog w = workItemService.update(id, body.getItemName(), body.getItemType(), body.getCarType(),
				body.getUnitPrice(), body.getDescription());
		return ResponseEntity.status(HttpStatus.OK).body(WorkItemMapper.toDto(w));
	}

	/** PATCH /api/work-items/{id}/toggle */
	@PatchMapping("/{id}/toggle")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> toggle(@PathVariable Integer id) {
		workItemService.toggle(id);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Status toggled."));
	}

	private boolean matchesWorkItemQuery(WorkItemDto dto, String q) {
		String normalized = q == null ? "" : q.trim().toLowerCase();
		if (normalized.isBlank()) {
			return true;
		}
		String haystack = String.join(" ", dto.getItemName() == null ? "" : dto.getItemName(),
				dto.getDescription() == null ? "" : dto.getDescription()).toLowerCase();
		return haystack.contains(normalized);
	}

	private Comparator<WorkItemDto> workItemComparator(String sort) {
		return switch (sort == null ? "newest" : sort) {
		case "oldest" ->
			Comparator.comparing(WorkItemDto::getWorkItemId, Comparator.nullsLast(Comparator.naturalOrder()));
		default -> Comparator.comparing(WorkItemDto::getWorkItemId, Comparator.nullsLast(Comparator.reverseOrder()));
		};
	}
}
