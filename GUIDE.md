**ĐỀ MẪU DỰ ÁN**

**CINEMA MANAGEMENT SYSTEM**

**Hệ thống quản lý rạp phim**

Spring Boot REST API + HTML/CSS/JS + Bootstrap + MySQL

**Nhóm 3 sinh viên | Thời lượng: 3-4 tuần**

# **1\. Mục tiêu dự án**

Xây dựng một ứng dụng web quản lý rạp phim, hỗ trợ toàn bộ quy trình từ quản lý phim, lịch chiếu, đặt vé trực tuyến đến bán vé tại quầy và thống kê doanh thu.

**Các nghiệp vụ chính được hỗ trợ:**

- Quản lý phim, thể loại, đạo diễn (nhiều đạo diễn / phim), diễn viên.
- Quản lý phòng chiếu, sơ đồ ghế ngồi.
- Quản lý bảng giá vé linh hoạt theo loại phòng và loại ghế.
- Tạo và quản lý suất chiếu theo lịch.
- Đặt vé trực tuyến: chọn ghế, combo bắp nước, áp mã khuyến mãi, thanh toán đa phương thức.
- Bán vé tại quầy (POS đơn giản dành cho nhân viên).
- Quản lý khách hàng và chương trình thành viên tích điểm.
- Quản lý chương trình khuyến mãi & mã giảm giá.
- Hệ thống thông báo tự động (xác nhận đặt vé, nhắc lịch chiếu, ưu đãi).
- Thống kê doanh thu, báo cáo theo phim, theo ngày, theo phương thức thanh toán.

**Dự án được phát triển theo mô hình và công nghệ:**

- Spring Boot: REST API backend (Controller - Service - Repository).
- HTML/CSS/JS thuần + Bootstrap 5: Giao diện người dùng (Frontend).
- Spring Data JPA / Hibernate: ORM, tương tác cơ sở dữ liệu.
- MySQL 8: Lưu trữ dữ liệu.
- Maven: Quản lý thư viện và vòng đời dự án.
- Spring Security: Phân quyền người dùng (Admin / Manager / Staff / Customer).
- SOLID: Áp dụng các nguyên tắc thiết kế phần mềm sạch.

# **2\. Quy mô và Thời gian**

| **Tiêu chí**    | **Chi tiết**                                                                                                                   |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| Nhóm            | 3 sinh viên                                                                                                                    |
| Thời lượng      | 3-4 tuần (đề xuất)                                                                                                             |
| Phân công gợi ý | SV1: Backend API phim, suất chiếu, phòng chiếu SV2: Backend đặt vé, thành viên, combo SV3: Frontend toàn bộ + báo cáo thống kê |
| Mức độ          | Trung bình - Nâng cao (có thanh toán, combo, phân quyền 4 role)                                                                |

# **3\. Yêu cầu công nghệ**

## **3.1. Công nghệ bắt buộc**

- Ngôn ngữ: Java 17 hoặc Java 21.
- Framework Backend: Spring Boot 3.x.
- Giao diện Frontend: HTML/CSS/JavaScript thuần + Bootstrap 5.
- Công cụ quản lý: Maven.
- Cơ sở dữ liệu: MySQL 8.
- ORM: Spring Data JPA / Hibernate.
- Bảo mật: Spring Security (JWT hoặc Session-based).
- IDE: IntelliJ IDEA.

## **3.2. Thư viện đề xuất (Dependencies)**

| **Dependency**                 | **Mục đích**                             |
| ------------------------------ | ---------------------------------------- |
| spring-boot-starter-web        | REST API Controller                      |
| spring-boot-starter-data-jpa   | Spring Data JPA + Hibernate ORM          |
| spring-boot-starter-security   | Phân quyền & xác thực người dùng         |
| mysql-connector-j              | Kết nối MySQL                            |
| lombok                         | Giảm boilerplate (Getter/Setter/Builder) |
| modelmapper hoặc mapstruct     | Chuyển đổi Entity &lt;-&gt; DTO          |
| spring-boot-starter-validation | Validate dữ liệu đầu vào (@Valid)        |
| jjwt (tùy chọn)                | JSON Web Token cho xác thực stateless    |

# **4\. Bài toán nghiệp vụ**

Ứng dụng phục vụ 4 nhóm người dùng với các quyền hạn khác nhau:

| **Role** | **Tên gọi**   | **Quyền hạn chính**                                                      |
| -------- | ------------- | ------------------------------------------------------------------------ |
| ADMIN    | Quản trị viên | Toàn quyền: quản lý người dùng, cấu hình hệ thống, xem mọi báo cáo.      |
| MANAGER  | Quản lý rạp   | Quản lý phim, suất chiếu, phòng chiếu. Xem thống kê & báo cáo doanh thu. |
| STAFF    | Nhân viên     | Bán vé tại quầy (POS), check-in vé, xem lịch chiếu trong ngày.           |
| CUSTOMER | Khách hàng    | Đặt vé online, xem lịch sử đặt vé, tích điểm thành viên, dùng ưu đãi.    |

# **5\. Chức năng chi tiết**

## **5.1. Quản lý Phim & Danh mục**

- Phim (Movie): Thêm, sửa, xóa, xem danh sách phim. Tìm kiếm theo tên, thể loại, trạng thái (Đang chiếu / Sắp chiếu / Ngừng chiếu).
- Thể loại (Genre): Quản lý danh mục thể loại phim (Hành động, Tình cảm, Kinh dị...).
- Đạo diễn (Director): Quản lý thông tin đạo diễn, liên kết với phim qua bảng `movie_directors` (M-M). Một phim có thể có nhiều đạo diễn với vai trò MAIN / CO_DIRECTOR.
- Diễn viên (Actor): Quản lý thông tin, liên kết với phim qua `movie_actors` (M-M), lưu tên nhân vật và thứ tự hiển thị.
- Thông tin phim: Tiêu đề, mô tả, thời lượng, ngôn ngữ, giới hạn độ tuổi (P, C13, C16, C18), poster, trailer URL.

## **5.2. Quản lý Rạp, Phòng chiếu & Ghế**

- Phòng chiếu (Room): Quản lý các phòng (số phòng, loại phòng: 2D, 3D, IMAX, 4DX, tổng số ghế).
- Sơ đồ ghế (Seat): Mỗi ghế có mã (A1, B2...), loại ghế (Standard, VIP, Couple), trạng thái.

## **5.3. Quản lý Suất chiếu (Showtime)**

- Tạo suất chiếu: chọn phim, phòng chiếu, ngày giờ bắt đầu, giá vé áp dụng.
- Kiểm tra xung đột lịch chiếu (cùng phòng trùng giờ).
- Cập nhật trạng thái suất chiếu: Sắp chiếu / Đang chiếu / Đã kết thúc / Huỷ.

## **5.4. Đặt vé & Bán vé (Booking)**

- Luồng đặt vé online (Customer): Chọn phim → Chọn suất chiếu → Chọn ghế → Chọn combo (tùy chọn) → Nhập mã khuyến mãi (tùy chọn) → Xác nhận & thanh toán → Nhận mã đặt vé.
- Combo bắp nước: Quản lý combo (Vừa, Lớn, Đặc biệt), giá combo; thêm combo vào đơn đặt vé.
- Ưu đãi thành viên: Tự động áp dụng giảm giá dựa trên hạng thành viên (Silver, Gold, Platinum).
- Thanh toán (Payment): Hỗ trợ đa phương thức CASH / CARD / MOMO / VNPAY. Mỗi booking tạo 1 bản ghi payment riêng, lưu transaction_id và trạng thái (PENDING → SUCCESS / FAILED).
- Bán vé tại quầy (Staff - POS): Tìm suất chiếu, chọn ghế còn trống, thanh toán tiền mặt, xác nhận in vé.
- Mã đặt vé: Mỗi booking có mã duy nhất dùng để check-in (Staff quét/nhập mã xác nhận).
- Hủy vé: Customer có thể hủy vé trước giờ chiếu; hệ thống cập nhật trạng thái booking và payment (REFUNDED).

## **5.5. Quản lý Khách hàng & Thành viên**

- Đăng ký / đăng nhập tài khoản khách hàng.
- Thông tin cá nhân, lịch sử đặt vé, lịch sử thanh toán.
- Hệ thống điểm tích lũy: Mỗi booking thành công cộng điểm (1 điểm / 10,000đ).
- Hạng thành viên: Standard → Silver (≥500k) → Gold (≥2tr) → Platinum (≥5tr) (dựa trên tổng chi tiêu).
- Ưu đãi theo hạng: Silver −5%, Gold −10%, Platinum −15% trên giá vé.

## **5.6. Thống kê & Báo cáo (Manager/Admin)**

- Doanh thu theo ngày, tuần, tháng, năm (lấy từ bảng `payments`).
- Doanh thu theo từng bộ phim.
- Tỷ lệ lấp đầy ghế (occupancy rate) theo suất chiếu, theo phòng.
- Top phim ăn khách (theo số vé bán / doanh thu).
- Doanh thu combo bắp nước.
- Thống kê theo phương thức thanh toán (CASH / MOMO / VNPAY...).
- Thống kê khách hàng mới đăng ký theo kỳ.

## **5.7. Quản lý Bảng giá vé (SeatPrice) [MỚI]**

- Quản lý giá vé theo tổ hợp `(loại phòng, loại ghế)`: ví dụ IMAX + VIP = 180,000đ.
- Cho phép cập nhật giá tại từng thời điểm (`effective_date`) mà không xóa lịch sử.
- Hệ thống tự động tra cứu giá đúng nhất (is_active = true, effective_date ≤ ngày đặt vé).
- MANAGER có quyền thêm / sửa / vô hiệu hóa bảng giá.

## **5.8. Quản lý Khuyến mãi & Mã giảm giá (Promotion) [MỚI]**

- Tạo chương trình khuyến mãi với mã code duy nhất (ví dụ: SUMMER25).
- Hai loại giảm giá: PERCENT (%) hoặc FIXED (số tiền cố định).
- Giới hạn hạng thành viên tối thiểu (`min_tier`): chỉ Silver trở lên mới dùng được.
- Giới hạn thời gian: `start_date` → `end_date`.
- Customer nhập mã khi đặt vé; hệ thống tự validate và áp dụng.
- MANAGER có quyền tạo / sửa / tắt chương trình khuyến mãi.

## **5.9. Hệ thống Thông báo (Notification) [MỚI]**

- Tự động tạo thông báo sau các sự kiện: đặt vé thành công, hủy vé, nâng hạng thành viên.
- Các loại thông báo: BOOKING (xác nhận vé), REMINDER (nhắc lịch chiếu), PROMOTION (ưu đãi mới), SYSTEM.
- Customer xem danh sách thông báo, đánh dấu đã đọc.
- API hỗ trợ: lấy thông báo chưa đọc, đánh dấu tất cả là đã đọc.

# **6\. Yêu cầu phi chức năng & Kiến trúc**

- REST API: Backend cung cấp API JSON thuần, không render HTML từ server.
- Giao diện: Responsive Bootstrap 5, thân thiện người dùng. Sơ đồ ghế render bằng JS thuần (grid HTML/CSS).
- Bảo mật: Xác thực bằng JWT hoặc Session. Phân quyền theo Role (ADMIN, MANAGER, STAFF, CUSTOMER).
- Mã nguồn: Không viết logic nghiệp vụ bên trong Controller. Controller chỉ nhận request -> gọi Service -> trả response.
- Tách lớp rõ rệt theo kiến trúc 3 tầng + Repository pattern.
- Validation: Validate đầu vào tại cả Frontend (JS) và Backend (@Valid + BindingResult).
- Xử lý lỗi tập trung: Dùng @ControllerAdvice / @ExceptionHandler để trả lỗi JSON thống nhất.

# **7\. Cấu trúc Package đề xuất**

Tổ chức theo Feature-based (hoặc Layer-based) giúp tuân thủ Separation of Concerns:

src/main/java

└── com.example.cinema

├── config # SecurityConfig, JwtConfig, CorsConfig

├── model

│ ├── entity # @Entity: Movie, Room, Seat, Showtime, Booking...

│ └── dto # Request/Response DTO (BookingRequest, ShowtimeDTO...)

├── repository # JpaRepository interfaces

├── service

│ ├── MovieService.java (interface)

│ ├── BookingService.java (interface)

│ └── impl # MovieServiceImpl, BookingServiceImpl...

├── controller # REST Controllers (@RestController)

│ ├── MovieController.java

│ ├── ShowtimeController.java

│ ├── BookingController.java

│ └── StatisticsController.java

├── security # JwtFilter, UserDetailsServiceImpl

├── exception # GlobalExceptionHandler, AppException

├── util # Helper classes

└── CinemaApplication.java

src/main/resources

├── application.properties

└── static # HTML/CSS/JS Frontend files

├── index.html

├── css/

└── js/

# **8\. Thiết kế Cơ sở dữ liệu (ERD)**

## **8.1. Các bảng chính**

- Phim & Danh mục: movies, genres, directors, actors, movie_genres (M-M), movie_actors (M-M), **movie_directors (M-M)**.
- Rạp chiếu: rooms, seats (loại ghế, trạng thái, mã ghế), **seat_prices (giá vé theo loại ghế & loại phòng)**.
- Lịch chiếu: showtimes (phim + phòng + giờ chiếu + giá cơ bản).
- Đặt vé: bookings (thông tin chung), booking_details (chi tiết từng ghế), booking_combos (combo đã chọn), **payments (lịch sử thanh toán)**.
- Combo: combos (tên, giá, mô tả).
- Người dùng: users (thông tin đăng nhập, role), customers (thông tin thành viên, điểm, hạng).
- Khuyến mãi & Thông báo: **promotions (chương trình ưu đãi, mã giảm giá)**, **notifications (thông báo đặt vé, nhắc lịch chiếu)**.

## **8.2. Bảng mô tả chi tiết các Entity chính (20 bảng)**

| **Bảng**          | **Khóa chính** | **Các cột quan trọng**                                                                        |
| ----------------- | -------------- | --------------------------------------------------------------------------------------------- |
| movies            | id (BIGINT)    | title, description, duration, language, age_rating, status, poster_url, trailer_url           |
| genres            | id (BIGINT)    | name, description                                                                             |
| directors         | id (BIGINT)    | full_name, biography, nationality, birth_date                                                 |
| actors            | id (BIGINT)    | full_name, biography, nationality, birth_date, avatar_url                                     |
| movie_genres      | (movie_id, genre_id) | Bảng trung gian M-M: movie_id (FK), genre_id (FK)                                     |
| movie_actors      | (movie_id, actor_id) | Bảng trung gian M-M: movie_id (FK), actor_id (FK), character_name, display_order     |
| movie_directors   | (movie_id, director_id) | **[MỚI]** Bảng trung gian M-M: movie_id (FK), director_id (FK), role (MAIN/CO)   |
| rooms             | id (BIGINT)    | name, room_type (2D/3D/IMAX/4DX), total_seats, status                                        |
| seats             | id (BIGINT)    | room_id (FK), seat_code (A1...), seat_type (STANDARD/VIP/COUPLE), row_no, col_no              |
| seat_prices       | id (BIGINT)    | **[MỚI]** room_type (FK/ENUM), seat_type (ENUM), price, effective_date, is_active             |
| showtimes         | id (BIGINT)    | movie_id (FK), room_id (FK), start_time, end_time, base_price, status                        |
| bookings          | id (BIGINT)    | customer_id (FK), showtime_id (FK), promotion_id (FK), booking_code, total_price, status, created_at |
| booking_details   | id (BIGINT)    | booking_id (FK), seat_id (FK), price, seat_type                                               |
| booking_combos    | id (BIGINT)    | booking_id (FK), combo_id (FK), quantity, unit_price                                          |
| combos            | id (BIGINT)    | name, description, price, image_url, is_active                                                |
| payments          | id (BIGINT)    | **[MỚI]** booking_id (FK-unique), amount, payment_method (CASH/CARD/MOMO/VNPAY), payment_status, transaction_id, paid_at |
| promotions        | id (BIGINT)    | **[MỚI]** code, name, discount_type (PERCENT/FIXED), discount_value, min_tier, start_date, end_date, is_active |
| users             | id (BIGINT)    | username, password (hashed), email, role (ENUM), is_active                                    |
| customers         | id (BIGINT)    | user_id (FK-unique), full_name, phone, points, membership_tier, total_spent                   |
| notifications     | id (BIGINT)    | **[MỚI]** user_id (FK), title, message, type (BOOKING/REMINDER/PROMOTION), is_read, created_at |

# **9\. Mô hình thực thể JPA (Entities)**

**Class Diagram phải khớp 1:1 với ERD để Hibernate ánh xạ chính xác.**

## **9.1. Các quan hệ JPA quan trọng**

- Many-to-One / One-to-Many: Showtime -> Movie, Showtime -> Room, BookingDetail -> Booking, BookingDetail -> Seat, Notification -> User.
- Many-to-Many: Movie <-> Genre, Movie <-> Actor, **Movie <-> Director** (dùng @ManyToMany + @JoinTable).
- Composition: Booking chứa List<BookingDetail> và List<BookingCombo> (cascade = ALL).
- One-to-One: User <-> Customer (mỗi tài khoản khách hàng có 1 profile thành viên). **Booking <-> Payment** (mỗi booking có 1 bản ghi thanh toán).
- **[MỚI]** Many-to-One: Booking -> Promotion (có thể null nếu không áp ưu đãi code).
- **[MỚI]** SeatPrice tra cứu theo (room_type, seat_type) để xác định đơn giá vé.

## **9.2. Ví dụ Entity Showtime**

@Entity

@Table(name = "showtimes")

@Data @NoArgsConstructor

public class Showtime {

@Id @GeneratedValue(strategy = GenerationType.IDENTITY)

private Long id;

@ManyToOne(fetch = FetchType.LAZY)

@JoinColumn(name = "movie_id", nullable = false)

private Movie movie;

@ManyToOne(fetch = FetchType.LAZY)

@JoinColumn(name = "room_id", nullable = false)

private Room room;

private LocalDateTime startTime;

private LocalDateTime endTime;

private BigDecimal basePrice;

@Enumerated(EnumType.STRING)

private ShowtimeStatus status; // UPCOMING, SHOWING, ENDED, CANCELLED

}

# **10\. Sơ đồ lớp (Class Diagram) và Logic Nghiệp vụ**

Để tuân thủ SOLID, các lớp được chia thành 3 nhóm trách nhiệm chính:

- Nhóm Entity: Chứa dữ liệu thuần túy (Movie, Showtime, Booking, Customer...).
- Nhóm Nghiệp vụ (Logic):

- Repository: Interface kế thừa JpaRepository, chứa các phương thức query (findByStatus, findAvailableSeats...).
- Service (Interface + Impl): Chứa luật nghiệp vụ - ví dụ: kiểm tra ghế còn trống trước khi đặt, tính giá theo hạng thành viên, cộng điểm sau khi thanh toán.
- Controller (@RestController): Nhận HTTP request, gọi Service, trả JSON response. Không chứa logic.

- Nhóm Phụ trợ:

- SecurityConfig: Cấu hình Spring Security, phân quyền endpoint theo Role.
- GlobalExceptionHandler: Bắt lỗi toàn cục, trả response lỗi JSON chuẩn.
- DTOs: Tách biệt dữ liệu truyền vào/ra khỏi Entity nội bộ.

# **11\. Biểu đồ Use Case**

Phân quyền hệ thống dựa trên 4 Actor:

| **Actor** | **Các Use Case**                                                                                                                                                                                                                  |
| --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| CUSTOMER  | Đăng ký / Đăng nhập. Xem danh sách phim đang chiếu / sắp chiếu. Xem lịch chiếu & sơ đồ ghế. Đặt vé online (chọn ghế + combo + áp ưu đãi). Xem lịch sử đặt vé. Hủy vé (trước giờ chiếu). Xem thông tin thành viên & điểm tích lũy. |
| STAFF     | Toàn bộ quyền CUSTOMER (không bao gồm phần thành viên). Bán vé tại quầy (POS). Check-in vé bằng mã đặt vé. Xem lịch chiếu trong ngày.                                                                                             |
| MANAGER   | Quản lý phim (CRUD). Quản lý phòng chiếu & ghế. Tạo & quản lý suất chiếu. Quản lý combo bắp nước. Xem thống kê doanh thu & báo cáo. Quản lý thông tin khách hàng.                                                                 |
| ADMIN     | Toàn bộ quyền MANAGER. Quản lý tài khoản người dùng (CRUD). Phân quyền Role. Cấu hình hệ thống (hạng thành viên, tỷ lệ điểm...).                                                                                                  |

# **12\. Biểu đồ Tuần tự (Sequence Diagram): Luồng đặt vé online**

Đây là luồng nghiệp vụ quan trọng nhất, mô tả cách các lớp tương tác khi khách hàng đặt vé.

## **Luồng xử lý (Workflow):**

- Customer gửi POST /api/bookings với thông tin: showtime_id, danh sách seat_id\[\], combo\[\], customer_id.
- BookingController nhận request, validate DTO (@Valid), gọi BookingService.createBooking(request).
- BookingService thực hiện 4 bước kiểm tra nghiệp vụ:

- Kiểm tra suất chiếu có tồn tại và trạng thái là UPCOMING không?
- Kiểm tra từng ghế có available (chưa bị đặt trong suất chiếu này) không? \[gọi BookingDetailRepository\]
- Tính tổng giá: (giá ghế \* số ghế) + giá combo - ưu đãi hạng thành viên.
- Kiểm tra khách hàng có đủ điều kiện dùng ưu đãi không?

- Nếu hợp lệ: BookingRepository lưu Booking + BookingDetail + BookingCombo vào DB (transaction).
- CustomerService cộng điểm tích lũy cho khách hàng, cập nhật hạng thành viên nếu đủ điều kiện.
- Trả về BookingResponse (booking_code, tổng tiền, danh sách ghế, trạng thái) cho Frontend.
- Frontend hiển thị xác nhận đặt vé + mã booking.

# **13\. Áp dụng nguyên tắc SOLID**

| **Nguyên tắc** | **Viết tắt**          | **Áp dụng trong dự án**                                                                                                  |
| -------------- | --------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| S              | Single Responsibility | BookingService chỉ xử lý logic đặt vé. MembershipService chỉ xử lý tích điểm & hạng. Không gộp chung.                    |
| O              | Open/Closed           | Thêm loại ưu đãi mới (sinh nhật, mã giảm giá...) bằng cách tạo thêm class implement DiscountStrategy, không sửa code cũ. |
| L              | Liskov Substitution   | Các lớp ServiceImpl có thể thay thế hoàn toàn cho Interface Service tương ứng mà không làm vỡ hành vi.                   |
| I              | Interface Segregation | Tách BookingService và BookingQueryService - không bắt buộc implement phương thức không dùng.                            |
| D              | Dependency Inversion  | BookingController phụ thuộc vào BookingService (interface), không phụ thuộc trực tiếp BookingServiceImpl.                |

# **14\. Danh sách REST API Endpoints chính**

| **Method** | **Endpoint**                 | **Role**       | **Mô tả**                                      |
| ---------- | ---------------------------- | -------------- | ---------------------------------------------- |
| GET        | /api/movies                  | PUBLIC         | Lấy danh sách phim (filter theo status, genre) |
| POST       | /api/movies                  | MANAGER/ADMIN  | Thêm phim mới                                  |
| GET        | /api/showtimes               | PUBLIC         | Lấy lịch chiếu (filter theo movie, date)       |
| GET        | /api/showtimes/{id}/seats    | PUBLIC         | Lấy sơ đồ ghế còn trống của suất chiếu         |
| POST       | /api/bookings                | CUSTOMER/STAFF | Tạo đặt vé mới                                 |
| GET        | /api/bookings/{code}         | CUSTOMER/STAFF | Lấy thông tin đặt vé theo mã                   |
| PUT        | /api/bookings/{code}/checkin | STAFF          | Check-in vé (đổi trạng thái -> CHECKED_IN)     |
| GET        | /api/combos                  | PUBLIC         | Lấy danh sách combo đang active                |
| GET        | /api/customers/me            | CUSTOMER       | Thông tin thành viên + điểm + hạng             |
| GET        | /api/statistics/revenue      | MANAGER/ADMIN  | Thống kê doanh thu (query by date range)       |

# **15\. Ví dụ Interface và Lớp triển khai (Service Layer)**

Tuân thủ Dependency Inversion (D trong SOLID): Controller gọi thông qua Interface.

## **15.1. BookingService Interface**

public interface BookingService {

BookingResponse createBooking(BookingRequest request);

BookingResponse getBookingByCode(String bookingCode);

void cancelBooking(String bookingCode, Long customerId);

void checkIn(String bookingCode);

List&lt;BookingResponse&gt; getBookingsByCustomer(Long customerId);

}

## **15.2. BookingServiceImpl - Đoạn logic kiểm tra ghế**

@Service

@RequiredArgsConstructor

@Transactional

public class BookingServiceImpl implements BookingService {

private final BookingRepository bookingRepository;

private final ShowtimeRepository showtimeRepository;

private final SeatRepository seatRepository;

private final CustomerService customerService;

@Override

public BookingResponse createBooking(BookingRequest request) {

Showtime showtime = showtimeRepository.findById(request.getShowtimeId())

.orElseThrow(() -> new AppException("Suất chiếu không tồn tại"));

// Kiểm tra ghế còn trống

List&lt;Long&gt; bookedSeatIds = bookingRepository

.findBookedSeatIdsByShowtime(request.getShowtimeId());

boolean conflict = request.getSeatIds().stream()

.anyMatch(bookedSeatIds::contains);

if (conflict) throw new AppException("Ghế đã được đặt, vui lòng chọn ghế khác");

// Tính giá & áp ưu đãi thành viên

BigDecimal discount = customerService

.getMembershipDiscount(request.getCustomerId());

// ... tạo và lưu Booking ...

}

}

# **16\. Gợi ý phân công công việc (3 sinh viên)**

| **Thành viên** | **Phụ trách**                               | **Chi tiết công việc**                                                                                                                                                       |
| -------------- | ------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Sinh viên 1    | Backend: Phim, Lịch chiếu, Phòng/Ghế        | Entity + Repository + Service + Controller cho: Movie, Genre, Director, Actor, Room, Seat, Showtime. API quản lý lịch chiếu, kiểm tra xung đột giờ chiếu.                    |
| Sinh viên 2    | Backend: Đặt vé, Combo, Thành viên, Bảo mật | Entity + Repository + Service + Controller cho: Booking, BookingDetail, BookingCombo, Combo, Customer, Membership. Spring Security + JWT. Logic tính giá, tích điểm, ưu đãi. |
| Sinh viên 3    | Frontend toàn bộ + Thống kê                 | Tất cả màn hình HTML/CSS/JS + Bootstrap: trang chủ, lịch chiếu, sơ đồ ghế (JS grid), đặt vé, quản lý (CRUD), dashboard thống kê (Chart.js). Tích hợp API với fetch/axios.    |

**Lưu ý: Mỗi thành viên cần viết Unit Test cơ bản cho phần Service của mình (JUnit 5 + Mockito).**

# **17\. Tiêu chí đánh giá**

| **Tiêu chí**                                           | **Điểm tối đa** | **Ghi chú**  |
| ------------------------------------------------------ | --------------- | ------------ |
| Đầy đủ chức năng CRUD phim, phòng, suất chiếu          | 20đ             | Bắt buộc     |
| Luồng đặt vé hoàn chỉnh (chọn ghế + combo + tính tiền) | 25đ             | Bắt buộc     |
| Hệ thống thành viên, tích điểm, ưu đãi theo hạng       | 15đ             | Nhóm 3 người |
| Thống kê & báo cáo doanh thu (biểu đồ)                 | 15đ             | Nhóm 3 người |
| Phân quyền đúng (Spring Security, 4 Role)              | 10đ             | Nhóm 3 người |
| Giao diện Bootstrap responsive, sơ đồ ghế trực quan    | 10đ             | Khuyến khích |
| Mã nguồn sạch, đúng kiến trúc MVC, áp dụng SOLID       | 5đ              | Bắt buộc     |