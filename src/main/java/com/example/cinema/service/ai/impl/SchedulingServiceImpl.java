package com.example.cinema.service.ai.impl;

import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.movie.BranchMovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.service.movie.BuzzAnalysisService;
import com.example.cinema.service.ai.SchedulingService;

import com.example.cinema.service.ai.strategy.WeightingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final BranchMovieRepository branchMovieRepository;
    private final com.example.cinema.repository.movie.FormatRepository formatRepository;
    private final BuzzAnalysisService buzzAnalysisService;
    private final List<WeightingStrategy> strategies;

    @Autowired
    public SchedulingServiceImpl(MovieRepository movieRepository,
            RoomRepository roomRepository,
            ShowtimeRepository showtimeRepository,
            BranchMovieRepository branchMovieRepository,
            com.example.cinema.repository.movie.FormatRepository formatRepository,
            BuzzAnalysisService buzzAnalysisService,
            List<WeightingStrategy> strategies) {
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.showtimeRepository = showtimeRepository;
        this.branchMovieRepository = branchMovieRepository;
        this.formatRepository = formatRepository;
        this.buzzAnalysisService = buzzAnalysisService;
        this.strategies = strategies;
    }

    @Override
    public List<ShowtimeResponse> getSuggestions(LocalDate date, String mode) {
        SchedulingRequest request = new SchedulingRequest();
        request.setDate(date);
        request.setMode(mode);
        request.setStrategy("BALANCED");
        return getSuggestions(request);
    }

    @Override
    public List<ShowtimeResponse> getSuggestions(SchedulingRequest request) {
        LocalDate targetDate = request.getDate();
        LocalDateTime workingStartDateTime = targetDate
                .atTime((request.getStartTime() == null) ? LocalTime.of(8, 0) : request.getStartTime());
        LocalDateTime workingEndDateTime = targetDate.plusDays(1).atTime(2, 0);

        double topRatio = (request.getRatio() != null) ? request.getRatio() : 0.7;

        // AI cần biết đang xếp lịch cho rạp nào
        Long branchId = request.getBranchId();

        List<Movie> candidatesPool;
        Map<Long, Integer> branchPriorities = new HashMap<>();

        if (branchId != null) {
            // Lấy danh sách phim đã được phân bổ cho chi nhánh này
            List<BranchMovie> distributed = branchMovieRepository.findByBranchIdAndIsActiveTrue(branchId);
            candidatesPool = distributed.stream().map(BranchMovie::getMovie).collect(Collectors.toList());
            distributed.forEach(bm -> branchPriorities.put(bm.getMovie().getId(), bm.getPriorityLevel()));
        } else {
            // Admin xếp lịch toàn hệ thống (hiếm gặp)
            candidatesPool = movieRepository.findAll();
            candidatesPool.forEach(m -> branchPriorities.put(m.getId(), 1));
        }

        List<Movie> activeMovies = candidatesPool.stream()
                .filter(m -> m.getStatus() == MovieStatus.SHOWING || m.getStatus() == MovieStatus.NOW_SHOWING
                        || m.getStatus() == MovieStatus.PRE_RELEASE)
                .collect(Collectors.toList());

        if (activeMovies.isEmpty())
            return new ArrayList<>();

        // Lọc phòng thuộc chi nhánh
        List<Room> rooms = branchId != null
                ? roomRepository.findAll().stream().filter(r -> r.getBranch().getId().equals(branchId))
                        .collect(Collectors.toList())
                : roomRepository.findAll();

        Map<Long, Double> buzzScores = buzzAnalysisService.getExternalBuzzScores();
        Map<Long, Integer> movieUsageCount = new HashMap<>();
        activeMovies.forEach(m -> movieUsageCount.put(m.getId(), 0));

        List<Showtime> crossDayShowtimes = showtimeRepository.findAllByStartTimeBetween(
                targetDate.minusDays(1).atStartOfDay(), targetDate.plusDays(1).atTime(LocalTime.MAX), branchId);

        List<ShowtimeResponse> suggestions = new ArrayList<>();
        int staggeredOffset = 0;

        for (Room room : rooms) {
            final Long currentRoomId = room.getId();
            final String rType = room.getRoomType() != null ? room.getRoomType().getId() : "HALL_2D";

            LocalDateTime actualStartTime = workingStartDateTime.plusMinutes(staggeredOffset);
            staggeredOffset = (staggeredOffset + 15) % 45;

            Optional<Showtime> lastNightShow = crossDayShowtimes.stream()
                    .filter(s -> s.getRoom().getId().equals(currentRoomId))
                    .filter(s -> s.getEndTime().isAfter(workingStartDateTime))
                    .filter(s -> s.getStartTime().isBefore(workingStartDateTime))
                    .findFirst();

            if (lastNightShow.isPresent()) {
                actualStartTime = lastNightShow.get().getEndTime().plusMinutes(15);
            }

            List<Showtime> keptShowtimes = crossDayShowtimes.stream()
                    .filter(s -> s.getRoom().getId().equals(currentRoomId))
                    .filter(s -> {
                        if ("OVERWRITE".equalsIgnoreCase(request.getMode())) {
                            return s.getStartTime().isBefore(workingStartDateTime)
                                    || s.getStartTime().isAfter(workingEndDateTime);
                        }
                        return true;
                    })
                    .sorted(Comparator.comparing(Showtime::getStartTime))
                    .collect(Collectors.toList());

            LocalDateTime cursor = actualStartTime;

            while (cursor.isBefore(workingEndDateTime)) {
                final LocalDateTime checkTime = cursor;
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

                final LocalTime evalTime = cursor.toLocalTime();
                List<Movie> candidates = activeMovies.stream()
                        .filter(m -> {
                            if (room.getRoomType() == null)
                                return !m.getFormats().isEmpty();
                            Set<Format> supported = room.getRoomType().getSupportedFormats();
                            return supported == null || supported.isEmpty()
                                    || m.getFormats().stream().anyMatch(supported::contains);
                        })
                        .sorted((m1, m2) -> {
                            double w1 = calculateAdvancedWeight(m1, evalTime, buzzScores, request, movieUsageCount,
                                    branchPriorities);
                            double w2 = calculateAdvancedWeight(m2, evalTime, buzzScores, request, movieUsageCount,
                                    branchPriorities);
                            return Double.compare(w2, w1);
                        })
                        .collect(Collectors.toList());

                if (candidates.isEmpty()) {
                    cursor = cursor.plusMinutes(30);
                    continue;
                }

                int topSize = Math.max(1, (int) Math.ceil(candidates.size() * topRatio));
                Movie selectedMovie = candidates.get(new Random().nextInt(topSize));

                int duration = (selectedMovie.getDuration() > 0) ? selectedMovie.getDuration() : 120;
                LocalDateTime expectedEndTime = cursor.plusMinutes(duration);

                if (expectedEndTime.isAfter(workingEndDateTime))
                    break;

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
                    res.setRoomId(room.getId());
                    res.setRoomName(room.getName());
                    res.setRoomType(rType);

                    Set<Format> supported = room.getRoomType() != null ? room.getRoomType().getSupportedFormats()
                            : new HashSet<>();
                    Format selectedFormat = selectedMovie.getFormats().stream()
                            .filter(f -> supported.isEmpty() || supported.contains(f))
                            .max(Comparator.comparing(Format::getId))
                            .orElse(selectedMovie.getFormats().isEmpty() ? null
                                    : selectedMovie.getFormats().iterator().next());

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

            for (Showtime ks : keptShowtimes) {
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

    private double calculateAdvancedWeight(Movie m, LocalTime time, Map<Long, Double> buzz, SchedulingRequest req,
            Map<Long, Integer> usage, Map<Long, Integer> branchPriorities) {
        double totalWeight = 0.0;

        for (WeightingStrategy strategy : strategies) {
            totalWeight += strategy.calculateWeight(m, time, buzz, req, usage, branchPriorities);
        }

        return totalWeight;
    }

    @Override
    @Transactional
    public void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite) {
        if (suggestions.isEmpty())
            return;
        LocalDate targetDate = suggestions.stream().filter(s -> s.getId() == -1L).findFirst()
                .map(s -> s.getStartTime().toLocalDate()).orElse(null);
        if (targetDate == null)
            return;

        if (overwrite) {
            showtimeRepository.deleteByStartTimeBetweenAndSoldSeats(targetDate.atStartOfDay(),
                    targetDate.plusDays(1).atTime(2, 0), 0);
        }

        for (ShowtimeResponse res : suggestions) {
            if (res.getId() != null && res.getId() > 0)
                continue;
            Showtime s = new Showtime();
            s.setMovie(movieRepository.getReferenceById(res.getMovieId()));
            s.setRoom(roomRepository.getReferenceById(res.getRoomId()));
            if (res.getFormatName() != null) {
                movieRepository.findById(res.getMovieId()).ifPresent(m -> {
                    m.getFormats().stream().filter(f -> f.getName().equals(res.getFormatName())).findFirst()
                            .ifPresent(s::setFormat);
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
