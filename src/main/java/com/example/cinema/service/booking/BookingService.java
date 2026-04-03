package com.example.cinema.service.booking;

import com.example.cinema.model.dto.request.BookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);

    List<BookingResponse> getMyBookings();

    BookingResponse getMyBookingByCode(String bookingCode);

    void cancelBooking(String bookingCode);

    void checkInBooking(String bookingCode);
}
