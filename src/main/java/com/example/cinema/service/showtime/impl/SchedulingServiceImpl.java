package com.example.cinema.service.showtime.impl;

import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.service.movie.BuzzAnalysisService;
import com.example.cinema.service.showtime.SchedulingService;
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
    public List<ShowtimeResponse> generateAISuggestions(LocalDate targetDate, String mode) {
        List<Movie> activeMovies = movieRepository.findAll().stream()
                .filter(m -> m.getStatus() == MovieStatus.NOW_SHOWING || m.getStatus() == MovieStatus.PRE_RELEASE)
                .collect(Collectors.toList());
        
        List<Room> rooms = roomRepository.findAll();
        Map<Long, Double> buzzScores = buzzAnalysisService.getExternalBuzzScores();

        // 1. Lấy suất chiếu hiện có nếu là chế độ lấp chỗ trống
        List<Showtime> existingShowtimes = (mode != null && mode.equalsIgnoreCase("FILL")) 
                ? showtimeRepository.findAllByStartTimeBetween(
                    targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX))
                : new ArrayList<>();

        // Sort movies by Priority + Buzz
        activeMovies.sort((m1, m2) -> {
            int p1 = m1.getPriorityLevel() != null ? m1.getPriorityLevel() : 1;
            int p2 = m2.getPriorityLevel() != null ? m2.getPriorityLevel() : 1;
            double s1 = p1 * 20 + buzzScores.getOrDefault(m1.getId(), 0.0);
            double s2 = p2 * 20 + buzzScores.getOrDefault(m2.getId(), 0.0);
            return Double.compare(s2, s1);
        });

        DayOfWeek dow = targetDate.getDayOfWeek();
        LocalTime primeStart = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? LocalTime.of(10, 0) : LocalTime.of(17, 0);
        LocalTime primeEnd = LocalTime.of(23, 0);

        List<ShowtimeResponse> suggestions = new ArrayList<>();
        int staggeredOffset = 0;

        for (Room room : rooms) {
            LocalTime currentTime = LocalTime.of(9, 0);
            staggeredOffset = (staggeredOffset + 15) % 45;
            currentTime = currentTime.plusMinutes(staggeredOffset);

            while (currentTime.isBefore(LocalTime.of(23, 30))) {
                final LocalTime startTimeFinal = currentTime;
                
                // Nếu là FILL, kiểm tra xem có bị trùng với suất chiếu hiện có không
                if (mode != null && mode.equalsIgnoreCase("FILL")) {
                    boolean isOverlap = existingShowtimes.stream()
                        .filter(s -> s.getRoom().getId().equals(room.getId()))
                        .anyMatch(s -> {
                            LocalTime sStart = s.getStartTime().toLocalTime();
                            LocalTime sEnd = s.getEndTime().toLocalTime();
                            return startTimeFinal.isBefore(sEnd) && startTimeFinal.plusMinutes(120).isAfter(sStart);
                        });
                    
                    if (isOverlap) {
                        currentTime = currentTime.plusMinutes(30); // Nhảy 30p để tìm chỗ trống tiếp theo
                        continue;
                    }
                }

                Movie movieToSchedule;
                boolean isPrimeTime = !currentTime.isBefore(primeStart) && currentTime.isBefore(primeEnd);
                if (isPrimeTime && Math.random() < 0.7) {
                    movieToSchedule = activeMovies.get(0);
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
                res.setEndTime(start.plusMinutes(movieToSchedule.getDuration() + 15));
                
                suggestions.add(res);
                currentTime = currentTime.plusMinutes(movieToSchedule.getDuration() + 30);
            }
        }
        return suggestions;
    }

    @Override
    @Transactional
    public void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite) {
        if (overwrite && !suggestions.isEmpty()) {
            LocalDate targetDate = suggestions.get(0).getStartTime().toLocalDate();
            // Xóa suất chiếu cũ của ngày đó (Chỉ xóa những suất chưa có vé - Giả định logic cơ bản)
            showtimeRepository.deleteByStartTimeBetween(
                targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX));
        }

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
