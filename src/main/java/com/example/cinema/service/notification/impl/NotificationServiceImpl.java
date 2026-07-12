package com.example.cinema.service.notification.impl;

import com.example.cinema.model.dto.response.NotificationResponse;
import com.example.cinema.model.entity.Notification;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.repository.notification.NotificationRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.notification.INotificationAutomationService;
import com.example.cinema.service.notification.INotificationService;
import com.example.cinema.service.notification.INotificationStrategy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements INotificationService, INotificationAutomationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final Map<String, INotificationStrategy> strategies;

    public NotificationServiceImpl(NotificationRepository notificationRepository, 
                                   UserRepository userRepository,
                                   List<INotificationStrategy> strategyList) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        // Chuyển danh sách strategy thành Map để truy xuất nhanh qua tên channel (OCP)
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(INotificationStrategy::getChannel, s -> s));
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificationRepository.findByUserUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificationRepository.countByUserUsernameAndIsReadFalse(username);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Notification> unread = notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getUsername().equals(username) && !n.getIsRead())
                .collect(Collectors.toList());
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void sendAndSave(User user, String title, String message, NotificationType type) {
        if (user == null || user.getId() == null) {
            log.info("Skipping notification save: No registered user found (Guest customer).");
            return;
        }
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // Mặc định nếu là thông báo hệ thống/đặt vé thì gửi thêm Email nếu User có email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sendNotification(user.getEmail(), title, message, "EMAIL");
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendNotification(String to, String subject, String body, String channel) {
        INotificationStrategy strategy = strategies.get(channel.toUpperCase());
        if (strategy != null) {
            strategy.send(to, subject, body);
        } else {
            log.error("❌ Không tìm thấy strategy cho kênh: {}", channel);
        }
    }

    // --- Implementation of INotificationAutomationService ---

    @Override
    @Transactional
    public void notifyUser(User user, String title, String message, NotificationType type) {
        sendAndSave(user, title, message, type);
    }

    @Override
    @Transactional
    public void notifyUserOnce(User user, String title, String message, NotificationType type) {
        // Simple logic: check if same notification title exists for user
        boolean exists = notificationRepository.findAll().stream()
                .anyMatch(n -> n.getUser().getId().equals(user.getId()) && n.getTitle().equals(title));
        if (!exists) {
            notifyUser(user, title, message, type);
        }
    }

    @Override
    public void runPromotionBroadcast() {
        // Placeholder for scheduled task logic
    }

    @Override
    public void runShowtimeReminders() {
        // Placeholder for scheduled task logic
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
