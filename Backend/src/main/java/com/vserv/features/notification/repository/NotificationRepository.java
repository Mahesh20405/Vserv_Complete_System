package com.vserv.features.notification.repository;

import com.vserv.entity.AppUser;
import com.vserv.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
	List<Notification> findByUserOrderBySentAtDesc(AppUser user);

	Optional<Notification> findByNotificationIdAndUser(Integer notificationId, AppUser user);

	@Modifying
	@Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
	int markAllReadByUser(@Param("user") AppUser user);
}
