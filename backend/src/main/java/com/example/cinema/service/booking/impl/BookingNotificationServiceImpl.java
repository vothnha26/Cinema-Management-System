package com.example.cinema.service.booking.impl;

import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.Notification;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.repository.notification.NotificationRepository;
import com.example.cinema.service.booking.IBookingNotificationService;
import com.example.cinema.service.notification.impl.EmailNotificationStrategy;
import com.example.cinema.service.user.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookingNotificationServiceImpl implements IBookingNotificationService {
    private static final Logger log = LoggerFactory.getLogger(BookingNotificationServiceImpl.class);

    private final EmailNotificationStrategy emailService;
    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;

    public BookingNotificationServiceImpl(EmailNotificationStrategy emailService,
                                         NotificationRepository notificationRepository,
                                         CustomerService customerService) {
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
    }

    @Override
    public void sendBookingConfirmation(Booking booking) {
        Customer c = booking.getCustomer();
        Showtime s = booking.getShowtime();

        // 1. Gửi Email
        try {
            String targetEmail = (c.getUser() != null) ? c.getUser().getEmail() : c.getEmail();
            if (targetEmail != null && !targetEmail.isEmpty()) {
                String content = String.format("<p>Mã đặt vé: <b>%s</b></p><p>Phim: %s</p><p>Suất chiếu: %s</p><p>Tổng tiền: %,.0fđ</p>",
                        booking.getBookingCode(), s.getMovie().getTitle(), s.getStartTime(), booking.getTotalPrice());
                emailService.send(targetEmail, "XÁC NHẬN ĐẶT VÉ THÀNH CÔNG - " + booking.getBookingCode(), content);
            }
        } catch (Exception e) {
            log.error("Lỗi gửi Email: {}", e.getMessage());
        }

        // 2. Thông báo trong App & Cộng điểm
        if (c.getUser() != null) {
            customerService.addLoyaltyPoints(c, booking.getTotalPrice());
            createNotification(c.getUser().getId(), "Thanh toán thành công", "Mã đơn hàng: " + booking.getBookingCode(), NotificationType.BOOKING);
        }
    }

    private void createNotification(Long userId, String title, String msg, NotificationType type) {
        Notification n = new Notification();
        com.example.cinema.model.entity.User u = new com.example.cinema.model.entity.User();
        u.setId(userId);
        n.setUser(u);
        n.setTitle(title);
        n.setMessage(msg);
        n.setType(type);
        notificationRepository.save(n);
    }
}
