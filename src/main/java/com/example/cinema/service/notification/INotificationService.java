package com.example.cinema.service.notification;

import com.example.cinema.model.dto.response.NotificationResponse;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.NotificationType;

import java.util.List;

public interface INotificationService {
    List<NotificationResponse> getMyNotifications();
    long getUnreadCount();
    void markAsRead(Long notificationId);
    void markAllAsRead();
    void sendAndSave(User user, String title, String message, NotificationType type);
}
