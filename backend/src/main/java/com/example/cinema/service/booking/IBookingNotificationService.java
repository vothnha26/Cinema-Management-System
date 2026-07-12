package com.example.cinema.service.booking;

import com.example.cinema.model.entity.Booking;

public interface IBookingNotificationService {
    void sendBookingConfirmation(Booking booking);
}
