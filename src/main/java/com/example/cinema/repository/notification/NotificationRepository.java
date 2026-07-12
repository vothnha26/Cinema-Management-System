package com.example.cinema.repository.notification;

import com.example.cinema.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUsernameOrderByCreatedAtDesc(String username);
    long countByUserUsernameAndIsReadFalse(String username);
}
