# 🎬 StarCinema Elite – Hệ Thống Quản Lý Rạp Chiếu Phim Hiện Đại

## 📌 Giới thiệu
StarCinema là một nền tảng quản lý rạp chiếu phim toàn diện, được thiết kế với mục tiêu mang lại trải nghiệm điện ảnh đỉnh cao cho khách hàng và hiệu quả vận hành tối đa cho nhà quản lý. Dự án được xây dựng dựa trên các tiêu chuẩn kỹ thuật khắt khe, ưu tiên tính mở rộng và khả năng bảo trì.

---

## 🎖️ Tuyên ngôn Kỹ thuật (Core Mandates)
Mọi thành viên tham gia phát triển bắt buộc phải tuân thủ:
1. **SOLID Absolute Compliance:** Viết code sạch, tách biệt trách nhiệm (SRP), ưu tiên tính đóng mở (OCP).
2. **Design Pattern First:** Luôn xem xét áp dụng các mẫu thiết kế (**Strategy, Factory, Facade, Observer, Decorator**) trước khi triển khai logic phức tạp (Tính giá, Khuyến mãi, Phân bổ ghế).
3. **Workflow:** `Sequence Diagram` (Thiết kế) -> `Implementation` (Code) -> `Automated Validation` (Test).

---

## 🚀 Tính năng cốt lõi

### 1. Phân hệ Khách hàng (Public Portal)
*   **Trình duyệt phim thông minh:** Lọc phim theo trạng thái (Đang chiếu, Sắp chiếu, Đã chiếu) và theo chi nhánh. Hệ thống chỉ hiển thị những phim thực sự có suất chiếu khả dụng.
*   **Đặt vé nhanh (Quick Booking):** Cơ chế chọn cascading (Chi nhánh -> Phim -> Ngày -> Suất) giúp khách hàng mua vé chỉ trong vài giây.
*   **Sơ đồ ghế Real-time:** Sử dụng **WebSocket (STOMP)** và **Redis** để đồng bộ trạng thái ghế ngay lập tức, ngăn chặn tình trạng đặt trùng ghế.
*   **Thanh toán VietQR tự động:** Tích hợp Webhook để xác nhận thanh toán chuyển khoản ngân hàng ngay tức thì.

### 2. Phân hệ Quản lý (Manager & Admin)
*   **Quản lý suất chiếu (Showtime Management):** Thuật toán kiểm tra xung đột lịch chiếu tự động, hỗ trợ phân bổ phim theo chi nhánh.
*   **Hệ thống tính giá linh hoạt (Pricing Strategy):** Áp dụng **Strategy Pattern** để tính giá vé dựa trên nhiều yếu tố (Loại ghế, loại phòng, khung giờ, ngày lễ, hội viên).
*   **Thống kê & Báo cáo (Analytics):** Dashboard trực quan về doanh thu, tỷ lệ lấp đầy phòng chiếu và hiệu quả khuyến mãi.
*   **Audit Trail:** Ghi lại toàn bộ lịch sử thao tác của nhân viên để đảm bảo tính minh bạch.

### 3. Phân hệ Nhân viên (Staff/POS)
*   **Bán vé tại quầy (POS):** Giao diện tối ưu cho thao tác nhanh.
*   **Soát vé (Check-in):** Hệ thống quét mã QR để xác thực vé vào cổng.

---

## 🛠️ Công nghệ sử dụng
*   **Backend:** Java 17, Spring Boot 3.2.x, Spring Security (JWT).
*   **Database:** MySQL (Production), H2 (Testing), Redis (Locking/Caching).
*   **Real-time:** Spring WebSocket + STOMP.
*   **Media:** Cloudinary Cloud Integration.
*   **Frontend:** HTML5, Vanilla CSS/JS (Star Cinema Style), Bootstrap 5.
*   **DevOps:** Maven, Git, JUnit, Selenium.

---

## 📂 Cấu trúc thư mục tiêu biểu
*   `src/main/java/.../controller`: API Endpoints (Phân tách theo module: movie, showtime, booking...).
*   `src/main/java/.../service/commerce/pricing`: Nơi triển khai các Strategy tính giá vé (Cực kỳ quan trọng).
*   `src/main/resources/static`: Giao diện người dùng (index, booking, payment, dashboard...).
*   `report/`: Tài liệu đặc tả Use Case, Activity và Sequence Diagrams.

---

## 📝 Hướng dẫn cho thành viên mới
1. **Nghiên cứu Diagram:** Trước khi sửa logic, hãy đọc các file `.puml` trong thư mục `report/` để hiểu luồng đi của dữ liệu.
2. **Setup Môi trường:**
    *   Copy `.env.example` thành `.env` và cấu hình các key (Cloudinary, DB, SePay).
    *   Chạy `mvn clean install` để tải dependencies.
3. **Quy tắc Commit:** Commit message rõ ràng, tập trung vào "Tại sao" thay vì "Cái gì".

---
*StarCinema - Engineering Excellence in Cinema Management.*
