package com.example.cinema.service.notification.impl;

import com.example.cinema.service.notification.INotificationStrategy;
import com.example.cinema.util.QRCodeGenerator;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy cụ thể: Gửi thông báo qua Email với định dạng HTML và mã QR vé.
 */
@Component
public class EmailNotificationStrategy implements INotificationStrategy {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationStrategy.class);

    private final JavaMailSender mailSender;

    public EmailNotificationStrategy(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // Nếu body đã là HTML (bắt đầu bằng <html hoặc <!DOCTYPE) thì gửi trực tiếp
            // Nếu không, bọc trong mẫu khung của StarCinema
            String htmlContent;
            if (body.trim().toLowerCase().startsWith("<html") || body.trim().toLowerCase().startsWith("<!doctype")) {
                htmlContent = body;
            } else {
                htmlContent = wrapInStarCinemaTemplate(subject, body);
            }

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("✅ [EmailStrategy] Đã gửi mail thành công tới: {}", to);
        } catch (Exception e) {
            log.error("❌ [EmailStrategy] Gửi mail thất bại tới {}: ", to, e);
        }
    }

    private String wrapInStarCinemaTemplate(String title, String content) {
        return "<html><body style='font-family: Arial, sans-serif; color: #333;'>"
                + "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; overflow: hidden;'>"
                + "<div style='background: linear-gradient(135deg, #E5133A, #8B0020); padding: 20px; text-align: center;'>"
                + "<h1 style='color: #fff; margin: 0; letter-spacing: 5px;'>STAR CINEMA</h1>"
                + "</div>"
                + "<div style='padding: 30px;'>"
                + "<h2>" + title + "</h2>"
                + "<div style='line-height: 1.6; color: #444;'>" + content + "</div>"
                + "<p style='font-size: 12px; color: #999; margin-top: 30px;'>Đây là email tự động từ hệ thống quản trị StarCinema.</p>"
                + "</div>"
                + "<div style='background: #0F1320; color: #7B82A0; padding: 15px; text-align: center; font-size: 12px;'>"
                + "© 2026 StarCinema - Trải nghiệm điện ảnh đỉnh cao"
                + "</div>"
                + "</div></body></html>";
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }
}
