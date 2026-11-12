package com.pennywise.repository;

import com.pennywise.model.Notification;
import java.util.List;

/**
 * Repository interface for financial alerts and notifications.
 */
public interface NotificationRepository extends CrudRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findUnreadByUserId(Long userId);

    boolean markAllAsRead(Long userId);

    boolean markAsRead(Long notificationId);
}

