package com.example.cinema.controller.commerce;

import com.example.cinema.model.dto.request.SeatPriceRequest;
import com.example.cinema.model.dto.response.ApiResponse;
import com.example.cinema.model.dto.response.SeatPriceResponse;
import com.example.cinema.service.commerce.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@CrossOrigin(origins = "*")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeatPriceResponse>>> getAllSeatPrices() {
        return ResponseEntity.ok(ApiResponse.ok(pricingService.getAllSeatPrices()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SeatPriceResponse>> updateSeatPrice(@RequestBody @Valid SeatPriceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(pricingService.updateSeatPrice(request)));
    }
}
