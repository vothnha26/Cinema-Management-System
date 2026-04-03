package com.example.cinema.showtime;

import com.example.cinema.BaseIntegTest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.model.enums.RoomType;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.service.showtime.SchedulingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchedulingIntegTest extends BaseIntegTest {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    @DisplayName("SCH-3.2: Test logic gợi ý lịch chiếu AI (Buzz Score & Staggered)")
    void testAISchedulingSuggestions() {
        // 1. Chuẩn bị dữ liệu: 2 phim với ưu tiên khác nhau
        Movie m1 = new Movie();
        m1.setTitle("Blockbuster A");
        m1.setDuration(120);
        m1.setPriorityLevel(5); // Cao
        m1.setStatus(MovieStatus.NOW_SHOWING);
        m1 = movieRepository.save(m1);
        final Long m1Id = m1.getId();

        Movie m2 = new Movie();
        m2.setTitle("Indie B");
        m2.setDuration(90);
        m2.setPriorityLevel(1); // Thấp
        m2.setStatus(MovieStatus.NOW_SHOWING);
        m2 = movieRepository.save(m2);
        final Long m2Id = m2.getId();

        // 2. Chuẩn bị 2 phòng
        Room r1 = new Room(); r1.setName("R1"); r1.setType(RoomType.HALL_2D); r1.setCapacity(50);
        r1 = roomRepository.save(r1);
        Room r2 = new Room();
        r2.setName("R2");
        r2.setType(RoomType.HALL_3D);
        r2.setCapacity(100);
        r2 = roomRepository.save(r2);

        
        
        
        // 3. Chạy AI Suggestions cho ngày mai
        LocalDate targetDate = LocalDate.now().plusDays(2);
        List<ShowtimeResponse> suggestions = schedulingService.generateAISuggestions(targetDate, "NEW");

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty(), "AI phải tạo ra được gợi ý lịch chiếu");

        // Kiểm tra phân bổ: Phim ưu tiên cao (m1) thường xuất hiện nhiều hơn hoặc sớm hơn
        boolean hasM1 = suggestions.stream().anyMatch(s -> s.getMovieId().equals(m1Id));
        assertTrue(hasM1, "Lịch phải bao gồm phim Blockbuster A");
        // 

        // 4. Áp dụng gợi ý vào DB
        schedulingService.applySuggestions(suggestions, false);

        // 5. Kiểm tra DB
        long count = showtimeRepository.count();
        assertTrue(count >= suggestions.size(), "Các suất chiếu gợi ý phải được lưu vào DB");
    }
}
