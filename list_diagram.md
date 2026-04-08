# Danh sách các Sơ đồ cho Báo cáo StarCinema (Tinh gọn & Chuyên sâu)

## 1. Sơ đồ Hoạt động (Activity Diagram)
*Tập trung vào luồng nghiệp vụ tổng thể của hệ thống.*

1. **Quy trình Đăng ký & Xác thực tài khoản:** Luồng từ Đăng ký -> Gửi mã OTP qua Mail -> Xác thực -> Đăng nhập thành công.
2. **Quy trình Đặt vé & Thanh toán trực tuyến:** Luồng chính của Customer: Chọn phim -> Chọn suất chiếu & Chỗ ngồi -> Chọn Combo FnB -> Áp dụng Khuyến mãi -> Thanh toán qua Gateway -> Nhận vé điện tử.
3. **Quy trình Xếp lịch chiếu thông minh (AI-powered Scheduling):** Luồng nhập yêu cầu (Ngày, Chi nhánh, Chiến lược: Doanh thu/Gia đình/Đêm muộn) -> Hệ thống AI tính toán trọng số phim -> Đề xuất danh sách suất chiếu tối ưu -> Manager phê duyệt & Áp dụng.
4. **Quy trình Quản lý Nội dung (Movie & Artist):** Luồng tìm kiếm phim từ TMDB API -> Chỉnh sửa thông tin nghệ sĩ/phim -> Lưu trữ vào hệ thống.
5. **Quy trình Thống kê & Báo cáo Doanh thu:** Luồng tổng hợp dữ liệu từ các giao dịch Ticket và Combo thành biểu đồ doanh số.

## 2. Sơ đồ Tuần tự (Sequence Diagram)
*Tập trung vào giải pháp kỹ thuật, tính tương tác giữa các Object và áp dụng Design Patterns.*

1. **Tính giá vé thông minh (Pricing Strategy Pattern):** Thể hiện cách `PricingService` gọi các Strategy khác nhau dựa trên loại ghế (VIP/Thường), ngày (Ngày lễ/Thứ 2-6) và loại suất chiếu để trả về giá vé cuối cùng. (Minh chứng cho OCP & Strategy Pattern).
2. **Thuật toán Xếp lịch tự động (AI Scheduling Algorithm):** Thể hiện sự tương tác giữa `SchedulingService`, `BuzzAnalysisService` (lấy xu hướng bên ngoài) và các `Repository` để tính toán trọng số (Weighting) và đưa ra gợi ý suất chiếu tối ưu.
3. **Xây dựng Sơ đồ ghế động (Seat Layout Builder Pattern):** Thể hiện cách hệ thống render layout ghế linh hoạt cho từng loại phòng chiếu (2D, 3D, IMAX).
3. **Điều phối Đặt vé & Thanh toán (Booking Facade Pattern):** Thể hiện `BookingFacade` điều phối giữa `OrderService`, `PromotionService` và `PaymentGateway` để xử lý một giao dịch phức tạp.
4. **Hệ thống Quy tắc Khuyến mãi (Promotion Rules):** Thể hiện cách hệ thống kiểm tra điều kiện và áp dụng giảm giá (Strategy/Decorator) mà không dùng `if-else` lồng nhau.
5. **Đồng bộ hóa & Tìm kiếm Nội dung (Movie Facade):** Thể hiện sự phối hợp giữa Database nội bộ và TMDB API để lấy dữ liệu phim và ảnh (Cloudinary).
6. **Xác nhận vé & Check-in (Ticket Validation):** Luồng nhân viên quét mã QR/ID để xác thực vé tại phòng chiếu và cập nhật trạng thái đã sử dụng.

---
**💡 Ghi chú cho Báo cáo:**
- Các sơ đồ **Pricing Strategy** và **Booking Facade** là trái tim của kiến trúc hệ thống, cần được phân tích kỹ để thể hiện trình độ áp dụng SOLID.
- Các sơ đồ CRUD đơn giản (Thêm/Xóa/Sửa thông thường) đã được gộp chung hoặc lược bỏ để tránh làm loãng báo cáo.
