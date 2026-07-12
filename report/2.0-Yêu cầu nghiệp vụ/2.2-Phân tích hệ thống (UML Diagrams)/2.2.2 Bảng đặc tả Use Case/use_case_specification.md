# Đặc tả Use Case Chi tiết - Hệ thống FlashCinema

Tài liệu này cung cấp cái nhìn chi tiết và toàn diện về toàn bộ các chức năng vận hành của hệ thống FlashCinema. Mỗi Use Case (UC) được mô tả bao gồm Actor, Luồng xử lý chính và các Luồng ngoại lệ để đảm bảo tính minh bạch trong thiết kế phần mềm.

---

### 2.2.2.1. UC-01: Đặt vé và Thanh toán trực tuyến
**Actor:** Khách hàng.
*   **Mô tả:** Luồng nghiệp vụ cốt lõi cho phép người dùng đặt chỗ và mua vé từ xa.
*   **Luồng chính:**
    1. Khách hàng lựa chọn suất chiếu từ trang chủ hoặc phim chi tiết.
    2. Hệ thống hiển thị sơ đồ ghế thực tế (Real-time Seat Map).
    3. Khách hàng chọn ghế và nhấn "Giữ chỗ". Hệ thống sử dụng **Redis Lock** để bảo vệ trạng thái ghế.
    4. Khách hàng chọn thêm bắp nước (FnB) và áp dụng voucher/điểm thưởng.
    5. Hệ thống tính tổng tiền thông qua **PriceDecorator**.
    6. Khách hàng thực hiện quét mã **VietQR (SePay)**.
    7. Hệ thống nhận diện giao dịch thành công qua **Webhook** ngân hàng.
    8. Hệ thống phát tín hiệu **WebSocket** để tự động chuyển trang và xuất vé điện tử (QR Code).

### 2.2.2.2. UC-02: Tìm kiếm và Đồng bộ hóa Phim từ TMDB
**Actor:** Quản lý.
*   **Mô tả:** Tự động hóa việc lấy metadata phim từ hệ thống quốc tế.
*   **Luồng chính:**
    1. Quản lý nhập từ khóa tìm kiếm phim.
    2. Hệ thống gọi API TMDB và hiển thị kết quả.
    3. Quản lý xác nhận phim muốn đồng bộ.
    4. Hệ thống tải Poster lên **Cloudinary** và lưu metadata vào DB.

### 2.2.2.3. UC-03: Xếp lịch chiếu thông minh bằng Gemini AI
**Actor:** Quản lý.
*   **Mô tả:** Sử dụng AI để tối ưu hóa công suất phòng chiếu.
*   **Luồng chính:**
    1. Quản lý gửi yêu cầu lập lịch kèm các điều kiện (phim, ngày, phòng).
    2. **Gemini AI Service** phân tích và trả về lịch chiếu tối ưu dưới dạng JSON.
    3. Quản lý duyệt và hệ thống tự động sinh các thực thể Showtime.

### 2.2.2.4. UC-04: Kiểm soát vào phòng (Check-in)
**Actor:** Nhân viên.
*   **Mô tả:** Xác thực quyền vào xem phim của khách hàng.
*   **Luồng chính:**
    1. Nhân viên quét mã QR trên vé khách hàng.
    2. Hệ thống xác thực trạng thái "Đã thanh toán" và "Đúng giờ chiếu".
    3. Hệ thống đổi trạng thái vé sang "Đã sử dụng".

### 2.2.2.5. UC-05: Thiết lập Chính sách giá và Khuyến mãi
**Actor:** Quản trị viên.
*   **Mô tả:** Quản lý các quy tắc phụ thu và chiết khấu linh hoạt.
*   **Luồng chính:**
    1. Admin tạo Quy tắc (Pricing Rule) như: Phụ thu cuối tuần +20k.
    2. Hệ thống lưu logic và tự động kích hoạt trong luồng đặt vé khi thỏa mãn điều kiện.

### 2.2.2.6. UC-06: Xem báo cáo Thống kê & Dashboard
**Actor:** Quản lý / Admin.
*   **Mô tả:** Cung cấp dữ liệu trực quan về doanh thu và hiệu suất kinh doanh.
*   **Luồng chính:**
    1. Người dùng chọn loại báo cáo (Doanh thu phim, Bán bắp nước, Tỷ lệ lấp đầy).
    2. Hệ thống truy vấn và hiển thị biểu đồ thời gian thực.

### 2.2.2.7. UC-07: Quản lý Danh mục Phim (Local Movie Management)
**Actor:** Quản lý.
*   **Mô tả:** Chỉnh sửa thông tin phim, mác dán tuổi và trạng thái phim (Đang chiếu/Sắp chiếu).
*   **Luồng chính:**
    1. Quản lý thay đổi trạng thái hoặc chỉnh sửa thông tin metadata phim nội bộ.
    2. Hệ thống cập nhật và làm mới Cache liên quan đến phim.

### 2.2.2.8. UC-08: Quản lý Suất chiếu đơn lẻ (Manual Showtime)
**Actor:** Quản lý.
*   **Mô tả:** Tạo hoặc hủy các suất chiếu thủ công ngoài luồng AI.
*   **Luồng chính:**
    1. Quản lý chọn Phim, Phòng, Giờ bắt đầu.
    2. Hệ thống kiểm tra xung đột thời gian (Collision Check) với các suất khác trong cùng phòng.
    3. Hệ thống khởi tạo suất chiếu mới.

### 2.2.2.9. UC-09: Quản lý Sơ đồ Phòng và Ghế (Room & Seat Mall)
**Actor:** Quản trị viên.
*   **Mô tả:** Thiết lập hạ tầng vật lý của rạp chiếu.
*   **Luồng chính:**
    1. Admin thiết lập số hàng, số cột và loại ghế (Standard/VIP) cho từng phòng.
    2. Hệ thống tự động sinh ma trận ghế và lưu vào DB.

### 2.2.2.10. UC-10: Quản lý Hạng thành viên (Loyalty Management)
**Actor:** Quản trị viên.
*   **Mô tả:** Cấu hình các hạng Silver, Gold, Platinum và quyền lợi giảm giá.
*   **Luồng chính:**
    1. Admin định nghĩa các ngưỡng chi tiêu để nâng hạng.
    2. Hệ thống tự động quét và nâng hạng cho User khi đạt điều kiện.

### 2.2.2.11. UC-11: Bán vé và Bắp nước tại POS (Counter Sale)
**Actor:** Nhân viên.
*   **Mô tả:** Quy trình bán hàng trực tiếp tại quầy.
*   **Luồng chính:**
    1. Nhân viên chọn vé và combo khách muốn mua.
    2. Hệ thống tính tiền và in vé bản cứng/hóa đơn.
    3. Ghi nhận doanh thu trực tiếp vào hệ thống.

### 2.2.2.12. UC-12: Quản lý Tài khoản nôi bộ (Staff & Account)
**Actor:** Quản trị viên.
*   **Mô tả:** Cấu hình nhân sự và phân quyền (RBAC).
*   **Luồng chính:**
    1. Admin tạo tài khoản cho nhân viên mới và gán Role (Staff/Manager).
    2. Hệ thống cấp quyền truy cập các API tương ứng.

### 2.2.2.13. UC-13: Theo dõi Nhật ký vận hành (Audit Logs)
**Actor:** Quản trị viên.
*   **Mô tả:** Giám sát các hoạt động nhạy cảm trong hệ thống.
*   **Luồng chính:**
    1. Hệ thống tự động ghi nhật ký các hành động của Admin/Manager sử dụng AOP.
    2. Admin tra cứu nhật ký để kiểm soát an ninh dữ liệu.

### 2.2.2.14. UC-14: Quản lý Combo và Sản phẩm FnB
**Actor:** Quản lý.
*   **Mô tả:** Thiết lập các gói bắp nước ưu đãi.
*   **Luồng chính:**
    1. Quản lý tạo combo mới (Vd: Combo 2 người).
    2. Thiết lập giá và hình ảnh minh họa cho sản phẩm.

### 2.2.2.15. UC-15: Tra cứu và Quản lý Lịch sử Đặt vé
**Actor:** Khách hàng.
*   **Mô tả:** Giúp khách hàng quản lý các vé đã mua.
*   **Luồng chính:**
    1. Khách hàng truy cập trang cá nhân.
    2. Hệ thống hiển thị danh sách các Invoice và trạng thái Vé (Chưa dùng/Đã dùng/Đã hủy).

---
Tài liệu Đặc tả 15 Use Case trọng tâm hệ thống FlashCinema - Đảm bảo bao quát toàn bộ quy trình vận hành và quản trị.
