package com.example.cinema.movie;

import com.example.cinema.BaseIntegTest;
import com.example.cinema.model.dto.request.MovieRequest;
import com.example.cinema.model.dto.response.MovieResponse;
import com.example.cinema.model.dto.tmdb.TMDBMovieDto;
import com.example.cinema.model.dto.tmdb.TMDBSearchResponse;
import com.example.cinema.model.enums.MovieStatus;
import com.example.cinema.service.movie.MovieService;
import com.example.cinema.service.movie.tmdb.TMDBService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MovieIntegTest: Kiểm thử luồng quản lý phim thực tế (No Mocking).
 */
@AutoConfigureMockMvc
public class MovieIntegTest extends BaseIntegTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private TMDBService tmdbService;

    @Autowired
    private MovieService movieService;

    @Test
    @DisplayName("MGT-2.1: Test luồng tìm phim TMDB và tạo phim thực tế")
    @Rollback(false) // Tắt rollback để bạn có thể xem kết quả trong DB Client
    void testRealMovieCreationFlow() {
        // 1. Tìm phim trên TMDB (Thực tế)
        String query = "Dune: Part Two";
        TMDBSearchResponse searchResponse = tmdbService.searchMovies(query);
        
        assertNotNull(searchResponse, "TMDB Search Response should not be null");
        assertFalse(searchResponse.getResults().isEmpty(), "Should find at least one movie for 'Dune'");
        
        Long tmdbId = searchResponse.getResults().get(0).getId();
        System.out.println(">>> Found TMDB ID: " + tmdbId);

        // 2. Lấy chi tiết phim (Thực tế)
        TMDBMovieDto details = tmdbService.getMovieDetails(tmdbId);
        assertNotNull(details, "TMDB Details should not be null");
        System.out.println(">>> Movie Title from TMDB: " + details.getTitle());

        // 3. Chuẩn bị Request tạo phim trong hệ thống
        MovieRequest request = new MovieRequest();
        request.setTitle(details.getTitle());
        request.setDescription(details.getOverview());
        request.setDuration(details.getRuntime() != null ? details.getRuntime() : 120);
        request.setReleaseDate(details.getReleaseDate() != null ? LocalDate.parse(details.getReleaseDate()) : LocalDate.now());
        request.setRating(details.getVoteAverage());
        request.setAgeRating("PG13");
        request.setStatus(MovieStatus.SHOWING);
        request.setTmdbId(tmdbId);
        request.setPosterUrl(details.getPosterPath());
        request.setTrailerUrl("https://youtube.com/watch?v=real_trailer");

        // Sử dụng byte array của 1x1 GIF hợp lệ để Cloudinary chấp nhận
        byte[] validImageBytes = java.util.Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

        MockMultipartFile posterFile = new MockMultipartFile(
            "poster", "test_poster.gif", "image/gif", validImageBytes
        );

        // 4. Thực hiện tạo phim (Có gọi CloudinaryService bên trong)
        // Lưu ý: MovieServiceImpl.createMovie hiện tại đang xử lý logic upload ảnh
        // (Tôi cần kiểm tra xem nó có thực sự gọi Cloudinary không)
        MovieResponse response = movieService.createMovie(request, posterFile);

        // 5. Kiểm chứng (Verification)
        assertNotNull(response.getId(), "Saved Movie ID should not be null");
        assertEquals(details.getTitle(), response.getTitle());
        
        System.out.println(">>> Real Movie Created with ID: " + response.getId());
        if (response.getPosterUrl() != null && response.getPosterUrl().contains("cloudinary")) {
            System.out.println(">>> SUCCESS: Real Cloudinary URL generated: " + response.getPosterUrl());
        }
    }
}
