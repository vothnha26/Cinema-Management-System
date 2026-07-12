package com.example.cinema.model.enums;

public enum PricingConditionType {
    DAY_OF_WEEK,      // Giá trị: MONDAY, TUESDAY... (Hoặc JSON list)
    TIME_RANGE,       // Giá trị: HH:mm-HH:mm
    DATE_RANGE,       // Giá trị: yyyy-MM-dd:yyyy-MM-dd
    ROOM_TYPE,        // Giá trị: ID của RoomType
    SEAT_TYPE,        // Giá trị: ID của SeatType
    BRANCH,           // Giá trị: ID của Branch
    SHOW_FORMAT,      // Giá trị: 2D, 3D, IMAX...
    MOVIE_AGE_RATING, // Giá trị: P, C13, C16, C18...
    MEMBER_TIER       // Giá trị: SILVER, GOLD, DIAMOND...
}
