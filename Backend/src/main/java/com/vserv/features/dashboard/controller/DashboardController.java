package com.vserv.features.dashboard.controller;

import com.vserv.features.dashboard.service.DashboardService;

import com.vserv.features.dashboard.dto.DashboardStatsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	/** GET /api/dashboard */
	@GetMapping
	public ResponseEntity<DashboardStatsDto> getDashboardStats() {
		return ResponseEntity.status(HttpStatus.OK).body(dashboardService.getStats());
	}
}
