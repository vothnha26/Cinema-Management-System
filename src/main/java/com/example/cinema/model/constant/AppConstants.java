package com.example.cinema.model.constant;

public class AppConstants {
    // Redis Keys & TTL
    public static final String REDIS_SEAT_LOCK_PREFIX = "seat_lock:";
    public static final String REDIS_SEAT_DEADLINE_PREFIX = "seat_deadline:";
    public static final String REDIS_PENDING_BOOKING_PREFIX = "pending_booking:";
    public static final long REDIS_HOLD_DURATION_MINUTES = 15;
    
    // Booking Codes
    public static final String BOOKING_CODE_PREFIX = "BKG-";
    public static final String POS_CODE_PREFIX = "POS-";
    public static final String TXN_CODE_PREFIX = "TXN-";
    public static final String MANUAL_CODE_PREFIX = "MANUAL-";
    public static final String TXN_PREFIX_ONLINE = "ONL-";
    public static final String BOOKING_CODE_PREFIX_SC = "SC-";
    public static final String DEFAULT_GUEST_NAME = "Khách vãng lai";
    public static final String DATE_TIME_FORMAT = "HH:mm dd/MM/yyyy";
    
    // Auth & Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_STAFF = "ROLE_STAFF";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    
    // Messages
    public static final String MSG_UNAUTHORIZED = "Bạn không có quyền thực hiện hành động này";
    public static final String MSG_NOT_FOUND = "Không tìm thấy dữ liệu yêu cầu";
    public static final String MSG_SUCCESS = "Thao tác thành công";

    // Room Types
    public static final String ROOM_TYPE_IMAX = "IMAX";
    public static final String ROOM_TYPE_4DX = "HALL_4DX";
    public static final String ROOM_TYPE_STANDARD = "STD";
    
    // Room Status
    public static final String ROOM_STATUS_ACTIVE = "ACTIVE";
    public static final String ROOM_STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String ROOM_STATUS_INACTIVE = "INACTIVE";

    private AppConstants() {}
}
