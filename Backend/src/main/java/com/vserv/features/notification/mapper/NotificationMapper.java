package com.vserv.features.notification.mapper;

import com.vserv.entity.Notification;
import com.vserv.features.notification.dto.NotificationDto;

public final class NotificationMapper {
	private NotificationMapper() {
	}

	public static NotificationDto toDto(Notification notification) {
		return NotificationDto.from(notification);
	}
}
