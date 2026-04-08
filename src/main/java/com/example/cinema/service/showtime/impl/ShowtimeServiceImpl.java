package com.example.cinema.service.showtime.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.SeatResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.dto.response.PriceCalculationResult;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.booking.BookingDetailRepository;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.service.showtime.ShowtimeService;
import com.example.cinema.service.commerce.PricingService;
import com.example.cinema.service.commerce.impl.PricingServiceImpl;
import com.example.cinema.service.commerce.pricing.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final PricingService pricingService;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final com.example.cinema.repository.movie.FormatRepository formatRepository;
    private final com.example.cinema.repository.movie.BranchMovieRepository branchMovieRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "seat_lock:";

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository,
            SeatRepository seatRepository,
            BookingDetailRepository bookingDetailRepository,
            PricingService pricingService,
            MovieRepository movieRepository,
            RoomRepository roomRepository,
            com.example.cinema.repository.movie.FormatRepository formatRepository,
            com.example.cinema.repository.movie.BranchMovieRepository branchMovieRepository,
            StringRedisTemplate redisTemplate) {
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.pricingService = pricingService;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.formatRepository = formatRepository;
        this.branchMovieRepository = branchMovieRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ShowtimeResponse> getAllShowtimes(LocalDate date) {
        List<Showtime> showtimes;
        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(java.time.LocalTime.MAX);
            showtimes = showtimeRepository.findAllByStartTimeBetween(startOfDay, endOfDay, null);
        } else {
            showtimes = showtimeRepository.findAll();
        }
        return showtimes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowtimeResponse> getShowtimesByBranch(Long branchId, LocalDate date) {
        List<Showtime> showtimes;
        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(java.time.LocalTime.MAX);
            showtimes = showtimeRepository.findByBranchIdAndStartTimeBetween(branchId, startOfDay, endOfDay);
        } else {
            showtimes = showtimeRepository.findByBranchId(branchId);
        }
        return showtimes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowtimeResponse> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findByMovieId(movieId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatResponse> getSeatStatusForShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new AppException("Suất chiếu không tồn tại"));

        List<Seat> allSeats = seatRepository.findByRoomId(showtime.getRoom().getId());

        List<Long> bookedSeatList = bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId);
        Set<Long> bookedSeatIds = bookedSeatList != null ? new HashSet<>(bookedSeatList) : new HashSet<>();

        // TỐI ƯU HIỆU NĂNG: Lấy dữ liệu Pricing một lần duy nhất (Fix N+1)
        Long branchId = showtime.getRoom().getBranch().getId();
        RoomType roomType = showtime.getRoom().getRoomType();
        
        PricingServiceImpl psImpl = (PricingServiceImpl) pricingService;
        
        // Lấy toàn bộ bảng giá của RoomType này
        List<SeatPrice> seatPrices = psImpl.getSeatPriceRepository().findAllByRoomTypeAndIsActiveTrue(roomType);
        
        // Chuyển thành Map với Key là String ID của SeatType
        Map<String, BigDecimal> priceMap = new HashMap<>();
        for (SeatPrice sp : seatPrices) {
            if (sp.getSeatType() != null) {
                priceMap.put(sp.getSeatType().getId(), sp.getPrice());
            }
        }

        // Lấy quy tắc chi nhánh một lần
        List<com.example.cinema.model.entity.BranchPricingRule> branchRules = 
                psImpl.getBranchPricingRuleRepository().findAllByBranchIdOrderByPriorityAsc(branchId);

        Set<Long> lockedSeatIds = new HashSet<>();
        try {
            String pattern = LOCK_KEY_PREFIX + showtimeId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null) {
                for (String key : keys) {
                    String[] parts = key.split(":");
                    if (parts.length >= 3) lockedSeatIds.add(Long.parseLong(parts[2]));
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Redis connection failed: " + e.getMessage());
        }

        PricingRuleMatcher matcher = psImpl.getPricingRuleMatcher();

        return allSeats.stream().map(seat -> {
            SeatResponse res = new SeatResponse();
            res.setId(seat.getId());
            res.setRowChar(seat.getRowChar());
            res.setColNum(seat.getColNum());
            res.setSeatCode(seat.getRowChar() + seat.getColNum());
            
            if (seat.getSeatType() != null) {
                res.setSeatTypeId(seat.getSeatType().getId());
                res.setSeatTypeName(seat.getSeatType().getName());
                res.setSeatType(seat.getSeatType().getId());
            } else {
                res.setSeatTypeId("NORMAL"); res.setSeatTypeName("Thường"); res.setSeatType("NORMAL");
            }
            
            res.setAvailable(!bookedSeatIds.contains(seat.getId()) && !lockedSeatIds.contains(seat.getId()));

            // Tính giá nhanh trong bộ nhớ
            BigDecimal basePrice = priceMap.getOrDefault(res.getSeatTypeId(), new BigDecimal("80000"));
            PriceCalculationResult calculation = calculateFastPrice(showtime, seat, basePrice, branchRules, matcher);
            res.setPrice(calculation.getFinalPrice());
            res.setPriceBreakdown(calculation.getAppliedRules());

            return res;
        }).collect(Collectors.toList());
    }

    private PriceCalculationResult calculateFastPrice(Showtime showtime, Seat seat, BigDecimal basePrice, 
                                                     List<com.example.cinema.model.entity.BranchPricingRule> branchRules,
                                                     PricingRuleMatcher matcher) {
        PriceCalculator calculator = new BasePriceCalculator(basePrice);
        List<String> appliedRules = new ArrayList<>();
        appliedRules.add("Giá gốc: " + basePrice.intValue() + "đ");

        for (com.example.cinema.model.entity.BranchPricingRule link : branchRules) {
            PricingRule rule = link.getRule();
            if (rule.isActive() && matcher.matches(rule, showtime, seat, null)) {
                if (rule.getImpactType() == com.example.cinema.model.enums.PricingImpactType.ADDITIVE) {
                    calculator = new AdditiveDecorator(calculator, rule.getImpactValue());
                    appliedRules.add(rule.getName() + " (+" + rule.getImpactValue().intValue() + "đ)");
                } else if (rule.getImpactType() == com.example.cinema.model.enums.PricingImpactType.SUBTRACTIVE) {
                    calculator = new AdditiveDecorator(calculator, rule.getImpactValue().negate());
                    appliedRules.add(rule.getName() + " (-" + rule.getImpactValue().intValue() + "đ)");
                } else if (rule.getImpactType() == com.example.cinema.model.enums.PricingImpactType.PERCENTAGE) {
                    calculator = new PercentageDecorator(calculator, rule.getImpactValue());
                    appliedRules.add(rule.getName() + " (x" + rule.getImpactValue() + ")");
                } else if (rule.getImpactType() == com.example.cinema.model.enums.PricingImpactType.FIXED) {
                    final BigDecimal fixed = rule.getImpactValue();
                    calculator = () -> fixed;
                    appliedRules.add(rule.getName() + " (Cố định: " + fixed.intValue() + "đ)");
                }
                if (!rule.isStackable()) break;
            }
        }
        return new PriceCalculationResult(calculator.calculate(), appliedRules);
    }

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException("Movie not found"));
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("Room not found"));
        validateBranchMovieDistribution(room.getBranch().getId(), movie.getId());
        Format format = null;
        if (request.getFormatId() != null) {
            format = formatRepository.findById(request.getFormatId()).orElseThrow(() -> new AppException("Format not found"));
        }
        validateRoomFormatCompatibility(room, format);
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getStartTime().plusMinutes(movie.getDuration()));
        showtime.setStatus(ShowtimeStatus.UPCOMING);
        showtime.setTotalSeats(room.getCapacity());
        showtime.setSoldSeats(0);
        showtime.setFormat(format);
        return mapToResponse(showtimeRepository.save(showtime));
    }

    @Override
    @Transactional
    public ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id).orElseThrow(() -> new AppException("Not found"));
        Movie movie = movieRepository.findById(request.getMovieId()).orElseThrow(() -> new AppException("Movie not found"));
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("Room not found"));
        validateBranchMovieDistribution(room.getBranch().getId(), movie.getId());
        Format format = null;
        if (request.getFormatId() != null) {
            format = formatRepository.findById(request.getFormatId()).orElseThrow(() -> new AppException("Format not found"));
        }
        validateRoomFormatCompatibility(room, format);
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getStartTime().plusMinutes(movie.getDuration()));
        showtime.setFormat(format);
        return mapToResponse(showtimeRepository.save(showtime));
    }

    private void validateBranchMovieDistribution(Long branchId, Long movieId) {
        boolean isDistributed = branchMovieRepository.existsByBranchIdAndMovieIdAndIsActiveTrue(branchId, movieId);
        if (!isDistributed) throw new AppException("Phim này chưa được phân phối tại chi nhánh!");
    }

    private void validateRoomFormatCompatibility(Room room, Format format) {
        if (format == null || room.getRoomType() == null) return;
        Set<Format> supported = room.getRoomType().getSupportedFormats();
        if (supported == null || supported.isEmpty()) return;
        if (!supported.contains(format)) throw new AppException("Phòng chiếu không hỗ trợ định dạng " + format.getName());
    }

    @Override
    @Transactional
    public void deleteShowtime(Long id) {
        Showtime s = showtimeRepository.findById(id).orElseThrow(() -> new AppException("Not found"));
        if (s.getSoldSeats() > 0) throw new AppException("Suất chiếu đã bán vé");
        if (s.getStartTime().isBefore(LocalDateTime.now().plusHours(2))) throw new AppException("Cách giờ chiếu dưới 2 tiếng");
        showtimeRepository.deleteById(id);
    }

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        ShowtimeResponse res = new ShowtimeResponse();
        res.setId(showtime.getId());
        if (showtime.getMovie() != null) {
            Movie m = showtime.getMovie();
            res.setMovieId(m.getId()); res.setMovieTitle(m.getTitle()); res.setMovieDuration(m.getDuration());
            res.setPosterUrl(m.getPosterUrl()); res.setAgeRating(m.getAgeRating() != null ? m.getAgeRating().name() : "P");
            res.setGenres(m.getGenres().stream().map(Genre::getName).collect(Collectors.toList()));
        }
        if (showtime.getRoom() != null) {
            res.setRoomId(showtime.getRoom().getId()); res.setRoomName(showtime.getRoom().getName());
            res.setRoomType(showtime.getRoom().getRoomType().getId());
        }
        res.setStartTime(showtime.getStartTime()); res.setEndTime(showtime.getEndTime());
        
        // Fix lỗi hiển thị format name để không bị (null)
        if (showtime.getFormat() != null) {
            res.setFormatId(showtime.getFormat().getId());
            res.setFormatName(showtime.getFormat().getName());
        } else {
            res.setFormatName("2D"); // Giá trị mặc định an toàn
        }
        
        res.setStatus(showtime.getStatus()); res.setTotalSeats(showtime.getTotalSeats());
        List<Long> bookedSeats = bookingDetailRepository.findBookedSeatIdsByShowtime(showtime.getId());
        res.setSoldSeats(bookedSeats != null ? bookedSeats.size() : 0);
        return res;
    }
}
