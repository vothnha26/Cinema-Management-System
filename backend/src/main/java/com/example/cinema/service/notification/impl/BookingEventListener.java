package com.example.cinema.service.notification.impl;

import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.service.notification.INotificationService;
import com.example.cinema.service.notification.TicketBookedEvent;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern (Spring EventListener):
 * Lắng nghe TicketBookedEvent và xử lý gửi thông báo.
 * 
 * @Async đảm bảo việc gửi mail không block luồng nghiệp vụ chính.
 *        BookingService không cần inject NotificationService => Decoupled.
 */
@Component
public class BookingEventListener {

    private final INotificationService notificationService;
    private final com.example.cinema.repository.user.UserRepository userRepository;

    public BookingEventListener(INotificationService notificationService, 
                                com.example.cinema.repository.user.UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Async
    @EventListener
    public void onTicketBooked(TicketBookedEvent event) {
        // Nạp lại User trong thread mới để tránh lỗi detached entity
        com.example.cinema.model.entity.User attachedUser = userRepository.findById(event.getCustomer().getId())
                .orElse(event.getCustomer());

        String title = "Xác nhận đặt vé - " + event.getBookingCode();
        String body = String.format(
                "Xin chào %s,\n\n" +
                        "Bạn đã đặt vé thành công!\n" +
                        "📽 Phim: %s\n" +
                        "🕐 Suất chiếu: %s\n" +
                        "💺 Ghế: %s\n" +
                        "💰 Tổng tiền: %s\n" +
                        "🎫 Mã đặt vé: %s\n\n" +
                        "Vui lòng đưa mã đặt vé này tại quầy để check-in.\n" +
                        "Chúc bạn xem phim vui vẻ! 🎬\n\n" +
                        "— StarCinema",
                attachedUser.getUsername(),
                event.getMovieTitle(),
                event.getShowtime(),
                event.getSeatInfo(),
                event.getTotalPrice(),
                event.getBookingCode());

        notificationService.sendAndSave(
                attachedUser,
                title,
                body,
                NotificationType.BOOKING);
    }
}
