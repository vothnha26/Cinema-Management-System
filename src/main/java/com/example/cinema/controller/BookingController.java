package com.example.cinema.controller;

import com.example.cinema.model.dto.request.BookingRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.service.booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody @Valid BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @PostMapping("/hold-seat")
    public ResponseEntity<ApiResponse<Void>> holdSeat(@RequestParam Long showtimeId, @RequestParam Long seatId, @RequestParam String sessionId) {
        bookingService.holdSeat(showtimeId, seatId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/release-seat")
    public ResponseEntity<ApiResponse<Void>> releaseSeat(@RequestParam Long showtimeId, @RequestParam Long seatId, @RequestParam String sessionId) {
        bookingService.releaseSeat(showtimeId, seatId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/my-locked-seats")
    public ResponseEntity<ApiResponse<List<Long>>> getMyLockedSeats(@RequestParam Long showtimeId, @RequestParam String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getMyLockedSeats(showtimeId, sessionId)));
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/{code}")
    public ResponseEntity<BookingResponse> getMyBookingByCode(@PathVariable String code) {
        return ResponseEntity.ok(bookingService.getMyBookingByCode(code));
    }

    @PutMapping("/{code}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable String code) {
        bookingService.cancelBooking(code);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    @PutMapping("/{code}/checkin")
    public ResponseEntity<ApiResponse<String>> checkInBooking(@PathVariable String code) {
        bookingService.checkInBooking(code);
        return ResponseEntity.ok(ApiResponse.ok("Booking checked in successfully"));
    }
}
