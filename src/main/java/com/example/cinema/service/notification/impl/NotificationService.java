package com.example.cinema.service.notification.impl;

import com.example.cinema.model.entity.Notification;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.repository.user.NotificationRepository;
import com.example.cinema.service.notification.INotificationStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service trung tâm quản lý Notification (SRP).
 * Chịu trách nhiệm:
 *   1. Lưu notification vào DB
 *   2. Dispatch gửi ra kênh bên ngoài thông qua INotificationStrategy (DIP)
 *
 * Không chứa logic nghiệp vụ Booking hay Payment.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final List<INotificationStrategy> strategies;

    /**
     * Spring tự động inject TẤT CẢ các bean implement INotificationStrategy.
     * Khi thêm SmsNotificationStrategy sau này, chỉ cần @Component là được.
     */
    public NotificationService(NotificationRepository notificationRepository,
                               List<INotificationStrategy> strategies) {
        this.notificationRepository = notificationRepository;
        this.strategies = strategies;
    }

    /**
     * Lưu thông báo vào database và gửi qua tất cả các kênh đã đăng ký.
     */
    public void sendAndSave(User user, String title, String message, NotificationType type) {
        // 1. Lưu vào DB
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notificationRepository.save(notification);

        // 2. Dispatch tới tất cả Strategy (hiện tại chỉ có Email)
        for (INotificationStrategy strategy : strategies) {
            strategy.send(user.getEmail(), title, message);
        }
    }

    /**
     * Lấy danh sách notifications của user.
     */
    public List<Notification> getByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Đánh dấu notification là đã đọc.
     */
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
