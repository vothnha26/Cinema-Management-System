package com.example.cinema.controller.showtime;

import com.example.cinema.model.dto.request.ShowtimeRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.ShowtimeResponse;
import com.example.cinema.service.showtime.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.example.cinema.model.entity.Staff;
import com.example.cinema.model.entity.User;
import com.example.cinema.repository.user.StaffRepository;
import com.example.cinema.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/showtimes")
@CrossOrigin(origins = "*")
public class ShowtimeController {
    private static final Logger log = LoggerFactory.getLogger(ShowtimeController.class);

    private final ShowtimeService showtimeService;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    public ShowtimeController(ShowtimeService showtimeService, StaffRepository staffRepository, UserRepository userRepository) {
        this.showtimeService = showtimeService;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getAllShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            Authentication authentication) {

        log.info(">>> FETCHING SHOWTIMES. Auth: {}", (authentication != null ? authentication.getName() : "NULL"));

        // Nếu truyền branchId từ request (Public hoặc Quick Booking)
        if (branchId != null) {
            List<ShowtimeResponse> list = showtimeService.getAllShowtimes(date, branchId);
            if (movieId != null) {
                list = list.stream().filter(s -> s.getMovieId().equals(movieId)).toList();
            }
            return ResponseEntity.ok(ApiResponse.ok(list));
        }

        // Nếu là Manager hoặc Staff (tự động lấy theo chi nhánh của họ nếu không truyền branchId)
        if (authentication != null && (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))
                || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")))) {

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username).orElse(null));

            if (user != null) {
                Staff staff = staffRepository.findByUser(user).orElse(null);
                if (staff != null && staff.getBranch() != null) {
                    List<ShowtimeResponse> list = showtimeService.getShowtimesByBranch(staff.getBranch().getId(), date);
                    if (movieId != null) {
                        list = list.stream().filter(s -> s.getMovieId().equals(movieId)).toList();
                    }
                    return ResponseEntity.ok(ApiResponse.ok(list));
                }
            }
        }

        List<ShowtimeResponse> list = showtimeService.getAllShowtimes(date, null);
        if (movieId != null) {
            list = list.stream().filter(s -> s.getMovieId().equals(movieId)).toList();
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/dates")
    public ResponseEntity<ApiResponse<List<java.time.LocalDate>>> getDistinctDates(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.getDistinctShowtimeDates(movieId, branchId)));
    }    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.getShowtimeById(id)));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<com.example.cinema.model.dto.response.SeatResponse>>> getSeats(
            @PathVariable Long id,
            Authentication authentication) {
        
        String username = (authentication != null && !authentication.getName().equals("anonymousUser")) 
                ? authentication.getName() : null;
                
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.getSeatStatusForShowtime(id, username)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShowtimeResponse>> createShowtime(@RequestBody @Valid ShowtimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(showtimeService.createShowtime(request)));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<com.example.cinema.model.dto.response.BulkShowtimeResultResponse>> createBulkShowtimes(
            @RequestBody com.example.cinema.model.dto.request.BulkShowtimeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.createBulkShowtimes(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> updateShowtime(@PathVariable Long id,
            @RequestBody @Valid ShowtimeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.updateShowtime(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
