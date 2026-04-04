package com.example.cinema.service.notification.impl;

import com.example.cinema.service.notification.INotificationStrategy;
import com.example.cinema.util.QRCodeGenerator;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Strategy cụ thể: Gửi thông báo qua Email với định dạng HTML và mã QR vé.
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
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // Tách mã đơn hàng từ body nếu có (Body hiện tại truyền bookingCode)
            String bookingCode = body;
            String qrBase64 = QRCodeGenerator.generateQRCodeBase64(bookingCode, 200, 200);

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; overflow: hidden;'>"
                    + "<div style='background: linear-gradient(135deg, #E5133A, #8B0020); padding: 20px; text-align: center;'>"
                    + "<h1 style='color: #fff; margin: 0; letter-spacing: 5px;'>STAR CINEMA</h1>"
                    + "</div>"
                    + "<div style='padding: 30px; text-align: center;'>"
                    + "<h2>Cảm ơn bạn đã đặt vé!</h2>"
                    + "<p>Mã đặt vé của bạn là:</p>"
                    + "<div style='background: #f8f9fa; border: 2px dashed #E5133A; padding: 15px; display: inline-block; font-size: 24px; font-weight: bold; color: #E5133A; margin: 10px 0;'>"
                    + bookingCode + "</div>"
                    + "<p style='color: #666;'>Vui lòng đưa mã QR dưới đây cho nhân viên tại rạp để nhận vé.</p>"
                    + "<img src='data:image/png;base64," + qrBase64 + "' style='width: 200px; height: 200px; margin: 20px 0; border: 1px solid #eee;' />"
                    + "<p style='font-size: 12px; color: #999;'>Đây là email tự động, vui lòng không phản hồi.</p>"
                    + "</div>"
                    + "<div style='background: #0F1320; color: #7B82A0; padding: 15px; text-align: center; font-size: 12px;'>"
                    + "© 2026 StarCinema - Trải nghiệm điện ảnh đỉnh cao"
                    + "</div>"
                    + "</div></body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            System.out.println("✅ [EmailStrategy] Đã gửi mail thành công tới: " + to);
        } catch (Exception e) {
            System.err.println("❌ [EmailStrategy] Gửi mail thất bại tới " + to + ": " + e.getMessage());
        }
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }
}
