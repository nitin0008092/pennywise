package com.pennywise.service;

import com.pennywise.model.Notification;
import java.util.List;

/**
 * Service contract for financial notifications and rule-based insights.
 */
public interface NotificationService {

    List<Notification> getNotificationsForUser(Long userId);

    List<Notification> getUnreadNotifications(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    List<Notification> generateInsights(Long userId);
}

