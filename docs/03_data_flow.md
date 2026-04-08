# 🔄 Luồng dữ liệu chính – Cinema Management System

Tài liệu này sử dụng sơ đồ Sequence và Step để mô phỏng các tương tác quan trọng giữa Actors và Hệ thống.

## 1. Luồng Đặt vé Online (Customer)

Mô phỏng quy trình đặt vé tích hợp thanh toán tự động qua `CustomerBookingFacade`.

```mermaid
sequenceDiagram
    actor C as Khách hàng
    participant F as BookingFacade
    participant P as PricingService
    participant BK as BookingService
    participant Pay as PaymentService
    participant W as SePay Webhook

    C->>F: Chọn Suất chiếu & Ghế & Combo
    F->>P: Tính toán giá vé (áp Decorator)
    P-->>F: Giá cuối cùng & Mã ưu đãi
    F->>BK: Tạo Booking (Trạng thái PENDING)
    BK->>Pay: Tạo dữ liệu thanh toán VietQR
    BK-->>C: Trả về QR Code chuyển khoản
    
    Note over C,W: Chuyển khoản thành công qua Ngân hàng
    
    W->>Pay: Gửi POST Webhook (Xác nhận giao dịch)
    Pay->>BK: Cập nhật trạng thái Booking (PAID)
    BK->>BK: Sinh mã QR Check-in
    BK-->>C: Hiển thị thông báo Thành công & QR Code
```

---

## 2. Luồng Tính giá vé và Ưu đãi (Business Layer)

Mô phỏng cách hệ thống xử lý giá linh hoạt với `Strategy` và `Decorator`.

```mermaid
graph TD
    B[Base Price] --> S{Seat Surcharge}
    S -- VIP --> S1[+ Phụ phí VIP]
    S -- Thường --> S2[+0 VNĐ]
    S1 --> R{Room Surcharge}
    S2 --> R
    R -- IMAX/3D --> R1[+ Phụ phí định dạng]
    R -- 2D --> R2[+0 VNĐ]
    R1 --> T{Time Surcharge}
    R2 --> T
    T -- Weekend/Holiday --> T1[+ Phụ phí khung giờ]
    T -- Weekday --> T2[+0 VNĐ]
    T1 --> D{Discount Applied?}
    T2 --> D
    D -- Yes --> D1[Apply Discount Strategy]
    D -- No --> D2[Final Price]
    D1 --> D2
```

---

## 3. Luồng Quản trị (Admin/Manager Flow)

Mô tả cách hệ thống lắng nghe các thay đổi cấu hình quan trọng.

1.  **Thay đổi Lịch chiếu**:
    - Manager tạo `Showtime` mới.
    - `SchedulingStrategy` kiểm tra xung đột phòng và thời gian dọn dẹp giữa các suất chiếu.
    - Cập nhật Redis Caching để cập diện lịch chiếu mới cho Khách hàng tức thì.

2.  **Quản lý Phim**:
    - Admin tìm kiếm phim qua **TMDB integration**.
    - Hệ thống map dữ liệu TMDB (Poster, Trailer, Nội dung) sang Entity của hệ thống.
    - `CloudinaryFacade` tải ảnh Poster lên Cloudinary và lưu URL vào DB.

---

## 4. Đặc điểm Luồng Thanh toán Webhook (SePay)

Cơ chế xử lý **Stateless Callback**:
- SePay POST dữ liệu đến `/api/webhook/sepay`.
- Hệ thống giải mã và kiểm tra ID Booking trong nội dung giao dịch.
- So khớp giá trị chuyển khoản với tổng tiền Booking.
- Nếu khớp: Chuyển trạng thái Booking sang `PAID`.
- Nếu lệch: Ghi log cảnh báo và giữ nguyên trạng thái `PENDING`.
