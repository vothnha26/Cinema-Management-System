package com.example.cinema.controller.public_api;

import com.example.cinema.model.dto.request.OnlineBookingRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.service.booking.impl.CustomerBookingFacade;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý online booking (Public, hỗ trợ Guest & Logged-in Customer).
 */
@RestController
@RequestMapping("/api/public/booking")
public class PublicBookingController {

    private final CustomerBookingFacade bookingFacade;

    public PublicBookingController(CustomerBookingFacade bookingFacade) {
        this.bookingFacade = bookingFacade;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<BookingResponse>> checkout(@RequestBody OnlineBookingRequest request) {
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            username = authentication.getName();
        }

        BookingResponse response = bookingFacade.processOnlineBooking(request, username);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
