package com.example.cinema.service.ai.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.SchedulingRequest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.model.enums.ShowtimeStatus;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.movie.BranchMovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.service.ai.SchedulingService;
import com.example.cinema.service.ai.GeminiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulingServiceImpl implements SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingServiceImpl.class);

    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BranchMovieRepository branchMovieRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    private final BookingRepository bookingRepository;

    @Autowired
    public SchedulingServiceImpl(MovieRepository movieRepository,
            RoomRepository roomRepository,
            ShowtimeRepository showtimeRepository,
            BranchMovieRepository branchMovieRepository,
            GeminiService geminiService,
            ObjectMapper objectMapper,
            BookingRepository bookingRepository) {
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.showtimeRepository = showtimeRepository;
        this.branchMovieRepository = branchMovieRepository;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<ShowtimeResponse> getSuggestions(LocalDate date, String mode) {
        SchedulingRequest request = new SchedulingRequest();
        request.setDate(date);
        request.setMode(mode);
        return getSuggestions(request);
    }

    @Override
    public List<ShowtimeResponse> getSuggestions(SchedulingRequest request) {
        log.info("AI Scheduling triggered for branch: {}, date: {}", request.getBranchId(), request.getDate());

        // 1. LẤY DỮ LIỆU ĐẦU VÀO CHO AI
        List<Movie> movies;
        if (request.getBranchId() != null) {
            movies = branchMovieRepository.findByBranchIdAndIsActiveTrue(request.getBranchId())
                    .stream()
                    .map(BranchMovie::getMovie)
                    .filter(m -> List.of(MovieStatus.SHOWING, MovieStatus.COMING).contains(m.getStatus()))
                    .collect(Collectors.toList());
        } else {
            movies = movieRepository.findAllByStatusIn(List.of(MovieStatus.SHOWING, MovieStatus.COMING));
        }

        List<Room> rooms = request.getBranchId() != null 
                ? roomRepository.findAll().stream().filter(r -> r.getBranch().getId().equals(request.getBranchId())).collect(Collectors.toList())
                : roomRepository.findAll();

        if (movies.isEmpty() || rooms.isEmpty()) {
            log.warn("No movies or rooms found for AI scheduling");
            return new ArrayList<>();
        }

        // 2. XÂY DỰNG PROMPT CHO GEMINI
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một chuyên gia điều phối lịch chiếu phim chuyên nghiệp. Hãy lập lịch chiếu cho ngày ")
              .append(request.getDate()).append(" với các dữ liệu sau:\n\n");

        prompt.append("DANH SÁCH PHIM:\n");
        for (Movie m : movies) {
            prompt.append("- ID: ").append(m.getId()).append(", Tên: ").append(m.getTitle())
                  .append(", Thời lượng: ").append(m.getDuration()).append(" phút")
                  .append(", Định dạng hỗ trợ: ").append(m.getFormats().stream().map(Format::getName).collect(Collectors.joining(",")))
                  .append("\n");
        }

        prompt.append("\nDANH SÁCH PHÒNG CHIẾU:\n");
        for (Room r : rooms) {
            prompt.append("- ID: ").append(r.getId()).append(", Tên: ").append(r.getName())
                  .append(", Loại phòng: ").append(r.getRoomType().getName())
                  .append(", Định dạng hỗ trợ: ").append(r.getRoomType().getSupportedFormats().stream().map(Format::getName).collect(Collectors.joining(",")))
                  .append("\n");
        }

        // Lấy suất chiếu hiện hữu để AI biết khoảng trống
        List<Showtime> existing = showtimeRepository.findAllByStartTimeBetween(
                request.getDate().atStartOfDay(), 
                request.getDate().plusDays(1).atStartOfDay(), 
                request.getBranchId());
        
        if (!existing.isEmpty() && "FILL".equals(request.getMode())) {
            prompt.append("\nCÁC SUẤT CHIẾU ĐÃ CÓ (KHÔNG ĐƯỢC LẬP ĐÈ LÊN CÁC KHOẢNG NÀY):\n");
            for (Showtime es : existing) {
                prompt.append("- Phòng: ").append(es.getRoom().getName())
                      .append(", Từ: ").append(es.getStartTime().toLocalTime())
                      .append(", Đến: ").append(es.getEndTime().toLocalTime())
                      .append("\n");
            }
        }

        prompt.append("\nYÊU CẦU ĐIỀU PHỐI:\n")
              .append("- Chế độ: ").append(request.getMode() != null ? request.getMode() : "FILL").append("\n")
              .append("- Chiến lược: ").append(request.getStrategy() != null ? request.getStrategy() : "BALANCED").append("\n")
              .append("- Khung giờ hoạt động: ").append(request.getStartTime() != null ? request.getStartTime() : "08:00").append(" đến ").append(request.getEndTime() != null ? request.getEndTime() : "23:55").append("\n")
              .append("- CHỈ THỊ ĐẶC BIỆT TỪ QUẢN LÝ (BẮT BUỘC TUÂN THỦ TUYỆT ĐỐI): ").append(request.getCustomDirectives() != null ? request.getCustomDirectives() : "Không có").append("\n")
              .append("- Quy tắc: Mỗi suất chiếu cách nhau ít nhất 15 phút dọn phòng. Không được lập lịch ngoài khung giờ hoạt động.\n");

        prompt.append("\nHÃY TRẢ VỀ KẾT QUẢ DƯỚI DẠNG MẢNG JSON NHƯ SAU (KHÔNG GIẢI THÍCH THÊM):\n")
              .append("[{\"id\": -1, \"movieId\": 1, \"movieTitle\": \"Tên Phim\", \"roomId\": 1, \"roomName\": \"Phòng 1\", \"formatName\": \"2D\", \"startTime\": \"")
              .append(request.getDate()).append("T08:00:00\", \"endTime\": \"").append(request.getDate()).append("T10:00:00\"}]");

        // 3. GỌI GEMINI AI
        String aiResponse;
        try {
            aiResponse = geminiService.generateResponse(prompt.toString());
            log.info("AI Response received: {}", aiResponse);
        } catch (Exception e) {
            log.error("Gemini API connection error: {}", e.getMessage());
            throw new AppException("Hệ thống AI đang quá tải hoặc gặp sự cố. Vui lòng thử lại sau vài giây.");
        }

        try {
            // Làm sạch response (Gemini thường bọc trong ```json ... ```)
            String cleanedJson = aiResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            if (cleanedJson.isEmpty() || "[]".equals(cleanedJson)) return new ArrayList<>();
            
            List<ShowtimeResponse> suggestions = objectMapper.readValue(cleanedJson, new TypeReference<List<ShowtimeResponse>>() {});
            
            // Bổ sung các thông tin còn thiếu cho FE
            suggestions.forEach(s -> {
                s.setId(-1L); // Đánh dấu là lịch nháp
                Movie m = movies.stream().filter(mov -> mov.getId().equals(s.getMovieId())).findFirst().orElse(null);
                if (m != null) s.setPosterUrl(m.getPosterUrl());
            });

            return suggestions;
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            throw new AppException("AI trả về kết quả không hợp lệ. Hãy thử điều chỉnh yêu cầu của bạn.");
        }
    }

    @Override
    @Transactional
    public void applySuggestions(List<ShowtimeResponse> suggestions, boolean overwrite) {
        if (suggestions.isEmpty()) return;
        
        LocalDate targetDate = suggestions.get(0).getStartTime().toLocalDate();
        
        // Lấy branchId từ suggestion đầu tiên (hoặc từ context nếu có)
        // Vì hiện tại ShowtimeResponse chưa có branchId, ta sẽ lấy từ Room của suggestion đầu tiên
        Long branchId = null;
        if (suggestions.get(0).getRoomId() != null) {
            Room firstRoom = roomRepository.findById(suggestions.get(0).getRoomId()).orElse(null);
            if (firstRoom != null && firstRoom.getBranch() != null) {
                branchId = firstRoom.getBranch().getId();
            }
        }

        if (overwrite) {
            // SỬA: Truyền branchId vào để chỉ xóa suất chiếu của đúng chi nhánh đang lập lịch
            List<Showtime> existing = showtimeRepository.findAllByStartTimeBetween(
                targetDate.atStartOfDay(), targetDate.plusDays(1).atStartOfDay(), branchId);
            
            for (Showtime s : existing) {
                // Chỉ xóa nếu thực sự chưa có bất kỳ đặt chỗ nào (tránh lỗi FK)
                if (bookingRepository.countByShowtimeId(s.getId()) == 0) {
                    showtimeRepository.delete(s);
                } else {
                    log.warn("Cannot delete showtime {} because it đã có vé", s.getId());
                }
            }
        }

        for (ShowtimeResponse res : suggestions) {
            if (res.getId() != null && res.getId() > 0) continue; // Bỏ qua lịch đã có
            
            Showtime s = new Showtime();
            Movie movie = movieRepository.findById(res.getMovieId()).orElseThrow(() -> new AppException("Movie not found"));
            Room room = roomRepository.findById(res.getRoomId()).orElseThrow(() -> new AppException("Room not found"));
            
            s.setMovie(movie);
            s.setRoom(room);
            s.setStartTime(res.getStartTime());
            s.setEndTime(res.getEndTime());
            s.setStatus(ShowtimeStatus.UPCOMING);
            s.setTotalSeats(room.getCapacity());
            s.setSoldSeats(0);
            
            // Tìm format từ tên
            if (res.getFormatName() != null) {
                movie.getFormats().stream()
                    .filter(f -> f.getName().equals(res.getFormatName()))
                    .findFirst()
                    .ifPresent(s::setFormat);
            }
            
            showtimeRepository.save(s);
        }
    }
}
