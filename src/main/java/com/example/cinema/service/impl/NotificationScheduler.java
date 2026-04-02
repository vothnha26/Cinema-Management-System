package com.example.cinema.service.impl;

import com.example.cinema.service.NotificationAutomationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private final NotificationAutomationService notificationAutomationService;

    public NotificationScheduler(NotificationAutomationService notificationAutomationService) {
        this.notificationAutomationService = notificationAutomationService;
    }

    @Scheduled(initialDelay = 15000, fixedDelay = 3600000)
    public void broadcastPromotions() {
        notificationAutomationService.runPromotionBroadcast();
    }

    @Scheduled(initialDelay = 20000, fixedDelay = 300000)
    public void sendShowtimeReminders() {
        notificationAutomationService.runShowtimeReminders();
    }
}
