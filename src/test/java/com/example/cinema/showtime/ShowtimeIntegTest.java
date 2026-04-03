package com.example.cinema.showtime;

import com.example.cinema.BaseIntegTest;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.model.entity.Movie;
import com.example.cinema.model.entity.Room;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.model.enums.RoomType;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.service.showtime.ShowtimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShowtimeIntegTest extends BaseIntegTest {

    @Autowired
    private ShowtimeService showtimeService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    @DisplayName("SCH-3.1 & 3.3: Test chặn trùng lịch và khoảng hở dọn dẹp")
    void testShowtimeConflictBlocking() {
        // 1. Chuẩn bị Movie (120 phút)
        Movie movie = new Movie();
        movie.setTitle("Inception Test");
        movie.setDuration(120);
        movie.setStatus(MovieStatus.NOW_SHOWING);
        movie = movieRepository.save(movie);

        // 2. Chuẩn bị Room
        Room room = new Room();
        room.setName("Cinema 1");
        room.setType(RoomType.HALL_2D);
        room.setCapacity(100);
        room = roomRepository.save(room);

        // 3. Tạo suất chiếu 1 (18:00 - 20:00)
        LocalDateTime startTime1 = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        ShowtimeRequest request1 = new ShowtimeRequest();
        request1.setMovieId(movie.getId());
        request1.setRoomId(room.getId());
        request1.setStartTime(startTime1);

        ShowtimeResponse response1 = showtimeService.createShowtime(request1);
        assertNotNull(response1.getId());

        // 4. Thử tạo suất chiếu 2 (20:10 - 22:10) -> Xung đột vì gap 15p (20:00 + 15p = 20:15)
        LocalDateTime startTime2 = startTime1.plusMinutes(130); // 18:00 + 120p duration + 10p wait
        ShowtimeRequest request2 = new ShowtimeRequest();
        request2.setMovieId(movie.getId());
        request2.setRoomId(room.getId());
        request2.setStartTime(startTime2);

        AppException exception = assertThrows(AppException.class, () -> {
            showtimeService.createShowtime(request2);
        });

        assertTrue(exception.getMessage().contains("Xung đột thời gian"));

        // 5. Thử tạo suất chiếu 3 (20:20 - 22:20) -> Hợp lệ (20:00 + 15p < 20:20)
        LocalDateTime startTime3 = startTime1.plusMinutes(140); // 18:00 + 120p duration + 20p wait
        ShowtimeRequest request3 = new ShowtimeRequest();
        request3.setMovieId(movie.getId());
        request3.setRoomId(room.getId());
        request3.setStartTime(startTime3);

        ShowtimeResponse response3 = showtimeService.createShowtime(request3);
        assertNotNull(response3.getId());
    }
}
