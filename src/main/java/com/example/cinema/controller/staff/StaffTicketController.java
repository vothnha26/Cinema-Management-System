package com.example.cinema.controller.staff;

import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.service.ticket.impl.TicketWorkflowManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho Staff kiểm soát vé (Check-in) tại cổng rạp.
 * Delegate sang TicketWorkflowManager (State Pattern).
 * Route /api/staff/** được SecurityConfig bảo vệ: chỉ STAFF + ADMIN.
 */
@RestController
@RequestMapping("/api/staff/tickets")
public class StaffTicketController {

    private final TicketWorkflowManager ticketWorkflowManager;

    public StaffTicketController(TicketWorkflowManager ticketWorkflowManager) {
        this.ticketWorkflowManager = ticketWorkflowManager;
    }

    /**
     * Lấy lịch sử tất cả các booking để hiển thị cho Staff.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> history() {
        return ResponseEntity.ok(ApiResponse.ok(ticketWorkflowManager.getAllHistory()));
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
     * Validate/Check-in vé bằng ID.
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<BookingResponse>> validate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ticketWorkflowManager.validateById(id)));
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
