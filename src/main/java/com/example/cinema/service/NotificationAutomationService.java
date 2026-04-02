package com.example.cinema.service;

import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.NotificationType;

public interface NotificationAutomationService {
    void notifyUser(User user, String title, String message, NotificationType type);
    void notifyUserOnce(User user, String title, String message, NotificationType type);
    void runPromotionBroadcast();
    void runShowtimeReminders();
}
