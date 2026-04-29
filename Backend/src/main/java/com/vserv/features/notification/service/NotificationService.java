package com.vserv.features.notification.service;

import com.vserv.entity.AppUser;
import com.vserv.entity.Notification;
import com.vserv.entity.ServiceBooking;

import java.util.List;

public interface NotificationService {
	List<Notification> findByUser(AppUser user);

	void send(AppUser user, Notification.NotificationType type, String title, String message, ServiceBooking booking);

	void markRead(Integer notifId, AppUser user);

	void markAllRead(AppUser user);

	void delete(Integer notifId, AppUser user);
}
