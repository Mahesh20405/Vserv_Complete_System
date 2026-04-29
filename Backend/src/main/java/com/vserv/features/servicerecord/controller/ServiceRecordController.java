package com.vserv.features.servicerecord.controller;

import com.vserv.features.servicerecord.mapper.ServiceRecordMapper;

import com.vserv.features.servicerecord.service.ServiceRecordService;

import com.vserv.core.exception.NotFoundException;
import com.vserv.core.pagination.PaginationUtils;
import com.vserv.core.util.SecurityUtils;
import com.vserv.entity.AppUser;
import com.vserv.entity.Invoice;
import com.vserv.entity.ServiceItem;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.servicerecord.dto.CompleteServiceRequest;
import com.vserv.features.servicerecord.dto.SaveItemsRequest;
import com.vserv.features.servicerecord.dto.ServiceItemDto;
import com.vserv.features.servicerecord.dto.ServiceItemRequest;
import com.vserv.features.servicerecord.dto.ServiceRecordDto;
import com.vserv.features.servicerecord.dto.UpdateRemarksRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/service-records")
public class ServiceRecordController {

	private final ServiceRecordService recordService;
	private final SecurityUtils securityUtils;

	public ServiceRecordController(ServiceRecordService recordService, SecurityUtils securityUtils) {
		this.recordService = recordService;
		this.securityUtils = securityUtils;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<?> listServiceRecords(
			@RequestParam(required = false) ServiceRecord.ServiceStatus status,
			@RequestParam(required = false) String q,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		AppUser me = securityUtils.requireCurrentUser();
		List<ServiceRecord> records = securityUtils.isAdmin()
				? (status != null ? recordService.findByStatus(status) : recordService.findAll())
				: (status != null ? recordService.findByAdvisorAndStatus(me, status) : recordService.findByAdvisor(me));
		List<ServiceRecordDto> items = records.stream().map(ServiceRecordMapper::toDto)
				.filter(dto -> matchesServiceRecordQuery(dto, q)).sorted(serviceRecordComparator(sort)).toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<ServiceRecordDto> getServiceRecordById(@PathVariable Integer id) {
		ServiceRecord record = recordService.findById(id)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		securityUtils.requireServiceRecordAccess(record);
		return ResponseEntity.status(HttpStatus.OK).body(ServiceRecordMapper.toDto(record));
	}

	@GetMapping("/{id}/items")
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<List<ServiceItemDto>> getServiceRecordItems(@PathVariable Integer id) {
		ServiceRecord record = recordService.findById(id)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		securityUtils.requireServiceRecordAccess(record);
		return ResponseEntity.status(HttpStatus.OK)
				.body(recordService.getItems(record).stream().map(ServiceRecordMapper::toItemDto).toList());
	}

	@PatchMapping("/{id}/start")
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<Map<String, String>> startServiceRecord(@PathVariable Integer id) {
		ServiceRecord record = recordService.findById(id)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		securityUtils.requireServiceRecordAccess(record);
		recordService.startService(id);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Service started."));
	}

	@PutMapping("/{id}/items")
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<Map<String, String>> saveServiceRecordItems(@PathVariable Integer id,
			@Valid @RequestBody SaveItemsRequest req) {
		ServiceRecord record = recordService.findById(id)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		securityUtils.requireServiceRecordAccess(record);
		List<ServiceItemRequest> entries = req.getItems() != null ? req.getItems() : List.of();
		recordService.saveItems(id, toServiceItems(entries));
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Items saved."));
	}

	@PatchMapping("/{id}/remarks")
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<ServiceRecordDto> updateServiceRecordRemarks(@PathVariable Integer id,
			@Valid @RequestBody UpdateRemarksRequest body) {
		ServiceRecord record = recordService.findById(id)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		securityUtils.requireServiceRecordAccess(record);
		ServiceRecord updated = recordService.updateRemarks(id, body.getRemarks());
		return ResponseEntity.status(HttpStatus.OK).body(ServiceRecordMapper.toDto(updated));
	}

	@PatchMapping("/{id}/complete")
	@PreAuthorize("hasAnyRole('ADMIN','ADVISOR')")
	public ResponseEntity<Map<String, Object>> completeServiceRecord(@PathVariable Integer id,
			@Valid @RequestBody CompleteServiceRequest req) {
		AppUser me = securityUtils.requireCurrentUser();
		ServiceRecord record = recordService.findById(id)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
		securityUtils.requireServiceRecordAccess(record);
		List<ServiceItem> items = req.getItems() != null ? toServiceItems(req.getItems()) : List.of();

		Invoice invoice = recordService.completeService(id, req.getActualHours(), req.getRemarks(),
				items.isEmpty() ? null : items, me);
		return ResponseEntity.status(HttpStatus.OK)
				.body(Map.of("message", "Service completed.", "invoiceId", invoice.getInvoiceId()));
	}

	private List<ServiceItem> toServiceItems(List<ServiceItemRequest> entries) {
		List<ServiceItem> list = new ArrayList<>();
		for (var e : entries) {
			ServiceItem item = new ServiceItem();
			item.setWorkItem(recordService.requireWorkItem(e.getWorkItemId()));
			item.setQuantity(e.getQuantity());
			item.setUnitPrice(e.getUnitPrice());
			list.add(item);
		}
		return list;
	}

	private boolean matchesServiceRecordQuery(ServiceRecordDto dto, String q) {
		String normalized = q == null ? "" : q.trim().toLowerCase();
		if (normalized.isBlank()) {
			return true;
		}
		String haystack = String.join(" ", dto.getOwnerName() == null ? "" : dto.getOwnerName(),
				dto.getVehicleInfo() == null ? "" : dto.getVehicleInfo(),
				dto.getServiceName() == null ? "" : dto.getServiceName(),
				dto.getAdvisorName() == null ? "" : dto.getAdvisorName()).toLowerCase();
		return haystack.contains(normalized);
	}

	private Comparator<ServiceRecordDto> serviceRecordComparator(String sort) {
		Comparator<ServiceRecordDto> byTimeline = Comparator
				.comparing(dto -> dto.getServiceEndDate() != null ? dto.getServiceEndDate()
						: dto.getServiceStartDate() != null ? dto.getServiceStartDate() : java.time.LocalDateTime.MIN);
		return switch (sort == null ? "newest" : sort) {
		case "oldest" -> byTimeline;
		default -> byTimeline.reversed();
		};
	}
}
