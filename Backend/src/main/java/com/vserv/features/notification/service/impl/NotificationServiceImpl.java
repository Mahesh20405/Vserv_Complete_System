package com.vserv.features.notification.service.impl;

import com.vserv.features.notification.repository.NotificationRepository;

import com.vserv.features.notification.service.NotificationService;

import com.vserv.core.exception.NotFoundException;
import com.vserv.entity.Notification;
import com.vserv.entity.AppUser;
import com.vserv.entity.ServiceBooking;
import com.vserv.features.paymentgateway.service.SmsSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

	private final NotificationRepository notifRepo;
	private final SmsSender smsSender;

	public NotificationServiceImpl(NotificationRepository notifRepo, SmsSender smsSender) {
		this.notifRepo = notifRepo;
		this.smsSender = smsSender;
	}

	public List<Notification> findByUser(AppUser user) {
		return notifRepo.findByUserOrderBySentAtDesc(user);
	}

	@Transactional
	public void send(AppUser user, Notification.NotificationType type, String title, String message,
			ServiceBooking booking) {
		Notification n = new Notification();
		n.setUser(user);
		n.setNotificationType(type);
		n.setTitle(title);
		n.setMessage(message);
		n.setRelatedBooking(booking);
		log.info("Notification sent userId={} type={} title={}", user.getUserId(), type, title);
		notifRepo.save(n);
		sendSmsIfEligible(user, title, message);
	}

	@Transactional
	public void markRead(Integer notifId, AppUser user) {
		Notification notification = notifRepo.findByNotificationIdAndUser(notifId, user)
				.orElseThrow(() -> new NotFoundException("Notification not found."));
		notification.setIsRead(true);
		notifRepo.save(notification);
	}

	@Transactional
	public void markAllRead(AppUser user) {
		notifRepo.markAllReadByUser(user);
	}

	@Transactional
	public void delete(Integer notifId, AppUser user) {
		Notification notification = notifRepo.findByNotificationIdAndUser(notifId, user)
				.orElseThrow(() -> new NotFoundException("Notification not found."));
		notifRepo.delete(notification);
	}

	private void sendSmsIfEligible(AppUser user, String title, String message) {
		if (user == null || user.getRole() == null
				|| user.getRole().getRoleName() != com.vserv.entity.Role.RoleName.CUSTOMER) {
			return;
		}
		if (!StringUtils.hasText(user.getPhone())) {
			return;
		}
		String smsBody = StringUtils.hasText(title) ? title + ": " + message : message;
		smsSender.send(user.getPhone().trim(), smsBody);
	}
}
