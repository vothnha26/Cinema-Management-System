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
        // Thời gian làm việc mặc định từ 08:00 sáng ngày hiện tại đến 02:00 sáng ngày hôm sau
        LocalDateTime workingStartDateTime = targetDate.atTime((request.getStartTime() == null) ? LocalTime.of(8, 0) : request.getStartTime());
        LocalDateTime workingEndDateTime = targetDate.plusDays(1).atTime(2, 0); // Cho phép xếp lịch xuyên đêm đến 2h sáng hôm sau

        double topRatio = (request.getRatio() != null) ? request.getRatio() : 0.7;

        List<Movie> activeMovies = movieRepository.findAll().stream()
                .filter(m -> m.getStatus() == MovieStatus.SHOWING || m.getStatus() == MovieStatus.NOW_SHOWING 
                        || m.getStatus() == MovieStatus.PRE_RELEASE || m.getStatus() == MovieStatus.COMING)
                .collect(Collectors.toList());

        if (activeMovies.isEmpty()) return new ArrayList<>();

        List<Room> rooms = roomRepository.findAll();
        Map<Long, Double> buzzScores = buzzAnalysisService.getExternalBuzzScores();
        
        Map<Long, Integer> movieUsageCount = new HashMap<>();
        activeMovies.forEach(m -> movieUsageCount.put(m.getId(), 0));

        // Lấy toàn bộ suất chiếu trong khoảng thời gian rộng để kiểm tra xung đột biên
        List<Showtime> crossDayShowtimes = showtimeRepository.findAllByStartTimeBetween(
                targetDate.minusDays(1).atStartOfDay(), targetDate.plusDays(1).atTime(LocalTime.MAX));

        List<ShowtimeResponse> suggestions = new ArrayList<>();
        int staggeredOffset = 0;

        for (Room room : rooms) {
            final Long currentRoomId = room.getId();
            final String rType = room.getType();

            // 1. Xác định thời điểm bắt đầu thực tế cho phòng này (Kiểm tra suất cuối ngày hôm trước)
            LocalDateTime actualStartTime = workingStartDateTime.plusMinutes(staggeredOffset);
            staggeredOffset = (staggeredOffset + 15) % 45;

            Optional<Showtime> lastNightShow = crossDayShowtimes.stream()
                    .filter(s -> s.getRoom().getId().equals(currentRoomId))
                    .filter(s -> s.getEndTime().isAfter(workingStartDateTime))
                    .filter(s -> s.getStartTime().isBefore(workingStartDateTime))
                    .findFirst();

            if (lastNightShow.isPresent()) {
                // Nếu suất hôm trước kết thúc muộn, bắt đầu sau khi dọn dẹp xong
                actualStartTime = lastNightShow.get().getEndTime().plusMinutes(15);
            }

            // Lọc các suất chiếu hiện có cần giữ lại
            List<Showtime> keptShowtimes = crossDayShowtimes.stream()
                    .filter(s -> s.getRoom().getId().equals(currentRoomId))
                    .filter(s -> {
                        if ("OVERWRITE".equalsIgnoreCase(request.getMode())) {
                            // Chỉ xóa các suất nằm TRONG khoảng thời gian AI đang xử lý
                            return s.getStartTime().isBefore(workingStartDateTime) || s.getStartTime().isAfter(workingEndDateTime);
                        }
                        return true; 
                    })
                    .sorted(Comparator.comparing(Showtime::getStartTime))
                    .collect(Collectors.toList());

            LocalDateTime cursor = actualStartTime;

            while (cursor.isBefore(workingEndDateTime)) {
                final LocalDateTime checkTime = cursor;
                
                // Kiểm tra xung đột với lịch cố định
                Optional<Showtime> conflict = keptShowtimes.stream()
                        .filter(s -> {
                            LocalDateTime sStart = s.getStartTime();
                            LocalDateTime sEnd = s.getEndTime().plusMinutes(15);
                            return !checkTime.isBefore(sStart) && checkTime.isBefore(sEnd);
                        }).findFirst();

                if (conflict.isPresent()) {
                    cursor = conflict.get().getEndTime().plusMinutes(15);
                    continue;
                }

                // Tính trọng số phim
                final LocalTime evalTime = cursor.toLocalTime();
                List<Movie> candidates = activeMovies.stream()
                    .filter(m -> {
                        if (room.getRoomType() == null) return !m.getFormats().isEmpty();
                        Set<Format> supported = room.getRoomType().getSupportedFormats();
                        if (supported == null || supported.isEmpty()) return !m.getFormats().isEmpty();
                        
                        // Kiểm tra xem phim có định dạng nào mà phòng hỗ trợ không
                        return m.getFormats().stream().anyMatch(supported::contains);
                    })
                    .sorted((m1, m2) -> {
                        double w1 = calculateAdvancedWeight(m1, evalTime, buzzScores, request, movieUsageCount);
                        double w2 = calculateAdvancedWeight(m2, evalTime, buzzScores, request, movieUsageCount);
                        return Double.compare(w2, w1);
                    })
                    .collect(Collectors.toList());

                if (candidates.isEmpty()) { cursor = cursor.plusMinutes(30); continue; }

                int topSize = Math.max(1, (int) Math.ceil(candidates.size() * topRatio));
                Movie selectedMovie = candidates.get(new Random().nextInt(topSize));

                int duration = (selectedMovie.getDuration() > 0) ? selectedMovie.getDuration() : 120;
                LocalDateTime expectedEndTime = cursor.plusMinutes(duration);
                
                if (expectedEndTime.isAfter(workingEndDateTime)) break;

                // Kiểm tra xem suất mới có đâm vào suất cố định tiếp theo không
                final LocalDateTime startRef = cursor;
                final LocalDateTime endRef = expectedEndTime.plusMinutes(15);
                boolean willOverlap = keptShowtimes.stream().anyMatch(s -> {
                    LocalDateTime sStart = s.getStartTime();
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
                    
                    // Gán định dạng phù hợp nhất cho suất chiếu AI (ưu tiên theo phòng hỗ trợ)
                    Set<Format> supported = room.getRoomType().getSupportedFormats();
                    Format selectedFormat = selectedMovie.getFormats().stream()
                            .filter(f -> supported == null || supported.isEmpty() || supported.contains(f))
                            .max(Comparator.comparing(Format::getId)) // Ưu tiên ID cao (thường là IMAX/4DX)
                            .orElse(selectedMovie.getFormats().isEmpty() ? null : selectedMovie.getFormats().iterator().next());
                    
                    if (selectedFormat != null) {
                        res.setFormatId(selectedFormat.getId());
                        res.setFormatName(selectedFormat.getName());
                    }
                    
                    res.setStartTime(cursor);
                    res.setEndTime(expectedEndTime);
                    suggestions.add(res);

                    movieUsageCount.put(selectedMovie.getId(), movieUsageCount.get(selectedMovie.getId()) + 1);
                    cursor = expectedEndTime.plusMinutes(15);
                } else {
                    cursor = cursor.plusMinutes(30);
                }
            }

            // Thêm các suất cũ đã giữ lại vào kết quả trả về
            for (Showtime ks : keptShowtimes) {
                // Chỉ trả về các suất thuộc ngày đang xét để hiển thị trên UI
                if (ks.getStartTime().toLocalDate().equals(targetDate)) {
                    ShowtimeResponse kr = new ShowtimeResponse();
                    kr.setId(ks.getId());
                    kr.setMovieId(ks.getMovie().getId());
                    kr.setMovieTitle(ks.getMovie().getTitle());
                    kr.setRoomId(ks.getRoom().getId());
                    kr.setRoomName(ks.getRoom().getName());
                    if (ks.getFormat() != null) {
                        kr.setFormatId(ks.getFormat().getId());
                        kr.setFormatName(ks.getFormat().getName());
                    }
                    kr.setStartTime(ks.getStartTime());
                    kr.setEndTime(ks.getEndTime());
                    kr.setTotalSeats(ks.getTotalSeats());
                    kr.setSoldSeats(ks.getSoldSeats());
                    suggestions.add(kr);
                }
            }
        }
        return suggestions;
    }

    private double calculateAdvancedWeight(Movie m, LocalTime time, Map<Long, Double> buzz, SchedulingRequest req, Map<Long, Integer> usage) {
        double weight = buzz.getOrDefault(m.getId(), 0.0) + (m.getRating() * 2) + (m.getPriorityLevel() * 10.0);
        weight -= (usage.getOrDefault(m.getId(), 0) * 20.0);

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

        if ("REVENUE".equalsIgnoreCase(req.getStrategy())) {
            if (time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(22, 30))) weight *= 1.5;
        } else if ("FAMILY".equalsIgnoreCase(req.getStrategy())) {
            if (time.isBefore(LocalTime.of(14, 0))) {
                if (m.getGenres().stream().anyMatch(g -> g.getName().contains("Hoạt hình") || g.getName().contains("Gia đình"))) weight += 100.0;
            }
        } else if ("LATE_NIGHT".equalsIgnoreCase(req.getStrategy())) {
            if (time.isAfter(LocalTime.of(21, 0))) {
                if (m.getGenres().stream().anyMatch(g -> g.getName().contains("Hành động") || g.getName().contains("Kinh dị"))) weight += 100.0;
            }
        }

        // AI PROMPT PARSING (Xử lý lời nhắn từ Manager)
        if (req.getCustomDirectives() != null && !req.getCustomDirectives().isBlank()) {
            String prompt = req.getCustomDirectives().toLowerCase();
            
            // 1. Phân tích khung giờ từ Prompt
            boolean isNightRef = prompt.contains("tối") || prompt.contains("khuya") || prompt.contains("đêm");
            boolean isMorningRef = prompt.contains("sáng") || prompt.contains("trưa");
            
            // 2. Phân tích thể loại ưu tiên
            for (Genre g : m.getGenres()) {
                String gName = g.getName().toLowerCase();
                if (prompt.contains(gName)) {
                    // Nếu đúng khung giờ người dùng nhắc tới
                    if ((isNightRef && time.isAfter(LocalTime.of(18, 0))) || 
                        (isMorningRef && time.isBefore(LocalTime.of(14, 0))) ||
                        (!isNightRef && !isMorningRef)) {
                        weight *= 2.5; // Ưu tiên cực cao (Boost)
                    }
                }
            }

            // 3. Phân tích tên phim cụ thể
            if (prompt.contains(m.getTitle().toLowerCase())) {
                weight *= 3.0; // Ưu tiên tuyệt đối phim được nhắc tên
            }
            
            // 4. Các chỉ thị đặc biệt
            if (prompt.contains("phim mới") && m.getStatus() == MovieStatus.PRE_RELEASE) weight *= 2.0;
            if (prompt.contains("rating") && m.getRating() >= 8.5) weight *= 1.5;
        }

        return weight;
    }

    @Override
    @Transactional
    public void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite) {
        if (suggestions.isEmpty()) return;
        LocalDate targetDate = suggestions.stream().filter(s -> s.getId() == -1L).findFirst().map(s -> s.getStartTime().toLocalDate()).orElse(null);
        if (targetDate == null) return;

        if (overwrite) {
            // Xóa lịch cũ nhưng giữ lại các suất đã có vé bán (Elite Safety)
            showtimeRepository.deleteByStartTimeBetweenAndSoldSeats(targetDate.atStartOfDay(), targetDate.plusDays(1).atTime(2, 0), 0);
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
