# 🎖️ StarCinema Conductor - Điều Phối Trạng Thái Phát Triển

Tệp này quản lý trạng thái phát triển và đối soát nghiệp vụ theo mandate **StarCinema Elite Engineering**. Quy trình chuẩn:
`Vẽ Sequence Diagram (SD) ──> Viết Code BE/FE ──> Automated Validation (MockMvc) ──> Đăng mã nguồn (Clean Push) ──> Cập nhật CONDUCTOR.md`.

---

## 📊 Bảng Theo Dõi Tiến Độ Step-by-Step

### 🔴 PHẦN 1: REFACTOR BACKEND (Spring Boot REST API)

| Bước         | Nhiệm vụ                                                                                  | Class liên quan                           | Trạng thái    |
| :----------- | :---------------------------------------------------------------------------------------- | :---------------------------------------- | :------------ |
| **Step 1.1** | Vẽ Sequence Diagram cho luồng Đặt vé Online & POS có áp dụng `PricingService`             | `uml/`                                    | 🟢 Hoàn thành |
| **Step 1.2** | Refactor `CustomerBookingFacade` (Tính giá vé động qua `PricingService`, xử lý N+1 query) | `CustomerBookingFacade.java`              | 🟢 Hoàn thành |
| **Step 1.3** | Refactor `StaffPosFacade` (Tính giá vé động qua `PricingService`, xử lý N+1 query)        | `StaffPosFacade.java`                     | 🟢 Hoàn thành |
| **Step 1.4** | Thêm các Query Method tránh N+1 load toàn bộ DB                                           | `CustomerRepository.java`                 | 🟢 Hoàn thành |
| **Step 1.5** | Tích hợp **Flyway Migration** để thay thế `ddl-auto=update`                               | `pom.xml`, `application.properties`       | 🟢 Hoàn thành |
| **Step 1.6** | Gom toàn bộ Magic Strings & Error Messages sang class Constant                            | `AppConstants.java`, `ErrorMessages.java` | 🟢 Hoàn thành |
| **Step 1.7** | Thay thế toàn bộ `System.err.println()` bằng SLF4J Logger                                 | Các classes BE                            | 🟢 Hoàn thành |
| **Step 1.8** | Cấu hình CORS Whitelist cho cổng chạy Frontend Next.js                                    | `SecurityConfig.java`                     | 🟢 Hoàn thành |
| **Step 1.9** | Chạy automated JUnit/MockMvc test cho toàn bộ luồng Backend                               | `src/test/java`                           | 🟢 Hoàn thành |
| **Step 1.10**| Tích hợp xác thực mã OTP, khôi phục mật khẩu và bảng `verification_codes`                 | `VerificationCode.java`, `AuthServiceImpl.java` | 🟢 Hoàn thành |

---

### 🟡 PHẦN 2: DI TRÚ FRONTEND SANG NEXT.JS (App Router)

| Bước         | Nhiệm vụ                                                                                                                                 | Thư mục/File liên quan                 | Trạng thái    |
| :----------- | :--------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------- | :------------ |
| **Step 2.1** | Khởi tạo dự án Next.js độc lập (`cinema-frontend`)                                                                                       | `cinema-frontend/`                     | 🟢 Hoàn thành |
| **Step 2.2** | Thiết lập API Client (Axios/Fetch + JWT Auto Interceptor)                                                                                | `src/lib/api.ts`, `authStore.ts`       | 🟢 Hoàn thành |
| **Step 2.3** | Di trú Giao diện Public (Trang chủ, chi tiết phim - SSR)                                                                                 | `src/app/page.tsx`, `[id]/page.tsx`    | 🟢 Hoàn thành |
| **Step 2.4** | Di trú Giao diện Đặt vé & Thanh toán thời gian thực (WebSockets)                                                                         | `src/app/booking/`, `src/app/payment/` | 🟢 Hoàn thành |
| **Step 2.5** | Di trú Giao diện POS bán vé tại quầy                                                                                                     | `src/app/pos/page.tsx`                 | 🟢 Hoàn thành |
| **Step 2.6** | Di trú trang cá nhân & Lịch sử đặt vé                                                                                                    | `src/app/profile/`, `src/app/history/` | 🟢 Hoàn thành |
| **Step 2.7** | Chạy kiểm thử tích hợp Selenium cho FE mới và dọn dẹp static cũ                                                                          | Hệ thống                               | 🟢 Hoàn thành |
| **Step 2.8** | Di trú toàn bộ phân hệ quản trị Admin/Manager (Phòng chiếu, Thiết kế ghế, Lịch chiếu, Đặt vé, Phân bổ phim, Hội viên, Nhật ký kiểm toán) | `src/app/admin/`                       | 🟢 Hoàn thành |
| **Step 2.9** | Thiết lập giao diện multi-step OTP & khôi phục mật khẩu ở frontend                                                                       | `src/app/auth/page.tsx`, `authService.ts` | 🟢 Hoàn thành |

---

## 📌 Nhật Ký Hoạt Động (Activity Logs)

- **12/07/2026:** Khởi tạo tài liệu `CONDUCTOR.md`, hoàn thành refactor core logic đặt vé (dynamic pricing, N+1 query), gom hằng số Magic Strings, chuẩn hóa log bằng SLF4J cho 12 file nghiệp vụ chính, và tích hợp Flyway Migration/baseline V1 thành công.
- **12/07/2026 (CI/CD & Docker):** Docker hóa toàn bộ dự án Spring Boot Backend, MySQL, Redis qua `Dockerfile` và `docker-compose.yml`. Tích hợp kiểm thử Selenium tự động chạy headless qua `BookingSeleniumTest.java` và thiết lập GitHub Actions CI pipeline (`selenium.yml`). Chạy thử nghiệm test thành công local với kết quả BUILD SUCCESS.
- **12/07/2026 (Frontend Migration & E2E Validation):** Hoàn thành di trú toàn bộ legacy React/Vite cinema sang Next.js App Router. Tích hợp Axios API Client, cơ chế giữ ghế real-time qua WebSocket STOMP, trang thanh toán VietQR động kèm cơ chế tự động polling check trạng thái, trang bán vé tại quầy POS hoàn chỉnh cho Staff có in hóa đơn, trang cá nhân & lịch sử xem vé QR. Sửa lỗi build prerender (Suspense). Chạy MockMvc và E2E Selenium tests thành công vượt mong đợi với BUILD SUCCESS.
- **13/07/2026 (Di Trú Admin Modules & Hoàn Tất Dự Án):** Hoàn thành di trú toàn bộ 7 phân hệ quản trị Admin/Manager sang Next.js App Router (bao gồm Rooms, Seat Designer, Showtimes Scheduler, Booking Manager, Distributions, Membership, Audit Logs). Xây dựng cấu trúc services API Type-Safe, cấu hình định tuyến RBAC Edge Middleware, tối ưu hóa SWR Cache và cơ chế phân trang Client-side kèm cảnh báo hiệu năng nợ kỹ thuật (Technical Debt) tự động. Dự án frontend đã biên dịch Next.js build thành công 100% không còn lỗi TypeScript.
- **13/07/2026 (Sửa lỗi Runtime React & Điều Chỉnh API Endpoint):** Khắc phục lỗi crash runtime do bóc tách sai trường dữ liệu response từ API (lỗi filter/map is not a function). Đồng thời điều chỉnh định tuyến gọi API của Quy tắc phụ thu về đúng endpoint `/api/admin/pricing-rules` ở backend thay vì gọi nhầm `/api/admin/pricing` (được phân tách cho giá ghế gốc). Dự án biên dịch Next.js build thành công 100% không còn lỗi TypeScript.
- **13/07/2026 (Hotfix API Route Mappings & MockMvc Test stabilization):** Điều chỉnh hằng số `ADMIN_API_ENDPOINTS` trong Next.js để ánh xạ chính xác các route của Phim (`/movies`), Combos bắp nước (`/combos`) và Khuyến mãi (`/promotions`) khớp với REST controllers ở backend (loại bỏ tiền tố `/admin` bị lỗi 500/404). Áp dụng pattern kiểm tra mảng an toàn `Array.isArray(res.data) ? res.data : []` tại trang khuyến mãi và combo để triệt tiêu lỗi crash giao diện. Đồng thời thêm MockBean `@MockBean CustomerRepository` vào `BookingControllerTest` giúp sửa lỗi tiêm phụ thuộc trong môi trường kiểm thử JUnit của backend. Chạy build frontend và kiểm thử backend đều đạt kết quả SUCCESS 100%.
- **13/07/2026 (Backend Auto-restart & Bug Resolution):** Thêm thư viện `spring-boot-devtools` vào `pom.xml` của backend để hỗ trợ tự động khởi động lại (auto-restart) khi thay đổi code. Xác nhận lỗi tĩnh `NoResourceFoundException: No static resource api/admin/combos` trong `bug.txt` đã được giải quyết triệt để nhờ việc chuyển đổi thành công đường dẫn API `/api/combos` ở đầu ngày.
- **13/07/2026 (Fix Hydration Mismatch React Error #418):** Giải quyết lỗi cảnh báo Hydration Mismatch (`React error #418`) bằng cách giới hạn việc render có điều kiện theo vai trò (Zustand state `userRole` & `isAdmin`) chỉ hoạt động sau khi component đã mount trên client (`mounted` state) tại `AdminSidebar`, `AdminCombosPage` và `Navbar` (khu vực hiển thị nút Đăng nhập / Profile).
- **13/07/2026 (Migrate deprecated Next.js middleware to proxy):** Chuyển đổi tệp cấu hình trung gian từ `middleware.ts` sang `proxy.ts` (và hàm xử lý từ `middleware` sang `proxy`) theo chuẩn Next.js 16+ để triệt tiêu hoàn toàn cảnh báo deprecation khi build frontend.
- **13/07/2026 (Tối ưu hóa Database Schema & Di Trú Khóa Chính):** Hoàn tất tối ưu hóa lược đồ cơ sở dữ liệu. Di trú `room_types` và `seat_types` sang khóa chính surrogate kiểu `Long` kết hợp trường `code` nghiệp vụ, đồng bộ timestamps audit cho các bảng master data. Cập nhật repository, logic mapper tương thích ngược, và sửa lỗi tương thích kiểu dữ liệu trong toàn bộ service layer. Tất cả các test JUnit/Selenium backend và build Next.js frontend đều đạt kết quả BUILD SUCCESS 100%.
- **13/07/2026 (Database Schema Enrichment & Constraint Hardening):** Bổ sung các thuộc tính hỗ trợ nghiệp vụ cho các bảng (bookings, payments, membership_levels, branches, customers, movies, users, promotions, audit_logs) thông qua migration `V2__enrich_schema.sql` của Flyway. Đồng bộ hóa JPA entities, chuẩn hóa logic mapping của `Combo.java` giữ tính tương thích ngược tốt, và tích hợp liên kết thực thể User vào `AuditLog` và `AuditLogAspect.java`. Tất cả kiểm thử backend và build Next.js frontend thành công 100%.
- **13/07/2026 (Giải Quyết Trùng Lặp Dữ Liệu & Chuẩn Hóa Bắp Nước):** Khắc phục triệt để conflict "dual source of truth" bằng cách loại bỏ các cột `price` và `stock_quantity` khỏi bảng danh mục `combos` trong `V2__enrich_schema.sql`. Cập nhật thực thể JPA `Combo.java`, đồng thời refactor các dịch vụ `ComboServiceImpl.java`, `CustomerBookingFacade.java`, và `StaffPosFacade.java` sử dụng hoàn toàn `BranchCombo` để xử lý giá và tồn kho theo từng chi nhánh. Biên dịch backend (`mvn clean test`) và frontend (`npm run build`) đều thành công 100%.
- **13/07/2026 (Tích hợp Secure OTP & Khôi Phục Mật Khẩu):** Triển khai toàn bộ giải pháp xác thực bảo mật tài khoản bằng OTP. Thiết lập bảng `verification_codes` theo dõi vòng đời mã OTP và mục đích (`REGISTER`, `RESET_PASSWORD`). Tích hợp logic yêu cầu xác thực email bắt buộc khi đăng ký, khóa tài khoản chưa xác thực lúc đăng nhập kèm kích hoạt OTP mới, và quy trình khôi phục mật khẩu (Quên mật khẩu -> Gửi OTP -> Xác thực OTP -> Đặt mật khẩu mới) ở Backend và Frontend. Tạo bộ kiểm thử MockMvc `AuthControllerTest` độc lập. Kết quả chạy `mvn test` backend và build Next.js frontend thành công đạt SUCCESS 100%.
