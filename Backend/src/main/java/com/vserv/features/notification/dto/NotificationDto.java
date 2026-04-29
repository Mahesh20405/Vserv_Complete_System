package com.vserv.features.notification.dto;

import com.vserv.entity.Notification;
import java.time.LocalDateTime;

public class NotificationDto {
	private Integer notificationId;
	private String notificationType;
	private String title;
	private String message;
	private Boolean isRead;
	private LocalDateTime sentAt;
	private Integer relatedBookingId;

	public static NotificationDto from(Notification n) {
		NotificationDto dto = new NotificationDto();
		dto.notificationId = n.getNotificationId();
		dto.notificationType = n.getNotificationType() != null ? n.getNotificationType().name() : null;
		dto.title = n.getTitle();
		dto.message = n.getMessage();
		dto.isRead = n.getIsRead();
		dto.sentAt = n.getSentAt();
		if (n.getRelatedBooking() != null)
			dto.relatedBookingId = n.getRelatedBooking().getBookingId();
		return dto;
	}

	public Integer getNotificationId() {
		return notificationId;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public Integer getRelatedBookingId() {
		return relatedBookingId;
	}
}
