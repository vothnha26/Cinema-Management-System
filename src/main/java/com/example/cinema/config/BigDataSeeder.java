package com.example.cinema.config;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.booking.BookingDetailRepository;
import com.example.cinema.repository.booking.BookingComboRepository;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.booking.PaymentRepository;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.commerce.BranchComboRepository;
import com.example.cinema.repository.commerce.ComboRepository;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.movie.BranchMovieRepository;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.repository.notification.NotificationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

//@org.springframework.context.annotation.Configuration
@org.springframework.core.annotation.Order(20) // Run after DataSeeder and PricingRuleSeeder
public class BigDataSeeder {

    @Bean
    public CommandLineRunner seedBigData(
            MovieRepository movieRepository,
            RoomRepository roomRepository,
            ShowtimeRepository showtimeRepository,
            BookingRepository bookingRepository,
            BookingDetailRepository bookingDetailRepository,
            BookingComboRepository bookingComboRepository,
            CustomerRepository customerRepository,
            MembershipLevelRepository membershipLevelRepository,
            UserRepository userRepository,
            SeatRepository seatRepository,
            ComboRepository comboRepository,
            PromotionRepository promotionRepository,
            PaymentRepository paymentRepository,
            BranchRepository branchRepository,
            BranchMovieRepository branchMovieRepository,
            BranchComboRepository branchComboRepository,
            NotificationRepository notificationRepository) {
        return args -> {
            System.out.println("🚀 Đang khởi động BigDataSeeder...");

            List<Branch> allBranches = branchRepository.findAll();
            if (allBranches.isEmpty()) {
                System.out.println("❌ Không tìm thấy Chi nhánh nào. Vui lòng chạy DataSeeder trước.");
                return;
            }

            // Đảm bảo tất cả phòng có chi nhánh
            List<Room> allRooms = roomRepository.findAll();
            for (Room room : allRooms) {
                if (room.getBranch() == null) {
                    room.setBranch(allBranches.get(0));
                    roomRepository.save(room);
                }
            }

            // 0. Tạo thêm Khách hàng nếu ít hơn 50
            if (customerRepository.count() < 50) {
                System.out.println("👥 Tạo thêm khách hàng ảo với tài khoản User...");
                String[] lastNames = { "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Phan", "Vũ", "Đặng", "Bùi", "Đỗ" };
                String[] middleNames = { "Văn", "Thị", "Đức", "Ngọc", "Minh", "Quang", "Hồng", "Tuấn", "Thanh", "Anh" };
                String[] firstNames = { "Anh", "Bình", "Chi", "Dũng", "Em", "Giang", "Hương", "Khánh", "Linh", "Minh",
                        "Nam", "Oanh", "Phúc", "Quân", "Sơn", "Trang", "Uyên", "Việt", "Xuân", "Yến" };

                Random random = new Random();
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                List<MembershipLevel> levels = membershipLevelRepository.findAll();

                for (int i = 1; i <= 50; i++) {
                    String fullName = lastNames[random.nextInt(lastNames.length)] + " " +
                            middleNames[random.nextInt(middleNames.length)] + " " +
                            firstNames[random.nextInt(firstNames.length)];
                    String username = "user" + String.format("%03d", i) + "_"
                            + UUID.randomUUID().toString().substring(0, 4);

                    // Tạo User trước
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setEmail(username + "@gmail.com");
                    user.setRole(Role.CUSTOMER);
                    user.setStatus(true);
                    User savedUser = userRepository.save(user);

                    // Tạo Customer liên kết với User
                    Customer c = new Customer();
                    c.setUser(savedUser);
                    c.setFullName(fullName);
                    c.setEmail(savedUser.getEmail());
                    c.setPhone("09" + (10000000 + random.nextInt(90000000)));
                    if (!levels.isEmpty()) {
                        c.setMembershipLevel(levels.get(random.nextInt(levels.size())));
                    }
                    c.setPoints(random.nextInt(1000));
                    c.setTotalSpending(BigDecimal.valueOf(random.nextInt(5000000)));
                    customerRepository.save(c);

                    // Tạo thông báo chào mừng
                    Notification n = new Notification();
                    n.setUser(savedUser);
                    n.setTitle("Chào mừng thành viên mới!");
                    n.setMessage("Chào mừng " + fullName + " đã gia nhập hệ thống StarCinema Elite!");
                    n.setType(NotificationType.SYSTEM);
                    n.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(n);
                }
                System.out.println("✅ Đã tạo 50 khách hàng thực tế kèm tài khoản đăng nhập và thông báo chào mừng.");
            }

            // 1. Tạo thêm Phim nếu ít hơn 15
            if (movieRepository.count() < 15) {
                System.out.println("🎬 Tạo thêm phim ảo...");
                String[] titles = { "Chiến binh cuối cùng", "Tình yêu sét đánh", "Hành trình vạn dặm",
                        "Bóng ma học đường", "Siêu anh hùng báo thù", "Thám tử lừng danh", "Cuộc chiến các vì sao",
                        "Thế giới khủng long", "Harry Potter", "Kẻ hủy diệt", "Avatar", "Oppenheimer", "Barbie",
                        "Inception", "Interstellar" };
                for (String title : titles) {
                    if (movieRepository.findByTitle(title).isEmpty()) {
                        Movie m = new Movie();
                        m.setTitle(title);
                        m.setDuration(90 + new Random().nextInt(90));
                        m.setStatus(MovieStatus.SHOWING);
                        m.setRating(7.0 + new Random().nextDouble() * 3.0);
                        m.setReleaseDate(LocalDate.now().minusMonths(new Random().nextInt(6)));
                        m.setAgeRating(AgeRating.values()[new Random().nextInt(AgeRating.values().length)]);
                        m.setPosterUrl("https://image.tmdb.org/t/p/w500/8Gxv8S7IlBoj19pYn3pkqJuL7Ar.jpg");
                        m.setOriginCountry(new Random().nextDouble() < 0.3 ? "VN" : "US");
                        m.setPriorityLevel(new Random().nextInt(5) + 1);
                        movieRepository.save(m);
                    }
                }
            }

            List<Movie> movies = movieRepository.findAll();
            List<Customer> customers = customerRepository.findAll();

            // Phân phối phim xuống TẤT CẢ chi nhánh
            System.out.println("📦 Phân phối phim xuống " + allBranches.size() + " chi nhánh...");
            for (Branch branch : allBranches) {
                for (Movie movie : movies) {
                    if (branchMovieRepository.findByBranchAndMovie(branch, movie).isEmpty()) {
                        BranchMovie bm = new BranchMovie();
                        bm.setBranch(branch);
                        bm.setMovie(movie);
                        bm.setStatus(movie.getStatus());
                        bm.setIsActive(true);
                        bm.setPriorityLevel(new Random().nextInt(5) + 1);
                        branchMovieRepository.save(bm);
                    }
                }
            }

            // 2. Kiểm tra nếu đã có suất chiếu cho hôm nay thì không tạo thêm dữ liệu lịch sử nữa (tránh spam)
            if (showtimeRepository.count() > 5000) {
                System.out.println("⏭️ Dữ liệu suất chiếu đã rất lớn (>5000), bỏ qua bước tạo thêm.");
                return;
            }

            System.out.println("📅 Tạo suất chiếu lịch sử (30 ngày) và thông báo đặt vé...");
            Random random = new Random();
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < 30; i++) {
                LocalDateTime date = now.minusDays(i).withHour(8).withMinute(0);
                for (Room room : allRooms) {
                    List<Seat> availableSeats = seatRepository.findByRoomId(room.getId());
                    if (availableSeats.isEmpty())
                        continue;

                    // Lấy các Combo của chi nhánh này
                    List<BranchCombo> branchCombos = branchComboRepository
                            .findByBranchAndIsActiveTrue(room.getBranch());

                    for (int j = 0; j < 4; j++) {
                        Movie movie = movies.get(random.nextInt(movies.size()));
                        LocalDateTime startTime = date.plusHours(j * 4);
                        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

                        Showtime showtime = new Showtime();
                        showtime.setMovie(movie);
                        showtime.setRoom(room);
                        showtime.setStartTime(startTime);
                        showtime.setEndTime(endTime);
                        showtime.setStatus(startTime.isBefore(now) ? ShowtimeStatus.ENDED : ShowtimeStatus.UPCOMING);
                        showtime.setTotalSeats(availableSeats.size());
                        showtime.setSoldSeats(0);
                        showtime = showtimeRepository.save(showtime);

                        // Ghế đã chọn cho suất chiếu này
                        Set<Integer> selected_seat_indexes = new HashSet<>();

                        // 3. Tạo Booking
                        int numBookings = random.nextInt(10) + 5;
                        int currentSold = 0;
                        for (int k = 0; k < numBookings; k++) {
                            Booking booking = new Booking();
                            booking.setShowtime(showtime);
                            booking.setBookingCode("BK" + showtime.getId() + "B" + k
                                    + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
                            booking.setCreatedAt(startTime.minusMinutes(random.nextInt(1440))); // Đặt vé trước đó

                            Customer bookingCustomer = null;
                            if (random.nextDouble() < 0.8 && !customers.isEmpty()) {
                                bookingCustomer = customers.get(random.nextInt(customers.size()));
                                booking.setCustomer(bookingCustomer);
                            }

                            if (startTime.isBefore(now)) {
                                booking.setStatus(
                                        random.nextDouble() < 0.9 ? BookingStatus.CONFIRMED : BookingStatus.CANCELLED);
                            } else {
                                booking.setStatus(BookingStatus.PENDING);
                            }

                            int ticketsCount = random.nextInt(3) + 1;
                            BigDecimal totalPrice = BigDecimal.ZERO;

                            // Tạo Booking Details (Vé)
                            List<BookingDetail> details = new ArrayList<>();
                            for (int t = 0; t < ticketsCount; t++) {
                                int seatIdx;
                                do {
                                    seatIdx = random.nextInt(availableSeats.size());
                                } while (selected_seat_indexes.contains(seatIdx)
                                        && selected_seat_indexes.size() < availableSeats.size());

                                if (selected_seat_indexes.size() >= availableSeats.size())
                                    break;
                                selected_seat_indexes.add(seatIdx);

                                Seat seat = availableSeats.get(seatIdx);
                                BookingDetail detail = new BookingDetail();
                                detail.setBooking(booking);
                                detail.setSeat(seat);
                                detail.setSeatCode(seat.getRowChar() + seat.getColNum());

                                // Giá vé giả lập
                                BigDecimal price = BigDecimal
                                        .valueOf(80000 + (seat.getSeatType().getId().equals("VIP") ? 20000 : 0));
                                detail.setPrice(price);
                                totalPrice = totalPrice.add(price);
                                details.add(detail);
                            }

                            // Tạo Booking Combo (Bắp nước)
                            List<BookingCombo> bCombos = new ArrayList<>();
                            if (random.nextDouble() < 0.4 && !branchCombos.isEmpty()) {
                                int numCombos = random.nextInt(2) + 1;
                                for (int c = 0; c < numCombos; c++) {
                                    BranchCombo bc = branchCombos.get(random.nextInt(branchCombos.size()));
                                    BookingCombo bCombo = new BookingCombo();
                                    bCombo.setBooking(booking);
                                    bCombo.setCombo(bc.getCombo());
                                    bCombo.setQuantity(random.nextInt(2) + 1);
                                    bCombo.setPrice(bc.getPrice());

                                    totalPrice = totalPrice
                                            .add(bc.getPrice().multiply(BigDecimal.valueOf(bCombo.getQuantity())));
                                    bCombos.add(bCombo);
                                }
                            }

                            booking.setTotalPrice(totalPrice);
                            booking = bookingRepository.save(booking);
                            bookingDetailRepository.saveAll(details);
                            bookingComboRepository.saveAll(bCombos);

                            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                                currentSold += details.size();

                                Payment payment = new Payment();
                                payment.setBooking(booking);
                                payment.setAmount(booking.getTotalPrice());
                                payment.setPaidAt(booking.getCreatedAt().plusMinutes(5));
                                payment.setPaymentMethod(
                                        random.nextDouble() < 0.5 ? PaymentMethod.CASH : PaymentMethod.VNPAY);
                                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                                payment.setTransactionId(
                                        "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                                paymentRepository.save(payment);

                                // Tạo thông báo đặt vé thành công
                                if (bookingCustomer != null) {
                                    Notification bn = new Notification();
                                    bn.setUser(bookingCustomer.getUser());
                                    bn.setTitle("Đặt vé thành công!");
                                    bn.setMessage("Bạn đã đặt thành công vé cho phim " + movie.getTitle() + ". Mã vé: " + booking.getBookingCode());
                                    bn.setType(NotificationType.BOOKING);
                                    bn.setCreatedAt(booking.getCreatedAt());
                                    notificationRepository.save(bn);
                                }
                            }
                        }
                        showtime.setSoldSeats(currentSold);
                        showtimeRepository.save(showtime);
                    }
                }
            }
            System.out.println("✅ HOÀN TẤT TẠO DỮ LIỆU LỚN VÀ THÔNG BÁO.");
        };
    }
}
