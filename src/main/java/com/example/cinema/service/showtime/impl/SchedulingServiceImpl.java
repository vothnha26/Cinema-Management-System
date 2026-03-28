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

            // Lấy danh sách suất chiếu hiện có của PHÒNG này, sắp xếp theo thời gian
            final Long currentRoomId = room.getId();
            List<Showtime> roomExisting = existingShowtimes.stream()
                    .filter(s -> s.getRoom().getId().equals(currentRoomId))
                    .sorted(Comparator.comparing(Showtime::getStartTime))
                    .collect(Collectors.toList());

            while (currentTime.isBefore(LocalTime.of(23, 30))) {
                final LocalTime startTimeFinal = currentTime;
                
                // 1. Kiểm tra xem thời điểm currentTime có đang nằm TRONG suất chiếu nào không
                Optional<Showtime> overlappingShowtime = roomExisting.stream()
                    .filter(s -> {
                        LocalTime sStart = s.getStartTime().toLocalTime();
                        LocalTime sEnd = s.getEndTime().toLocalTime().plusMinutes(15);
                        return !startTimeFinal.isBefore(sStart) && startTimeFinal.isBefore(sEnd);
                    })
                    .findFirst();

                if (overlappingShowtime.isPresent()) {
                    currentTime = overlappingShowtime.get().getEndTime().toLocalTime().plusMinutes(15);
                    continue;
                }

                // 2. Tìm phim phù hợp nhất cho khoảng trống này
                Movie movieToSchedule = null;
                // Thử từng phim trong danh sách đã sắp xếp theo Buzz Score
                for (Movie m : activeMovies) {
                    int durationPlusClean = m.getDuration() + 15;
                    LocalTime expectedEndTime = currentTime.plusMinutes(durationPlusClean);
                    
                    // Kiểm tra xem phim có bị tràn qua ngày hôm sau không (nếu currentTime + duration < currentTime nghĩa là đã qua nửa đêm)
                    boolean wrapsToNextDay = expectedEndTime.isBefore(currentTime);
                    if (wrapsToNextDay || expectedEndTime.isAfter(LocalTime.of(23, 55))) {
                        continue; 
                    }

                    final LocalTime startTimeRef = currentTime;
                    final LocalTime endTimeRef = expectedEndTime;
                    
                    boolean willOverlapNext = roomExisting.stream()
                        .anyMatch(s -> {
                            LocalTime sStart = s.getStartTime().toLocalTime();
                            return endTimeRef.isAfter(sStart) && startTimeRef.isBefore(sStart);
                        });

                    if (!willOverlapNext) {
                        movieToSchedule = m;
                        break; // Tìm thấy phim vừa vặn nhất (ưu tiên cao nhất) thì dừng lại
                    }
                }

                if (movieToSchedule == null) {
                    // Nếu không có phim nào vừa, nhích 15p để tìm khe hở khác
                    currentTime = currentTime.plusMinutes(15);
                    continue;
                }

                // Đủ điều kiện tạo suất mới
                ShowtimeResponse res = new ShowtimeResponse();
                res.setId(-1L); // Đánh dấu là bản thảo AI (ID âm)
                res.setMovieId(movieToSchedule.getId());
                res.setMovieTitle(movieToSchedule.getTitle());
                res.setMovieDuration(movieToSchedule.getDuration());
                res.setRoomId(room.getId());
                res.setRoomName(room.getName());
                
                LocalDateTime start = LocalDateTime.of(targetDate, currentTime);
                res.setStartTime(start);
                res.setEndTime(start.plusMinutes(movieToSchedule.getDuration()));
                
                suggestions.add(res);
                currentTime = currentTime.plusMinutes(movieToSchedule.getDuration() + 15);
            }
        }
        
        // Nếu là FILL, gộp suất chiếu cũ vào (với ID dương) để UI hiển thị
        if (mode != null && mode.equalsIgnoreCase("FILL")) {
            for (Showtime es : existingShowtimes) {
                ShowtimeResponse er = new ShowtimeResponse();
                er.setId(es.getId());
                er.setMovieId(es.getMovie().getId());
                er.setMovieTitle(es.getMovie().getTitle());
                er.setMovieDuration(es.getMovie().getDuration());
                er.setRoomId(es.getRoom().getId());
                er.setRoomName(es.getRoom().getName());
                er.setStartTime(es.getStartTime());
                er.setEndTime(es.getEndTime());
                er.setTotalSeats(es.getTotalSeats());
                er.setSoldSeats(es.getSoldSeats());
                suggestions.add(er);
            }
        }

        return suggestions;
    }

    @Override
    @Transactional
    public void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite) {
        if (overwrite && !suggestions.isEmpty()) {
            LocalDate targetDate = suggestions.get(0).getStartTime().toLocalDate();
            showtimeRepository.deleteByStartTimeBetween(
                targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX));
        }

        for (ShowtimeResponse res : suggestions) {
            // CHỈ LƯU NHỮNG SUẤT CHIẾU MỚI (ID âm hoặc null)
            if (res.getId() != null && res.getId() > 0) {
                continue; 
            }
            
            Showtime s = new Showtime();
            Long movieId = res.getMovieId();
            Long roomId = res.getRoomId();
            
            if (movieId != null && roomId != null) {
                s.setMovie(movieRepository.getReferenceById(movieId));
                Room room = roomRepository.getReferenceById(roomId);
                s.setRoom(room);
                s.setStartTime(res.getStartTime());
                s.setEndTime(res.getEndTime());
                s.setStatus(ShowtimeStatus.UPCOMING);
                s.setTotalSeats(room.getCapacity());
                s.setSoldSeats(0);
                showtimeRepository.save(s);
            }
        }
    }
}
