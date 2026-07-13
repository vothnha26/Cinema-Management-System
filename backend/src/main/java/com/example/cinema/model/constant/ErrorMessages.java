package com.example.cinema.model.constant;

public class ErrorMessages {
    public static final String CHOOSE_AT_LEAST_ONE_SEAT = "Vui lòng chọn ít nhất 1 ghế.";
    public static final String CHOOSE_SHOWTIME = "Vui lòng chọn suất chiếu.";
    public static final String GUEST_INFO_REQUIRED = "Vui lòng cung cấp đầy đủ thông tin khách vãng lai (Tên, Email, SĐT).";
    public static final String SHOWTIME_NOT_FOUND = "Suất chiếu không tồn tại.";
    public static final String SEAT_NOT_FOUND = "Ghế không tồn tại.";
    public static final String CUSTOMER_NOT_FOUND = "Không tìm thấy thông tin khách hàng.";
    public static final String UNAUTHORIZED = "Bạn không có quyền thực hiện hành động này.";
    
    public static final String INVALID_CREDENTIALS = "Tài khoản hoặc mật khẩu không chính xác";
    public static final String EMAIL_NOT_VERIFIED = "Tài khoản chưa được xác thực email. Một mã OTP mới đã được gửi tới email của bạn.";
    public static final String USERNAME_ALREADY_EXISTS = "Username đã tồn tại";
    public static final String EMAIL_ALREADY_IN_USE = "Email đã được sử dụng";
    public static final String REGISTRATION_SUCCESS = "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.";
    public static final String EMAIL_NOT_FOUND = "Không tìm thấy tài khoản với email này";
    public static final String INVALID_OR_EXPIRED_OTP = "Mã OTP không hợp lệ hoặc đã hết hạn";
    public static final String USER_NOT_FOUND = "Không tìm thấy người dùng";
    public static final String INCORRECT_OLD_PASSWORD = "Mật khẩu cũ không chính xác";
    
    private ErrorMessages() {}
}
