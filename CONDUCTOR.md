# 🎖️ StarCinema Conductor - Điều Phối Trạng Thái Phát Triển

Tệp này quản lý trạng thái phát triển và đối soát nghiệp vụ theo mandate **StarCinema Elite Engineering**. Quy trình chuẩn:
`Vẽ Sequence Diagram (SD) ──> Viết Code BE/FE ──> Automated Validation (MockMvc) ──> Đăng mã nguồn (Clean Push) ──> Cập nhật CONDUCTOR.md`.

---

## 📊 Bảng Theo Dõi Tiến Độ Step-by-Step

### 🔴 PHẦN 1: REFACTOR BACKEND (Spring Boot REST API)

| Bước | Nhiệm vụ | Class liên quan | Trạng thái |
| :--- | :--- | :--- | :--- |
| **Step 1.1** | Vẽ Sequence Diagram cho luồng Đặt vé Online & POS có áp dụng `PricingService` | `uml/` | 🟢 Hoàn thành |
| **Step 1.2** | Refactor `CustomerBookingFacade` (Tính giá vé động qua `PricingService`, xử lý N+1 query) | `CustomerBookingFacade.java` | 🟢 Hoàn thành |
| **Step 1.3** | Refactor `StaffPosFacade` (Tính giá vé động qua `PricingService`, xử lý N+1 query) | `StaffPosFacade.java` | 🟢 Hoàn thành |
| **Step 1.4** | Thêm các Query Method tránh N+1 load toàn bộ DB | `CustomerRepository.java` | 🟢 Hoàn thành |
| **Step 1.5** | Tích hợp **Flyway Migration** để thay thế `ddl-auto=update` | `pom.xml`, `application.properties` | 🟢 Hoàn thành |
| **Step 1.6** | Gom toàn bộ Magic Strings & Error Messages sang class Constant | `AppConstants.java`, `ErrorMessages.java` | 🟢 Hoàn thành |
| **Step 1.7** | Thay thế toàn bộ `System.err.println()` bằng SLF4J Logger | Các classes BE | 🟢 Hoàn thành |
| **Step 1.8** | Cấu hình CORS Whitelist cho cổng chạy Frontend Next.js | `SecurityConfig.java` | 🟢 Hoàn thành |
| **Step 1.9** | Chạy automated JUnit/MockMvc test cho toàn bộ luồng Backend | `src/test/java` | 🟢 Hoàn thành |

---

### 🟡 PHẦN 2: DI TRÚ FRONTEND SANG NEXT.JS (App Router)

| Bước | Nhiệm vụ | Thư mục/File liên quan | Trạng thái |
| :--- | :--- | :--- | :--- |
| **Step 2.1** | Khởi tạo dự án Next.js độc lập (`cinema-frontend`) | `cinema-frontend/` | 🟢 Hoàn thành |
| **Step 2.2** | Thiết lập API Client (Axios/Fetch + JWT Auto Interceptor) | `src/lib/api.ts`, `authStore.ts` | 🟢 Hoàn thành |
| **Step 2.3** | Di trú Giao diện Public (Trang chủ, chi tiết phim - SSR) | `src/app/page.tsx`, `[id]/page.tsx` | 🟢 Hoàn thành |
| **Step 2.4** | Di trú Giao diện Đặt vé & Thanh toán thời gian thực (WebSockets) | `src/app/booking/`, `src/app/payment/` | 🟢 Hoàn thành |
| **Step 2.5** | Di trú Giao diện POS bán vé tại quầy | `src/app/pos/page.tsx` | 🟢 Hoàn thành |
| **Step 2.6** | Di trú các trang quản trị của Admin / Manager | `src/app/profile/`, `src/app/history/` | 🟢 Hoàn thành |
| **Step 2.7** | Chạy kiểm thử tích hợp Selenium cho FE mới và dọn dẹp static cũ | Hệ thống | 🟢 Hoàn thành |

---

## 📌 Nhật Ký Hoạt Động (Activity Logs)

* **12/07/2026:** Khởi tạo tài liệu `CONDUCTOR.md`, hoàn thành refactor core logic đặt vé (dynamic pricing, N+1 query), gom hằng số Magic Strings, chuẩn hóa log bằng SLF4J cho 12 file nghiệp vụ chính, và tích hợp Flyway Migration/baseline V1 thành công.
* **12/07/2026 (CI/CD & Docker):** Docker hóa toàn bộ dự án Spring Boot Backend, MySQL, Redis qua `Dockerfile` và `docker-compose.yml`. Tích hợp kiểm thử Selenium tự động chạy headless qua `BookingSeleniumTest.java` và thiết lập GitHub Actions CI pipeline (`selenium.yml`). Chạy thử nghiệm test thành công local với kết quả BUILD SUCCESS.
* **12/07/2026 (Frontend Migration & E2E Validation):** Hoàn thành di trú toàn bộ legacy React/Vite cinema sang Next.js App Router. Tích hợp Axios API Client, cơ chế giữ ghế real-time qua WebSocket STOMP, trang thanh toán VietQR động kèm cơ chế tự động polling check trạng thái, trang bán vé tại quầy POS hoàn chỉnh cho Staff có in hóa đơn, trang cá nhân & lịch sử xem vé QR. Sửa lỗi build prerender (Suspense). Chạy MockMvc và E2E Selenium tests thành công vượt mong đợi với BUILD SUCCESS.
