package com.vserv.features.notification.controller;

import com.vserv.features.notification.mapper.NotificationMapper;

import com.vserv.core.pagination.PaginationUtils;
import com.vserv.features.notification.service.NotificationService;

import com.vserv.features.notification.dto.NotificationDto;
import com.vserv.entity.AppUser;
import com.vserv.core.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;
	private final SecurityUtils securityUtils;

	public NotificationController(NotificationService notificationService, SecurityUtils securityUtils) {
		this.notificationService = notificationService;
		this.securityUtils = securityUtils;
	}

	/** GET /api/notifications */
	@GetMapping
	public ResponseEntity<?> listNotifications(
			@RequestParam(required = false, defaultValue = "all") String filter,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		AppUser me = securityUtils.requireCurrentUser();
		List<NotificationDto> items = notificationService.findByUser(me).stream().map(NotificationMapper::toDto)
				.filter(dto -> matchesNotificationFilter(dto, filter)).sorted(Comparator
						.comparing(NotificationDto::getSentAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	/** PATCH /api/notifications/{id}/read */
	@PatchMapping("/{id}/read")
	public ResponseEntity<Map<String, String>> markRead(@PathVariable Integer id) {
		AppUser me = securityUtils.requireCurrentUser();
		notificationService.markRead(id, me);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Marked as read."));
	}

	/** PATCH /api/notifications/read-all */
	@PatchMapping("/read-all")
	public ResponseEntity<Map<String, String>> markAllRead() {
		AppUser me = securityUtils.requireCurrentUser();
		notificationService.markAllRead(me);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "All notifications marked as read."));
	}

	/** DELETE /api/notifications/{id} */
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Integer id) {
		AppUser me = securityUtils.requireCurrentUser();
		notificationService.delete(id, me);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Notification deleted successfully."));
	}

	private boolean matchesNotificationFilter(NotificationDto dto, String filter) {
		if (filter == null || filter.isBlank() || "all".equalsIgnoreCase(filter)) {
			return true;
		}
		if ("unread".equalsIgnoreCase(filter)) {
			return !Boolean.TRUE.equals(dto.getIsRead());
		}
		return filter.equalsIgnoreCase(dto.getNotificationType() == null ? "" : dto.getNotificationType());
	}
}
