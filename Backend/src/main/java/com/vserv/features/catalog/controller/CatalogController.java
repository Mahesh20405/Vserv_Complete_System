package com.vserv.features.catalog.controller;

import com.vserv.features.catalog.mapper.CatalogMapper;

import com.vserv.features.catalog.service.CatalogService;

import com.vserv.core.exception.NotFoundException;
import com.vserv.core.pagination.PaginationUtils;
import com.vserv.entity.ServiceCatalog;
import com.vserv.features.catalog.dto.CatalogDto;
import com.vserv.features.catalog.dto.CreateCatalogRequest;
import com.vserv.features.catalog.dto.UpdateCatalogRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

	private final CatalogService catalogService;

	public CatalogController(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@GetMapping
	public ResponseEntity<?> listCatalogServices(
			@RequestParam(defaultValue = "false") boolean activeOnly,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) ServiceCatalog.ServiceType serviceType,
			@RequestParam(required = false) ServiceCatalog.CatalogCarType carType,
			@RequestParam(required = false) String statusFilter,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		List<ServiceCatalog> items = activeOnly ? catalogService.findActiveServices()
				: catalogService.findAllServices();
		List<CatalogDto> result = items.stream().map(CatalogMapper::toDto)
				.filter(dto -> q == null || q.isBlank() || matchesCatalogQuery(dto, q))
				.filter(dto -> serviceType == null || serviceType.name().equalsIgnoreCase(dto.getServiceType()))
				.filter(dto -> carType == null || carType.name().equalsIgnoreCase(dto.getCarType()))
				.filter(dto -> statusFilter == null || statusFilter.isBlank()
						|| ("ACTIVE".equalsIgnoreCase(statusFilter) && Boolean.TRUE.equals(dto.getIsActive()))
						|| ("INACTIVE".equalsIgnoreCase(statusFilter) && !Boolean.TRUE.equals(dto.getIsActive())))
				.sorted(catalogComparator(sort)).toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(result, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CatalogDto> getCatalogServiceById(@PathVariable Integer id) {
		return ResponseEntity.status(HttpStatus.OK).body(CatalogMapper.toDto(catalogService.findServiceById(id)
				.orElseThrow(() -> new NotFoundException("Catalog service not found."))));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CatalogDto> createCatalogService(@Valid @RequestBody CreateCatalogRequest body) {
		ServiceCatalog s = catalogService.createService(body.getServiceName(), body.getServiceType(),
				body.getDescription(), body.getBasePrice(), body.getCarType(), body.getDurationHours());
		return ResponseEntity.status(HttpStatus.CREATED).body(CatalogMapper.toDto(s));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CatalogDto> updateCatalogService(@PathVariable Integer id,
			@Valid @RequestBody UpdateCatalogRequest body) {
		return ResponseEntity.status(HttpStatus.OK).body(CatalogMapper.toDto(catalogService.updateService(id, body)));
	}

	@PatchMapping("/{id}/toggle")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> toggle(@PathVariable Integer id) {
		catalogService.toggleService(id);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Status toggled."));
	}

	private boolean matchesCatalogQuery(CatalogDto dto, String q) {
		String normalized = q == null ? "" : q.trim().toLowerCase();
		if (normalized.isBlank()) {
			return true;
		}
		String haystack = String.join(" ", dto.getServiceName() == null ? "" : dto.getServiceName(),
				dto.getDescription() == null ? "" : dto.getDescription()).toLowerCase();
		return haystack.contains(normalized);
	}

	private Comparator<CatalogDto> catalogComparator(String sort) {
		return switch (sort == null ? "newest" : sort) {
		case "oldest" ->
			Comparator.comparing(CatalogDto::getCatalogId, Comparator.nullsLast(Comparator.naturalOrder()));
		default -> Comparator.comparing(CatalogDto::getCatalogId, Comparator.nullsLast(Comparator.reverseOrder()));
		};
	}
}
