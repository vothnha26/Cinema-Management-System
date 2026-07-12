package com.example.cinema.controller.staff;

import com.example.cinema.model.dto.request.PosBookingRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.service.pos.impl.StaffPosFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho Staff bán vé tại quầy POS (SRP).
 * Delegate hoàn toàn sang StaffPosFacade (DIP + Facade Pattern).
 * Route /api/staff/** được SecurityConfig bảo vệ: chỉ STAFF + ADMIN.
 */
@RestController
@RequestMapping("/api/staff/pos")
public class StaffPosController {

    private final StaffPosFacade posFacade;

    public StaffPosController(StaffPosFacade posFacade) {
        this.posFacade = posFacade;
    }

    /**
     * Xác nhận bán vé tại quầy.
     * Staff chỉ gọi 1 API này → Facade xử lý toàn bộ phía sau.
     */
    @PostMapping("/book")
    public ResponseEntity<ApiResponse<BookingResponse>> book(@RequestBody PosBookingRequest request) {
        BookingResponse response = posFacade.processDirectBooking(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
