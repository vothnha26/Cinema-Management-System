package com.example.cinema.controller.staff;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.service.ticket.impl.TicketWorkflowManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho Staff kiểm soát vé (Check-in) tại cổng rạp.
 * Delegate sang TicketWorkflowManager (State Pattern).
 * Route /api/staff/** được SecurityConfig bảo vệ: chỉ STAFF + ADMIN.
 */
@RestController
@RequestMapping("/api/staff/ticket")
public class StaffTicketController {

    private final TicketWorkflowManager ticketWorkflowManager;

    public StaffTicketController(TicketWorkflowManager ticketWorkflowManager) {
        this.ticketWorkflowManager = ticketWorkflowManager;
    }

    /**
     * Tra cứu vé bằng booking code.
     */
    @GetMapping("/lookup/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> lookup(@PathVariable String bookingCode) {
        BookingResponse response = ticketWorkflowManager.lookupByCode(bookingCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Check-in vé: chuyển CONFIRMED → CHECKED_IN.
     */
    @PostMapping("/checkin/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> checkin(@PathVariable String bookingCode) {
        BookingResponse response = ticketWorkflowManager.checkin(bookingCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Hủy vé (nếu cần).
     */
    @PostMapping("/cancel/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(@PathVariable String bookingCode) {
        BookingResponse response = ticketWorkflowManager.cancel(bookingCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
