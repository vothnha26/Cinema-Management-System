# ⚡ Tối ưu hóa Hiệu năng – Cinema Management System

Hệ thống Cinema Management System được thiết kế để xử lý lượng lớn người dùng cùng lúc (Concurrent Users) thông qua các giải pháp tối ưu hóa hiện đại.

## 1. Caching với Redis (Spring Data Redis)

Để giảm thiểu áp lực lên MySQL, hệ thống sử dụng **Redis** làm lớp đệm tốc độ cao:
- **Showtime Caching**: Lưu trữ danh sách lịch chiếu đang hoạt động. Khi khách hàng tìm kiếm phim, hệ thống sẽ lấy dữ liệu từ Redis (Thời gian phản hồi < 10ms).
- **Temporary Seat Locking**: Khi khách hàng đang chọn ghế, trạng thái "Ghế đang được chọn" sẽ lưu vào Redis với TTL (Time-To-Live) là 5-10 phút, tránh khóa cứng bảng `seats` trong database.
- **User Session/Token Cache**: Tối ưu hóa quá trình xác thực JWT bằng cách lưu trữ Token (nếu cần thiết) hoặc Blacklist các Token đã Logout.

---

## 2. Tối ưu hóa Cơ sở Dữ liệu (MySQL Optimization)

- **Indexing Strategy**:
    - Sử dụng **Composite Index** trên bảng `showtimes(movie_id, start_time, room_id)` để tìm kiếm suất chiếu nhanh hơn.
    - Duy trì **Unique Index** trên bảng `bookings(booking_code)` để đảm bảo tính nhất quán.
- **Connection Pooling**: Sử dụng **HikariCP** (default trong Spring Boot) với các tham số tối ưu (Maximum Pool Size, Idle Timeout) để quản lý kết nối hiệu quả.
- **Query Optimization**: Tránh lỗi **N+1** bằng cách sử dụng `@EntityGraph` hoặc `FETCH JOIN` trong Spring Data JPA linh hoạt.

---

## 3. Tối ưu hóa Tài nguyên Media (Content Delivery Network)

- **Cloudinary CDN**: Poster và ảnh quảng cáo của Phim không được lưu trên Backend. Chúng được lưu trữ và phục vụ qua **Cloudinary CDN**.
- **Transformation On-the-fly**: Tận dụng tính năng của Cloudinary để tự động resize poster phim (Small/Medium/Large) phù hợp với kích thước màn hình thiết bị (Mobile/Web), giúp giảm băng thông tải trang.

---

## 4. Xử lý Dữ liệu lớn (Big Data Handling)

- **BigDataSeeder**: Cung cấp công cụ sinh hàng triệu bản ghi giả lập để test sự ổn định của hệ thống trước khi triển khai thực tế.
- **Paginate Data**: Mọi API lấy danh sách (Phim, Booking, Audit Log) đều được **phân trang (Pagination)** bắt buộc, ngăn chặn việc load quá nhiều dữ liệu vào Memory RAM.

---

## 5. Tối ưu hóa Xử lý Bất đồng bộ (Asynchronous Processing)

- **@Async Support**: Các tác vụ tiêu tốn thời gian như:
    - Gửi email thông báo đặt vé thành công.
    - Sinh mã QR Booking.
    - Cập nhật điểm thưởng thành viên.
Được xử lý bất đồng bộ (`Thread Pool`), giúp Request của khách hàng phản hồi tức thì mà không phải chờ các service phụ trợ hoàn thành.
