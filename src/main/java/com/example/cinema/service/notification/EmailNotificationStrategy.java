package com.example.cinema.service.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Strategy cụ thể: Gửi thông báo qua Email.
 * Implement INotificationStrategy (Strategy Pattern).
 * Phụ thuộc vào JavaMailSender (DIP: inject qua Constructor).
 */
@Component
public class EmailNotificationStrategy implements INotificationStrategy {

    private final JavaMailSender mailSender;

    public EmailNotificationStrategy(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Ghi log lỗi, không để lỗi mail ảnh hưởng nghiệp vụ chính
            System.err.println("[EmailStrategy] Gửi mail thất bại tới " + to + ": " + e.getMessage());
        }
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }
}
