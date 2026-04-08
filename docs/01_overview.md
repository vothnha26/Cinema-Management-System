# 🎯 Tổng quan Project – Cinema Management System

## 1. Mục tiêu hệ thống

**Cinema Management System** là một nền tảng quản trị rạp chiếu phim hiện đại, hỗ trợ quản lý đa chi nhánh và tự động hóa quy trình từ đặt vé trực tuyến đến bán vé tại quầy (POS). Hệ thống tập trung vào trải nghiệm khách hàng tối ưu và cung cấp các công cụ quản lý mạnh mẽ cho ban quản trị.

### Vấn đề giải quyết:
- **Quản lý đa chi nhánh**: Hỗ trợ nhiều rạp phim thuộc cùng một hệ thống với cấu hình lịch chiếu và phim linh hoạt.
- **Tự động hóa giá vé**: Áp dụng quy tắc giá dựa trên khung giờ, loại ghế và hạng thành viên.
- **Thanh toán tích hợp**: Thanh toán qua QR Code (VietQR) và SePay để xác nhận booking tức thì.
- **Loyalty Program**: Tích điểm và nâng hạng thành viên tự động sau mỗi giao dịch.

---

## 2. Người dùng (Actors)

| Vai trò | Tên gọi | Phạm vi hoạt động |
|:---|:---|:---|
| `CUSTOMER` | Khách hàng | Đặt vé online, thanh toán, quản lý tài khoản & lịch sử giao dịch. |
| `STAFF` | Nhân viên | Xử lý bán vé tại quầy (POS), check-in vé bằng QR Code/Mã booking. |
| `MANAGER` | Quản lý chi nhánh | Quản lý phim tại rạp, lập lịch chiếu, quản lý kho Combo & doanh thu chi nhánh. |
| `ADMIN` | Quản trị viên hệ thống | Toàn quyền cấu hình: Quản lý chi nhánh, tài khoản, quy tắc giá & khuyến mãi toàn hệ thống. |

---

## 3. Use Case & Module chính

### 🛡️ Module Quản trị (Admin/Manager)
- **Quản lý Nội dung**: Phim (đồng bộ TMDB), Thể loại, Diễn viên, Đạo diễn.
- **Quản lý Hạ tầng**: Chi nhánh (Branch), Phòng chiếu (Room), Ghế ngồi (Seat) theo sơ đồ.
- **Quản lý Lịch chiếu**: Lập lịch suất chiếu (Showtime), kiểm tra xung đột thời gian & phòng.
- **Quản lý Thương mại**: Quy tắc giá linh hoạt (PricingRule), Khuyến mãi (Promotion), Combo bắp nước.
- **Thống kê & Báo cáo**: Doanh thu theo phim, chi nhánh, thời gian thực.

### 👥 Module Khách hàng (Customer)
- **Khám phá**: Xem phim đang chiếu/sắp chiếu, tìm kiếm phim.
- **Booking Online**: Chọn suất chiếu -> Chọn ghế -> Chọn Combo -> Áp mã giảm giá -> Thanh toán QRCode.
- **Cá nhân hóa**: Xem lịch sử đặt vé, mã QR check-in, quản lý điểm thưởng và hạng thành viên.

### 🖥️ Module Nhân viên (Staff - POS)
- **Bán vé tại quầy**: Giao diện POS đặt vé nhanh chóng cho khách vãng lai.
- **Check-in**: Quét mã QR code hoặc nhập mã Booking để xác nhận vé vào phòng.

---

## 4. Đặc điểm nổi bật trong Codebase

- **Quy tắc giá (Dynamic Pricing)**: Giá vé không cố định mà được tính toán động dựa trên `PricingRule` (Nguyên tắc: Ngày + Giờ + Loại ghế + Định dạng phim).
- **Thanh toán SePay/VietQR**: Xử lý Webhook để tự động xác nhận đặt vé ngay khi khách hàng chuyển khoản thành công.
- **Tối ưu hóa Search**: Tích hợp API TMDB để lấy thông tin phim nhanh chóng và chính xác.
- **Audit Logging**: Mọi hành động nhạy cảm của Admin/Manager đều được ghi log để kiểm tra.
