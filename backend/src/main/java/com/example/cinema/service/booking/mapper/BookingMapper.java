package com.example.cinema.service.booking.mapper;

import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.entity.BookingDetail;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BookingMapper {
    private final ModelMapper modelMapper;

    public BookingMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public BookingResponse toResponse(Booking b) {
        BookingResponse res = modelMapper.map(b, BookingResponse.class);
        
        // Custom mapping for complex fields
        if (b.getShowtime() != null) {
            res.setMovieTitle(b.getShowtime().getMovie().getTitle());
            res.setRoomName(b.getShowtime().getRoom().getName());
            res.setShowTime(b.getShowtime().getStartTime());
        }
        
        if (b.getDetails() != null) {
            res.setSeatCodes(b.getDetails().stream()
                    .map(BookingDetail::getSeatCode)
                    .collect(Collectors.toList()));
        }
        
        if (b.getPayment() != null) {
            res.setPaymentMethod(b.getPayment().getPaymentMethod());
            res.setPaymentStatus(b.getPayment().getPaymentStatus());
            res.setTransactionId(b.getPayment().getTransactionId());
            res.setPaidAt(b.getPayment().getPaidAt());
        }
        
        return res;
    }
}
