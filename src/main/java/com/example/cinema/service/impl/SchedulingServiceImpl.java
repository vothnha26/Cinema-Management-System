package com.example.cinema.service.impl;

import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.MovieRepository;
import com.example.cinema.repository.RoomRepository;
import com.example.cinema.repository.ShowtimeRepository;
import com.example.cinema.service.BuzzAnalysisService;
import com.example.cinema.service.SchedulingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulingServiceImpl implements SchedulingService {

    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BuzzAnalysisService buzzAnalysisService;

    public SchedulingServiceImpl(MovieRepository movieRepository, 
                                 RoomRepository roomRepository,
                                 ShowtimeRepository showtimeRepository,
                                 BuzzAnalysisService buzzAnalysisService) {
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.showtimeRepository = showtimeRepository;
        this.buzzAnalysisService = buzzAnalysisService;
    }

    @Override
    public List<ShowtimeResponse> generateAISuggestions(LocalDate targetDate) {
        List<Movie> activeMovies = movieRepository.findAll().stream()
                .filter(m -> m.getStatus() == MovieStatus.NOW_SHOWING || m.getStatus() == MovieStatus.PRE_RELEASE)
                .collect(Collectors.toList());
        
        List<Room> rooms = roomRepository.findAll();
        Map<Long, Double> buzzScores = buzzAnalysisService.getExternalBuzzScores();

        // 1. Tính toán Priority Score cuối cùng
        activeMovies.sort((m1, m2) -> {
            int p1 = m1.getPriorityLevel() != null ? m1.getPriorityLevel() : 1;
            int p2 = m2.getPriorityLevel() != null ? m2.getPriorityLevel() : 1;
            
            double s1 = p1 * 20 + buzzScores.getOrDefault(m1.getId(), 0.0);
            double s2 = p2 * 20 + buzzScores.getOrDefault(m2.getId(), 0.0);
            return Double.compare(s2, s1); // Giảm dần
        });

        // 2. Xác định khung giờ vàng (Prime Time)
        DayOfWeek dow = targetDate.getDayOfWeek();
        LocalTime primeStart = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? LocalTime.of(10, 0) : LocalTime.of(17, 0);
        LocalTime primeEnd = LocalTime.of(23, 0);

        List<ShowtimeResponse> suggestions = new ArrayList<>();
        int staggeredOffset = 0;

        // 3. Thuật toán phân bổ đơn giản (70/30)
        for (Room room : rooms) {
            LocalTime currentTime = LocalTime.of(9, 0); // Bắt đầu từ 9h sáng
            staggeredOffset = (staggeredOffset + 15) % 45; // Lệch giờ giữa các phòng (0, 15, 30p)
            currentTime = currentTime.plusMinutes(staggeredOffset);

            while (currentTime.isBefore(LocalTime.of(23, 30))) {
                Movie movieToSchedule;
                boolean isPrimeTime = !currentTime.isBefore(primeStart) && currentTime.isBefore(primeEnd);

                if (isPrimeTime && Math.random() < 0.7) {
                    movieToSchedule = activeMovies.get(0); // Top 1 phim ưu tiên
                } else {
                    movieToSchedule = activeMovies.get(new Random().nextInt(activeMovies.size()));
                }

                ShowtimeResponse res = new ShowtimeResponse();
                res.setMovieId(movieToSchedule.getId());
                res.setMovieTitle(movieToSchedule.getTitle());
                res.setRoomId(room.getId());
                res.setRoomName(room.getName());
                
                LocalDateTime start = LocalDateTime.of(targetDate, currentTime);
                res.setStartTime(start);
                res.setEndTime(start.plusMinutes(movieToSchedule.getDuration() + 15)); // Gồm dọn dẹp
                
                suggestions.add(res);

                // Nhảy đến slot tiếp theo
                currentTime = currentTime.plusMinutes(movieToSchedule.getDuration() + 30); // 15p dọn dẹp + 15p dãn cách
            }
        }

        return suggestions;
    }

    @Override
    @Transactional
    public void applySuggestions(List<ShowtimeResponse> suggestions) {
        // Logic lưu hàng loạt (Bulk Save) vào DB
        for (ShowtimeResponse res : suggestions) {
            Showtime s = new Showtime();
            s.setMovie(movieRepository.getReferenceById(res.getMovieId()));
            s.setRoom(roomRepository.getReferenceById(res.getRoomId()));
            s.setStartTime(res.getStartTime());
            s.setEndTime(res.getEndTime());
            s.setStatus(ShowtimeStatus.UPCOMING);
            showtimeRepository.save(s);
        }
    }
}
