# 🧪 Testing – Cinema Management System (Elite Standard)

## 1. Chiến lược kiểm thử (Elite Pipeline)

Hệ thống áp dụng quy trình kiểm thử 3 lớp để đảm bảo tính đúng đắn của logic nghiệp vụ và giao diện, tập trung vào **luồng nghiệp vụ thực tế (Real-world Flows)**:

```
Elite Testing Pyramid:
         / \
        /E2E\         ← Selenium: Kiểm tra luồng thực tế (End-to-End Flow)
       /─────\
      / Integ \       ← SpringBootTest + MockMvc: Kiểm tra API, Webhook & DB
     /─────────\
    / Unit Tests \    ← JUnit 5 + Mockito: Kiểm tra thuật toán (Pricing, AI, Strategy)
   /───────────────\
```

---

## 2. Các luồng nghiệp vụ trọng tâm (Core Business Flows)

### 2.1. Luồng Khách hàng (Customer Booking Flow)
**Mục tiêu:** Xác thực từ bước chọn ghế đến khi nhận vé thành công.
1.  **Selection:** Chọn Phim -> Suất chiếu -> Ghế (Kiểm tra trạng thái ghế `AVAILABLE`).
2.  **Pricing:** Tính toán tổng tiền qua `PricingService` (áp dụng Decorators cho loại ghế/phòng và Strategy cho khuyến mãi/thành viên).
3.  **Booking:** Tạo Booking code duy nhất qua `CustomerBookingFacade`.
4.  **Payment:** Sinh QR Code (VietQR) -> Xử lý Webhook từ cổng thanh toán (SePay) -> Cập nhật trạng thái `CONFIRMED`.
5.  **Completion:** Tự động cộng điểm tích lũy -> Gửi Email thông báo (Observer Pattern).

### 2.2. Luồng Quản lý (Managerial Flow)
**Mục tiêu:** Xác thực nghiệp vụ vận hành rạp.
1.  **Movie & Layout:** Quản lý kho phim và sơ đồ ghế (theo `SeatLayoutStrategy`).
2.  **Scheduling:** Sắp xếp suất chiếu (Kiểm tra xung đột lịch và quy tắc AI 70/30).
3.  **Pricing Policy:** Thiết lập các Decorators giá vé theo khung giờ và ngày trong tuần.
4.  **Audit Control:** Tự động ghi log mọi thao tác nhạy cảm qua `AuditLogAspect`.

---

## 3. Các loại hình kiểm thử chi tiết

### 3.1. Unit & Integration Tests (MockTest)
- **Pricing Engine:** Kiểm tra Decorator Pattern tính đúng giá cộng dồn và Strategy Pattern áp dụng mã giảm giá.
- **AI Scheduling:** Kiểm tra thuật toán phân bổ giờ vàng và dãn cách thời gian.
- **Webhook Integration:** Giả lập payload từ SePay/VietQR để kiểm tra logic cập nhật trạng thái thanh toán.

### 3.2. End-to-End (E2E) UI Testing
**Công cụ:** Selenium WebDriver + WebDriverManager.
- **Manager Dashboard:** Đăng nhập Staff -> Thêm phim -> Tạo suất chiếu -> Kiểm tra hiển thị trên Website.
- **Customer Portal:** Chọn ghế -> Thanh toán giả lập -> Kiểm tra thông báo thành công.

---

## 4. Quyết định kỹ thuật cho Testing

> [!IMPORTANT]
> - **Test Database:** Sử dụng H2 In-Memory cho Unit/Integ tests để tách biệt môi trường.
> - **External Services:** Mock `IVietQRService` và `INotificationAutomationService` để đảm bảo test có thể chạy offline và không tốn chi phí/gửi mail rác.
> - **Transactional:** Mọi Integration Test phải có `@Transactional` để rollback dữ liệu sau khi chạy.

---

## 5. Lệnh thực hiện

```bash
# Chạy toàn bộ suite (Bao gồm Unit + Integration)
mvn clean test

# Chạy riêng luồng E2E (Yêu cầu Chrome Browser)
mvn test -Dtest=*E2ETest
```

---

## 6. Danh sách Test Case Ưu tiên (Elite Checklist)

| STT | Luồng nghiệp vụ | Loại Test | Trạng thái |
|:---:|:----------------|:---------:|:----------:|
| 1 | Tính giá vé qua chuỗi Decorators (VIP, Weekend, Evening) | Unit | 🔄 |
| 2 | Chặn tạo suất chiếu trùng lịch/không đủ giờ dọn dẹp | Integration | 🔄 |
| 3 | Xử lý Webhook thanh toán thành công/thất bại | MockMvc | 🔄 |
| 4 | Tự động sinh sơ đồ ghế Standard/IMAX | Unit | 🔄 |
| 5 | Luồng mua vé trọn gói (Phim + Ghế + Combo + Member) | E2E | 🔄 |
| 6 | Ghi Audit Log cho hành động xóa suất chiếu | Integration | 🔄 |
