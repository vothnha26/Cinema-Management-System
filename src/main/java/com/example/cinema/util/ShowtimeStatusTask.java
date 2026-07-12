package com.example.cinema.util;

import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ShowtimeStatusTask {
    private static final Logger log = LoggerFactory.getLogger(ShowtimeStatusTask.class);

    private final ShowtimeRepository showtimeRepository;

    public ShowtimeStatusTask(ShowtimeRepository showtimeRepository) {
        this.showtimeRepository = showtimeRepository;
    }

    /**
     * Tự động cập nhật trạng thái suất chiếu mỗi phút.
     * UPCOMING -> SHOWING (nếu đã đến giờ bắt đầu)
     * SHOWING -> ENDED (nếu đã quá giờ kết thúc)
     */
    @Scheduled(fixedRate = 60000) // 1 phút chạy 1 lần
    @Transactional
    public void updateShowtimeStatuses() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. Cập nhật thành SHOWING
        List<Showtime> upcomingShows = showtimeRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShowtimeStatus.UPCOMING)
                .filter(s -> s.getStartTime().isBefore(now))
                .toList();
        
        for (Showtime s : upcomingShows) {
            s.setStatus(ShowtimeStatus.SHOWING);
        }

        // 2. Cập nhật thành ENDED
        List<Showtime> activeShows = showtimeRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShowtimeStatus.SHOWING || s.getStatus() == ShowtimeStatus.UPCOMING)
                .filter(s -> s.getEndTime().isBefore(now))
                .toList();

        for (Showtime s : activeShows) {
            s.setStatus(ShowtimeStatus.ENDED);
        }

        if (!upcomingShows.isEmpty() || !activeShows.isEmpty()) {
            showtimeRepository.saveAll(upcomingShows);
            showtimeRepository.saveAll(activeShows);
            log.info("🕒 [Auto-Task] Đã cập nhật trạng thái cho {} suất chiếu.", (upcomingShows.size() + activeShows.size()));
        }
    }
}