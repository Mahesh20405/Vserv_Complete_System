package com.vserv.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Integer notificationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnore
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type")
	private NotificationType notificationType;

	@Column(length = 200)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String message;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "related_booking_id")
	private ServiceBooking relatedBooking;

	@Column(name = "is_read")
	private Boolean isRead = false;

	@Column(name = "sent_at")
	private LocalDateTime sentAt = LocalDateTime.now();

	public Notification() {
	}

	public Integer getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Integer notificationId) {
		this.notificationId = notificationId;
	}

	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser user) {
		this.user = user;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ServiceBooking getRelatedBooking() {
		return relatedBooking;
	}

	public void setRelatedBooking(ServiceBooking relatedBooking) {
		this.relatedBooking = relatedBooking;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	public enum NotificationType {
		BOOKING_CONFIRMATION, SERVICE_REMINDER, STATUS_UPDATE, COMPLETION, PAYMENT_REMINDER
	}
}
