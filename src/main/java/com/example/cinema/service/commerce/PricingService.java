package com.example.cinema.service.commerce;

import com.example.cinema.model.dto.request.SeatPriceRequest;
import com.example.cinema.model.dto.response.SeatPriceResponse;
import com.example.cinema.model.entity.Seat;
import com.example.cinema.model.entity.Showtime;

import java.math.BigDecimal;
import java.util.List;

public interface PricingService {
    List<SeatPriceResponse> getAllSeatPrices();

    SeatPriceResponse updateSeatPrice(SeatPriceRequest request);

    com.example.cinema.model.dto.response.PriceCalculationResult calculateTicketPrice(Showtime showtime, Seat seat);
}
