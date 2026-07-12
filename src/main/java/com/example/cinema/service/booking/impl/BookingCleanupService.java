package com.example.cinema.service.booking.impl;

import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.entity.BookingDetail;
import com.example.cinema.model.enums.BookingStatus;
import com.example.cinema.repository.booking.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingCleanupService {

    private static final Logger log = LoggerFactory.getLogger(BookingCleanupService.class);
    private static final String LOCK_KEY_PREFIX = "seat_lock:";

    private final BookingRepository bookingRepository;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public BookingCleanupService(BookingRepository bookingRepository, 
                                 StringRedisTemplate redisTemplate,
                                 SimpMessagingTemplate messagingTemplate) {
        this.bookingRepository = bookingRepository;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Tự động quét dọn đơn hàng quá hạn mỗi phút một lần.
     * Hủy các đơn hàng PENDING đã quá 15 phút.
     */
    @Scheduled(fixedRate = 60000) // 1 phút chạy một lần
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndCreatedAtBefore(
                BookingStatus.PENDING, expiryTime);

        if (expiredBookings.isEmpty()) return;

        log.info("Found {} expired PENDING bookings. Starting cleanup...", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            log.info("Cancelling expired booking: {}", booking.getBookingCode());
            
            // 1. Cập nhật trạng thái thành CANCELLED
            booking.setStatus(BookingStatus.CANCELLED);
            
            // 2. Giải phóng ghế trong Redis và báo qua WebSocket
            if (booking.getShowtime() != null && booking.getDetails() != null) {
                Long showtimeId = booking.getShowtime().getId();
                for (BookingDetail detail : booking.getDetails()) {
                    if (detail.getSeat() != null) {
                        Long seatId = detail.getSeat().getId();
                        
                        // Xóa khóa Redis
                        redisTemplate.delete(LOCK_KEY_PREFIX + showtimeId + ":" + seatId);
                        
                        // Thông báo cho các client khác ghế đã trống
                        broadcastRelease(showtimeId, seatId);
                    }
                }
            }
        }
        
        bookingRepository.saveAll(expiredBookings);
        log.info("Cleanup completed.");
    }

    private void broadcastRelease(Long showtimeId, Long seatId) {
        Map<String, Object> message = new HashMap<>();
        message.put("seatId", seatId);
        message.put("action", "RELEASE");
        message.put("sessionId", "SYSTEM-CLEANUP");
        try {
            messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId + "/seats", message);
        } catch (Exception e) {
            // Log nhưng không làm hỏng quy trình cleanup
        }
    }
}
