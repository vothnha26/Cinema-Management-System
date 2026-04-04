package com.example.cinema.service.showtime.impl;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.dto.request.SchedulingRule;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.*;
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
    public List<ShowtimeResponse> generateAISuggestions(SchedulingRequest request) {
        LocalDate targetDate = request.getDate();
        LocalTime workingStartTime = (request.getStartTime() == null) ? LocalTime.of(9, 0) : request.getStartTime();
        LocalTime workingEndTime = (request.getEndTime() == null) ? LocalTime.of(23, 59) : request.getEndTime();
        double topRatio = (request.getRatio() != null) ? request.getRatio() : 0.7;

        List<Movie> activeMovies = movieRepository.findAll().stream()
                .filter(m -> m.getStatus() == MovieStatus.SHOWING || m.getStatus() == MovieStatus.NOW_SHOWING 
                        || m.getStatus() == MovieStatus.PRE_RELEASE || m.getStatus() == MovieStatus.COMING)
                .collect(Collectors.toList());

        if (activeMovies.isEmpty()) return new ArrayList<>();

        List<Room> rooms = roomRepository.findAll();
        Map<Long, Double> buzzScores = buzzAnalysisService.getExternalBuzzScores();
        
        // Theo dõi số lần mỗi phim được xếp để đảm bảo tính công bằng (Fairness)
        Map<Long, Integer> movieUsageCount = new HashMap<>();
        activeMovies.forEach(m -> movieUsageCount.put(m.getId(), 0));

        List<Showtime> existingInDay = showtimeRepository.findAllByStartTimeBetween(
                targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX));

        List<ShowtimeResponse> suggestions = new ArrayList<>();
        int staggeredOffset = 0;

        for (Room room : rooms) {
            LocalTime currentTime = workingStartTime.plusMinutes(staggeredOffset);
            staggeredOffset = (staggeredOffset + 15) % 45;

            final Long currentRoomId = room.getId();
            final String rType = room.getType().name();

            List<Showtime> keptShowtimes = existingInDay.stream()
                    .filter(s -> s.getRoom().getId().equals(currentRoomId))
                    .filter(s -> {
                        if ("OVERWRITE".equalsIgnoreCase(request.getMode())) {
                            LocalTime sStart = s.getStartTime().toLocalTime();
                            return sStart.isBefore(workingStartTime) || sStart.isAfter(workingEndTime);
                        }
                        return true; 
                    })
                    .sorted(Comparator.comparing(Showtime::getStartTime))
                    .collect(Collectors.toList());

            while (currentTime.isBefore(workingEndTime)) {
                final LocalTime checkTime = currentTime;
                Optional<Showtime> conflict = keptShowtimes.stream()
                        .filter(s -> {
                            LocalTime sStart = s.getStartTime().toLocalTime();
                            LocalTime sEnd = s.getEndTime().toLocalTime().plusMinutes(15);
                            return !checkTime.isBefore(sStart) && checkTime.isBefore(sEnd);
                        }).findFirst();

                if (conflict.isPresent()) {
                    currentTime = conflict.get().getEndTime().toLocalTime().plusMinutes(15);
                    continue;
                }

                // --- HỆ THỐNG TÍNH TRỌNG SỐ (WEIGHTING ENGINE) ---
                final LocalTime evalTime = currentTime;
                List<Movie> candidates = activeMovies.stream()
                    .filter(m -> m.getFormats().stream().anyMatch(f -> f.getName().equals(rType)))
                    .sorted((m1, m2) -> {
                        double w1 = calculateAdvancedWeight(m1, evalTime, buzzScores, request, movieUsageCount);
                        double w2 = calculateAdvancedWeight(m2, evalTime, buzzScores, request, movieUsageCount);
                        return Double.compare(w2, w1);
                    })
                    .collect(Collectors.toList());

                if (candidates.isEmpty()) { currentTime = currentTime.plusMinutes(30); continue; }

                // Chọn phim trong nhóm tinh hoa (Top Tier) dựa trên ratio
                int topSize = Math.max(1, (int) Math.ceil(candidates.size() * topRatio));
                Movie selectedMovie = candidates.get(new Random().nextInt(topSize));

                int duration = (selectedMovie.getDuration() > 0) ? selectedMovie.getDuration() : 120;
                LocalTime expectedEndTime = currentTime.plusMinutes(duration);
                if (expectedEndTime.isAfter(workingEndTime)) break;

                final LocalTime startRef = currentTime;
                final LocalTime endRef = expectedEndTime.plusMinutes(15);
                boolean willOverlap = keptShowtimes.stream().anyMatch(s -> {
                    LocalTime sStart = s.getStartTime().toLocalTime();
                    return sStart.isAfter(startRef) && sStart.isBefore(endRef);
                });

                if (!willOverlap) {
                    ShowtimeResponse res = new ShowtimeResponse();
                    res.setId(-1L);
                    res.setMovieId(selectedMovie.getId());
                    res.setMovieTitle(selectedMovie.getTitle());
                    res.setMovieDuration(selectedMovie.getDuration());
                    res.setRoomId(room.getId());
                    res.setRoomName(room.getName());
                    res.setRoomType(rType);
                    selectedMovie.getFormats().stream().filter(f -> f.getName().equals(rType)).findFirst().ifPresent(f -> res.setFormatName(f.getName()));
                    res.setStartTime(LocalDateTime.of(targetDate, currentTime));
                    res.setEndTime(LocalDateTime.of(targetDate, expectedEndTime));
                    suggestions.add(res);

                    movieUsageCount.put(selectedMovie.getId(), movieUsageCount.get(selectedMovie.getId()) + 1);
                    currentTime = expectedEndTime.plusMinutes(15);
                } else {
                    currentTime = currentTime.plusMinutes(30);
                }
                if (currentTime.isBefore(workingStartTime)) break;
            }

            // Add kept ones
            for (Showtime ks : keptShowtimes) {
                ShowtimeResponse kr = new ShowtimeResponse();
                kr.setId(ks.getId());
                kr.setMovieId(ks.getMovie().getId());
                kr.setMovieTitle(ks.getMovie().getTitle());
                kr.setRoomId(ks.getRoom().getId());
                kr.setRoomName(ks.getRoom().getName());
                kr.setStartTime(ks.getStartTime());
                kr.setEndTime(ks.getEndTime());
                suggestions.add(kr);
            }
        }
        return suggestions;
    }

    private double calculateAdvancedWeight(Movie m, LocalTime time, Map<Long, Double> buzz, SchedulingRequest req, Map<Long, Integer> usage) {
        // 1. Trọng số cơ bản: Buzz + Rating + Priority (Phim nào priority cao hơn sẽ có lợi thế)
        double weight = buzz.getOrDefault(m.getId(), 0.0) + (m.getRating() * 2) + (m.getPriorityLevel() * 10.0);

        // 2. Phạt trọng số dựa trên số lần sử dụng (Fairness)
        // Cứ mỗi suất đã xếp, phim bị giảm 20 điểm trọng số để nhường cho phim khác
        weight -= (usage.getOrDefault(m.getId(), 0) * 20.0);

        // 3. Áp dụng các quy tắc tùy chỉnh (Custom Rules)
        if (req.getRules() != null) {
            for (SchedulingRule rule : req.getRules()) {
                if (!time.isBefore(rule.getStartTime()) && time.isBefore(rule.getEndTime())) {
                    boolean match = false;
                    switch (rule.getPriorityType().toUpperCase()) {
                        case "GENRE":
                            match = m.getGenres().stream().anyMatch(g -> g.getName().equalsIgnoreCase(rule.getTargetValue()));
                            break;
                        case "PRIORITY":
                            match = m.getPriorityLevel() >= Integer.parseInt(rule.getTargetValue());
                            break;
                        case "RATING":
                            match = m.getRating() >= Double.parseDouble(rule.getTargetValue());
                            break;
                        case "MOVIE":
                            match = m.getId().toString().equals(rule.getTargetValue());
                            break;
                    }
                    if (match) weight *= (rule.getWeightBoost() != null ? rule.getWeightBoost() : 2.0);
                }
            }
        }

        // 4. Áp dụng Chiến lược Preset (REVENUE, FAMILY, LATE_NIGHT)
        if ("REVENUE".equalsIgnoreCase(req.getStrategy())) {
            // Tối ưu doanh thu: Phim priority cao và buzz cao được nhân hệ số trong khung giờ vàng
            if (time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(22, 30))) {
                weight *= 1.5;
            }
        } else if ("FAMILY".equalsIgnoreCase(req.getStrategy())) {
            if (time.isBefore(LocalTime.of(14, 0))) {
                boolean isFamily = m.getGenres().stream().anyMatch(g -> g.getName().contains("Hoạt hình") || g.getName().contains("Gia đình"));
                if (isFamily) weight += 100.0;
            }
        } else if ("LATE_NIGHT".equalsIgnoreCase(req.getStrategy())) {
            if (time.isAfter(LocalTime.of(21, 0))) {
                boolean isAction = m.getGenres().stream().anyMatch(g -> g.getName().contains("Hành động") || g.getName().contains("Kinh dị"));
                if (isAction) weight += 100.0;
            }
        }

        return weight;
    }

    @Override
    @Transactional
    public void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite) {
        if (suggestions.isEmpty()) return;
        LocalDate targetDate = suggestions.get(0).getStartTime().toLocalDate();
        if (overwrite) {
            List<Long> idsToKeep = suggestions.stream().filter(s -> s.getId() != null && s.getId() > 0).map(ShowtimeResponse::getId).collect(Collectors.toList());
            if (idsToKeep.isEmpty()) {
                showtimeRepository.deleteByStartTimeBetween(targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX));
            } else {
                showtimeRepository.deleteByStartTimeBetweenAndIdNotIn(targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX), idsToKeep);
            }
        }
        for (ShowtimeResponse res : suggestions) {
            if (res.getId() != null && res.getId() > 0) continue;
            Showtime s = new Showtime();
            s.setMovie(movieRepository.getReferenceById(res.getMovieId()));
            s.setRoom(roomRepository.getReferenceById(res.getRoomId()));
            if (res.getFormatName() != null) {
                movieRepository.findById(res.getMovieId()).ifPresent(m -> {
                    m.getFormats().stream().filter(f -> f.getName().equals(res.getFormatName())).findFirst().ifPresent(s::setFormat);
                });
            }
            s.setStartTime(res.getStartTime());
            s.setEndTime(res.getEndTime());
            s.setStatus(ShowtimeStatus.UPCOMING);
            s.setTotalSeats(s.getRoom().getCapacity());
            s.setSoldSeats(0);
            showtimeRepository.save(s);
        }
    }
}
