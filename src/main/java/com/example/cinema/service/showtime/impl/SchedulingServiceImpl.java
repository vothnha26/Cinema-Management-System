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
    public List<ShowtimeResponse> generateAISuggestions(LocalDate targetDate, String mode, double topRatio, LocalTime startTime, LocalTime endTime) {
        LocalTime workingStartTime = (startTime == null) ? LocalTime.of(9, 0) : startTime;
        LocalTime workingEndTime = (endTime == null) ? LocalTime.of(23, 59) : endTime;

        // Tạo bản sao final để dùng trong lambda
        final LocalTime fStartTime = workingStartTime;
        final LocalTime fEndTime = workingEndTime;

        List<Movie> activeMovies = movieRepository.findAll().stream()
                .filter(m -> m.getStatus() == MovieStatus.SHOWING || m.getStatus() == MovieStatus.NOW_SHOWING || m.getStatus() == MovieStatus.PRE_RELEASE)
                .collect(Collectors.toList());

        if (activeMovies.isEmpty()) return new ArrayList<>();

        List<Room> rooms = roomRepository.findAll();
        Map<Long, Double> buzzScores = buzzAnalysisService.getExternalBuzzScores();

        // Lấy tất cả suất chiếu trong ngày
        List<Showtime> existingInDay = showtimeRepository.findAllByStartTimeBetween(
                targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX));

        // Sắp xếp phim theo Buzz
        activeMovies.sort((m1, m2) -> {
            double s1 = (m1.getPriorityLevel() != null ? m1.getPriorityLevel() : 1) * 20 + buzzScores.getOrDefault(m1.getId(), 0.0);
            double s2 = (m2.getPriorityLevel() != null ? m2.getPriorityLevel() : 1) * 20 + buzzScores.getOrDefault(m2.getId(), 0.0);
            return Double.compare(s2, s1);
        });

        int topCount = Math.max(1, (int) Math.ceil(activeMovies.size() * 0.3));
        List<Movie> topMovies = activeMovies.subList(0, Math.min(topCount, activeMovies.size()));
        List<Movie> otherMovies = activeMovies.subList(Math.min(topCount, activeMovies.size()), activeMovies.size());

        List<ShowtimeResponse> suggestions = new ArrayList<>();
        int staggeredOffset = 0;

        for (Room room : rooms) {
            LocalTime currentTime = fStartTime;
            // Tạo độ trễ giữa các phòng để khách không ùa vào cùng lúc
            currentTime = currentTime.plusMinutes(staggeredOffset);
            staggeredOffset = (staggeredOffset + 15) % 45;

            final Long currentRoomId = room.getId();
            // Lấy các suất chiếu cũ của phòng này mà chúng ta chọn GIỮ LẠI
            List<Showtime> keptShowtimes = existingInDay.stream()
                    .filter(s -> s.getRoom().getId().equals(currentRoomId))
                    .filter(s -> {
                        if ("OVERWRITE".equalsIgnoreCase(mode)) {
                            LocalTime sStart = s.getStartTime().toLocalTime();
                            // Nếu ở chế độ ghi đè, chỉ giữ lại những suất nằm NGOÀI khung giờ yêu cầu
                            return sStart.isBefore(fStartTime) || sStart.isAfter(fEndTime);
                        }
                        return true; // Chế độ FILL giữ lại tất cả
                    })
                    .sorted(Comparator.comparing(Showtime::getStartTime))
                    .collect(Collectors.toList());

            while (currentTime.isBefore(fEndTime)) {
                // 1. Kiểm tra xem thời điểm currentTime có đang nằm trong một suất chiếu cũ (được giữ lại) không
                final LocalTime checkTime = currentTime;
                Optional<Showtime> conflict = keptShowtimes.stream()
                        .filter(s -> {
                            LocalTime sStart = s.getStartTime().toLocalTime();
                            LocalTime sEnd = s.getEndTime().toLocalTime().plusMinutes(15); // +15p dọn dẹp
                            return !checkTime.isBefore(sStart) && checkTime.isBefore(sEnd);
                        })
                        .findFirst();

                if (conflict.isPresent()) {
                    // Nếu trùng, nhảy đến sau khi suất đó kết thúc + 15p dọn dẹp
                    currentTime = conflict.get().getEndTime().toLocalTime().plusMinutes(15);
                    continue;
                }

                // 2. Chọn phim để xếp lịch
                Movie movieToSchedule = null;
                boolean shouldPickTop = Math.random() < topRatio || otherMovies.isEmpty();
                List<Movie> pool = shouldPickTop ? topMovies : otherMovies;
                if (pool.isEmpty()) pool = activeMovies;

                for (Movie m : pool) {
                    int duration = m.getDuration();
                    LocalTime expectedEndTime = currentTime.plusMinutes(duration);
                    LocalTime nextStartTimeLimit = expectedEndTime.plusMinutes(15); // Suất tiếp theo phải sau mốc này

                    // Kiểm tra không vượt quá giờ đóng cửa
                    if (expectedEndTime.isAfter(fEndTime) || (expectedEndTime.isBefore(currentTime) && !fEndTime.isBefore(fStartTime))) {
                        continue;
                    }

                    // Kiểm tra xem suất dự kiến này có đè lên bất kỳ suất "đã giữ lại" nào phía sau không
                    final LocalTime startRef = currentTime;
                    final LocalTime endRef = nextStartTimeLimit;
                    boolean willOverlapKept = keptShowtimes.stream()
                            .anyMatch(s -> {
                                LocalTime sStart = s.getStartTime().toLocalTime();
                                return sStart.isAfter(startRef) && sStart.isBefore(endRef);
                            });

                    if (!willOverlapKept) {
                        movieToSchedule = m;
                        break;
                    }
                }

                if (movieToSchedule == null) {
                    currentTime = currentTime.plusMinutes(15); // Không tìm được phim phù hợp, nhích thời gian lên
                    continue;
                }

                // 3. Thêm vào danh sách gợi ý
                ShowtimeResponse res = new ShowtimeResponse();
                res.setId(-1L);
                res.setMovieId(movieToSchedule.getId());
                res.setMovieTitle(movieToSchedule.getTitle());
                res.setMovieDuration(movieToSchedule.getDuration());
                res.setRoomId(room.getId());
                res.setRoomName(room.getName());
                LocalDateTime startDT = LocalDateTime.of(targetDate, currentTime);
                res.setStartTime(startDT);
                res.setEndTime(startDT.plusMinutes(movieToSchedule.getDuration()));

                suggestions.add(res);

                // 4. Tiến thời gian lên: Thời lượng phim + 15 phút dọn dẹp
                currentTime = currentTime.plusMinutes(movieToSchedule.getDuration() + 15);
                
                // Chống lặp vô hạn nếu nhảy qua ngày mới
                if (currentTime.isBefore(fStartTime) && !fEndTime.isBefore(fStartTime)) break;
            }

            // Gộp các suất chiếu cũ đã giữ lại vào danh sách trả về để hiển thị trên UI
            for (Showtime ks : keptShowtimes) {
                ShowtimeResponse kr = new ShowtimeResponse();
                kr.setId(ks.getId());
                kr.setMovieId(ks.getMovie().getId());
                kr.setMovieTitle(ks.getMovie().getTitle());
                kr.setMovieDuration(ks.getMovie().getDuration());
                kr.setRoomId(ks.getRoom().getId());
                kr.setRoomName(ks.getRoom().getName());
                kr.setStartTime(ks.getStartTime());
                kr.setEndTime(ks.getEndTime());
                kr.setTotalSeats(ks.getTotalSeats());
                kr.setSoldSeats(ks.getSoldSeats());
                suggestions.add(kr);
            }
        }

        return suggestions;
    }

    @Override
    @Transactional
    public void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite) {
        if (suggestions.isEmpty()) return;
        
        LocalDate targetDate = suggestions.get(0).getStartTime().toLocalDate();
        
        if (overwrite) {
            // Chỉ xóa các suất chiếu nằm trong Database mà KHÔNG có trong danh sách suggestions hiện tại (đã bao gồm các suất cũ được giữ lại)
            List<Long> idsToKeep = suggestions.stream()
                    .filter(s -> s.getId() != null && s.getId() > 0)
                    .map(ShowtimeResponse::getId)
                    .collect(Collectors.toList());
            
            if (idsToKeep.isEmpty()) {
                showtimeRepository.deleteByStartTimeBetween(
                    targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX));
            } else {
                showtimeRepository.deleteByStartTimeBetweenAndIdNotIn(
                    targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX), idsToKeep);
            }
        }

        for (ShowtimeResponse res : suggestions) {
            if (res.getId() != null && res.getId() > 0) continue;

            Showtime s = new Showtime();
            s.setMovie(movieRepository.getReferenceById(res.getMovieId()));
            s.setRoom(roomRepository.getReferenceById(res.getRoomId()));
            s.setStartTime(res.getStartTime());
            s.setEndTime(res.getEndTime());
            s.setStatus(ShowtimeStatus.UPCOMING);
            s.setTotalSeats(s.getRoom().getCapacity());
            s.setSoldSeats(0);
            showtimeRepository.save(s);
        }
    }
}
