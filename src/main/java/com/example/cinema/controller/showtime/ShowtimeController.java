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
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/showtimes")
@CrossOrigin(origins = "*")
public class ShowtimeController {

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
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            Authentication authentication) {

        System.out.println(">>> FETCHING SHOWTIMES. Auth: " + (authentication != null ? authentication.getName() : "NULL"));
        if (authentication != null) {
            System.out.println(">>> Authorities: " + authentication.getAuthorities());
        }

        // Nếu là Manager hoặc Staff, chỉ xem suất chiếu của rạp mình
        if (authentication != null && (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")) 
                || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")))) {

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username).orElse(null));
            
            if (user != null) {
                Staff staff = staffRepository.findByUser(user).orElse(null);
                System.out.println(">>> Staff lookup for " + username + ": " + (staff != null ? "Found, Branch: " + staff.getBranch().getName() : "NOT FOUND IN STAFF TABLE"));
                
                if (staff != null && staff.getBranch() != null) {
                    List<ShowtimeResponse> list = showtimeService.getShowtimesByBranch(staff.getBranch().getId(), date);
                    if (movieId != null) {
                        list = list.stream().filter(s -> s.getMovieId().equals(movieId)).toList();
                    }
                    return ResponseEntity.ok(ApiResponse.ok(list));
                }
            }
        }

        List<ShowtimeResponse> list = showtimeService.getAllShowtimes(date);
        if (movieId != null) {
            list = list.stream().filter(s -> s.getMovieId().equals(movieId)).toList();
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(showtimeService.getAllShowtimes(null).stream()
                .filter(s -> s.getId().equals(id)).findFirst().orElse(null)));
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
