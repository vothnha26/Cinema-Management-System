package com.example.cinema.service.impl;

import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.Notification;
import com.example.cinema.model.entity.Promotion;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.BookingStatus;
import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.repository.BookingRepository;
import com.example.cinema.repository.CustomerRepository;
import com.example.cinema.repository.NotificationRepository;
import com.example.cinema.repository.PromotionRepository;
import com.example.cinema.service.NotificationAutomationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationAutomationServiceImpl implements NotificationAutomationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final PromotionRepository promotionRepository;
    private final BookingRepository bookingRepository;

    public NotificationAutomationServiceImpl(NotificationRepository notificationRepository,
                                             CustomerRepository customerRepository,
                                             PromotionRepository promotionRepository,
                                             BookingRepository bookingRepository) {
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
        this.promotionRepository = promotionRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public void notifyUser(User user, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notifyUserOnce(User user, String title, String message, NotificationType type) {
        if (notificationRepository.existsByUserIdAndTypeAndTitle(user.getId(), type, title)) {
            return;
        }
        notifyUser(user, title, message, type);
    }

    @Override
    @Transactional
    public void runPromotionBroadcast() {
        LocalDate today = LocalDate.now();
        List<Promotion> activePromotions = promotionRepository
                .findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
        if (activePromotions.isEmpty()) {
            return;
        }

        List<Customer> customers = customerRepository.findAll();
        for (Promotion promotion : activePromotions) {
            String title = "Uu dai moi: " + promotion.getCode();
            String message = promotion.getName()
                    + " - dung ma " + promotion.getCode()
                    + " truoc ngay " + promotion.getEndDate()
                    + " de nhan uu dai khi dat ve.";

            for (Customer customer : customers) {
                if (customer.getMembershipTier().ordinal() >= promotion.getMinTier().ordinal()) {
                    notifyUserOnce(customer.getUser(), title, message, NotificationType.PROMOTION);
                }
            }
        }
    }

    @Override
    @Transactional
    public void runShowtimeReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderStart = now.plusMinutes(30);
        LocalDateTime reminderEnd = now.plusMinutes(90);
        List<Booking> upcomingBookings = bookingRepository.findByStatusAndShowtimeStartTimeBetween(
                BookingStatus.CONFIRMED,
                reminderStart,
                reminderEnd);

        for (Booking booking : upcomingBookings) {
            String title = "Nhac lich chieu: " + booking.getBookingCode();
            String message = "Phim " + booking.getShowtime().getMovie().getTitle()
                    + " se bat dau luc " + booking.getShowtime().getStartTime()
                    + ". Vui long den rap som de check-in.";
            notifyUserOnce(booking.getCustomer().getUser(), title, message, NotificationType.REMINDER);
        }
    }
}
