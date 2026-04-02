package com.example.cinema.service.notification.impl;

import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.service.notification.TicketBookedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern (Spring EventListener):
 * Lắng nghe TicketBookedEvent và xử lý gửi thông báo.
 * 
 * @Async đảm bảo việc gửi mail không block luồng nghiệp vụ chính.
 * BookingService không cần inject NotificationService => Decoupled.
 */
@Component
public class BookingEventListener {

    private final NotificationService notificationService;

    public BookingEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @EventListener
    public void onTicketBooked(TicketBookedEvent event) {
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
            event.getCustomer().getUsername(),
            event.getMovieTitle(),
            event.getShowtime(),
            event.getSeatInfo(),
            event.getTotalPrice(),
            event.getBookingCode()
        );

        notificationService.sendAndSave(
            event.getCustomer(),
            title,
            body,
            NotificationType.BOOKING
        );
    }
}
