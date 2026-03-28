# 🎯 Tổng quan Project – Cinema Management System

## 1. Vấn đề giải quyết

Rạp phim truyền thống quản lý vé bằng phương pháp thủ công hoặc phần mềm rời rạc, dẫn đến:

- Không có giao diện đặt vé trực tuyến cho khách hàng.
- Nhân viên không có công cụ POS để bán vé tại quầy nhanh chóng.
- Quản lý khó theo dõi doanh thu theo thời gian thực.
- Không có hệ thống thành viên (loyalty) để giữ chân khách hàng.

**Cinema Management System** giải quyết toàn bộ quy trình từ quản lý nội dung phim, lịch chiếu, đặt vé online, bán vé tại quầy đến thống kê doanh thu.

---

## 2. Người dùng (Actors)

| Role | Tên gọi | Mô tả |
|------|---------|-------|
| `CUSTOMER` | Khách hàng | Người dùng cuối – đặt vé online, tích điểm thành viên |
| `STAFF` | Nhân viên | Bán vé tại quầy (POS), check-in vé |
| `MANAGER` | Quản lý rạp | Quản lý phim, lịch chiếu, xem báo cáo doanh thu |
| `ADMIN` | Quản trị viên | Toàn quyền hệ thống, quản lý tài khoản & cấu hình |

---

## 3. Use Case chính

```
CUSTOMER:
  ✅ Đăng ký / Đăng nhập
  ✅ Xem phim đang chiếu / sắp chiếu
  ✅ Xem lịch chiếu & sơ đồ ghế
  ✅ Đặt vé online (chọn ghế → combo → áp ưu đãi → thanh toán)
  ✅ Hủy vé trước giờ chiếu
  ✅ Xem lịch sử đặt vé & điểm tích lũy

STAFF:
  ✅ Bán vé tại quầy (POS)
  ✅ Check-in vé bằng mã booking
  ✅ Xem lịch chiếu trong ngày

MANAGER:
  ✅ Thêm/sửa/xóa phim, thể loại, đạo diễn, diễn viên
  ✅ Quản lý phòng chiếu, sơ đồ ghế, giá vé
  ✅ Tạo & quản lý suất chiếu
  ✅ Quản lý combo bắp nước & khuyến mãi
  ✅ Xem thống kê doanh thu (biểu đồ)

ADMIN:
  ✅ Toàn bộ quyền MANAGER
  ✅ Quản lý tài khoản người dùng (CRUD, phân role)
  ✅ Cấu hình hệ thống (hạng thành viên, tỷ lệ điểm)
```

---

## 4. Constraints & Nghiệp vụ đặc biệt

- **Kiểm tra xung đột lịch chiếu**: Không thể tạo 2 suất chiếu cùng phòng trùng giờ.
- **Kiểm tra ghế**: Ghế bị đặt không thể chọn lại trong cùng suất chiếu (lock theo transaction).
- **Tích điểm tự động**: Sau mỗi booking thành công, hệ thống cộng điểm và tự động nâng hạng thành viên.
- **Ưu đãi theo hạng**: Silver −5%, Gold −10%, Platinum −15% trên giá vé.
- **Mã booking duy nhất**: Dùng để check-in, không thể trùng lặp.
