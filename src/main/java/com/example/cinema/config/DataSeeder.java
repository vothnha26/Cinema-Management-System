package com.example.cinema.config;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.repository.movie.GenreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      CustomerRepository customerRepository,
                                      MembershipBenefitRepository membershipBenefitRepository,
                                      PromotionRepository promotionRepository,
                                      GenreRepository genreRepository,
                                      SeatPriceRepository seatPriceRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Khởi tạo Thể loại (Genres)
            if (genreRepository.count() == 0) {
                Arrays.asList("Hành động", "Hài hước", "Kinh dị", "Tình cảm", "Viễn tưởng", "Hoạt hình", "Phiêu lưu", "Tâm lý")
                        .forEach(name -> genreRepository.save(new Genre(null, name)));
                System.out.println("✅ Đã khởi tạo danh sách Thể loại phim");
            }

            // 2. Khởi tạo Quyền lợi thành viên (Membership Benefits)
            if (membershipBenefitRepository.count() == 0) {
                membershipBenefitRepository.save(new MembershipBenefit(MembershipTier.STANDARD, 0.0));
                membershipBenefitRepository.save(new MembershipBenefit(MembershipTier.SILVER, 5.0));
                membershipBenefitRepository.save(new MembershipBenefit(MembershipTier.GOLD, 10.0));
                MembershipBenefit platinum = new MembershipBenefit(MembershipTier.PLATINUM, 15.0);
                platinum.setPointMultiplier(1.5);
                membershipBenefitRepository.save(platinum);
                System.out.println("✅ Đã khởi tạo Membership Benefits");
            }

            // 3. Khởi tạo Giá vé mặc định (Seat Prices)
            if (seatPriceRepository.count() == 0) {
                seatPriceRepository.save(new SeatPrice(null, RoomType.HALL_2D, SeatType.STANDARD, BigDecimal.valueOf(85000), LocalDate.now()));
                seatPriceRepository.save(new SeatPrice(null, RoomType.HALL_2D, SeatType.VIP, BigDecimal.valueOf(110000), LocalDate.now()));
                seatPriceRepository.save(new SeatPrice(null, RoomType.HALL_2D, SeatType.COUPLE, BigDecimal.valueOf(180000), LocalDate.now()));
                seatPriceRepository.save(new SeatPrice(null, RoomType.IMAX, SeatType.STANDARD, BigDecimal.valueOf(150000), LocalDate.now()));
                seatPriceRepository.save(new SeatPrice(null, RoomType.IMAX, SeatType.VIP, BigDecimal.valueOf(210000), LocalDate.now()));
                System.out.println("✅ Đã khởi tạo bảng giá vé mặc định");
            }

            // 4. Khởi tạo Khuyến mãi mẫu
            if (promotionRepository.count() == 0) {
                MembershipBenefit standard = membershipBenefitRepository.findByTier(MembershipTier.STANDARD).orElse(null);
                if (standard != null) {
                    promotionRepository.save(new Promotion.Builder("WELCOME2024", "Chào mừng thành viên mới", DiscountType.FIXED, BigDecimal.valueOf(20000))
                            .validity(LocalDate.now(), LocalDate.now().plusMonths(6))
                            .minOrder(BigDecimal.valueOf(100000)).limit(1000).minTier(standard).build());
                }
            }

            // 5. Khởi tạo tài khoản ADMIN
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@starcinema.vn");
                admin.setRole(Role.ADMIN);
                admin.setStatus(true);
                userRepository.save(admin);
                System.out.println("✅ Tài khoản ADMIN: admin / admin123");
            }

            // 6. Khởi tạo tài khoản MANAGER
            if (userRepository.findByUsername("manager01").isEmpty()) {
                User manager = new User();
                manager.setUsername("manager01");
                manager.setPassword(passwordEncoder.encode("123123"));
                manager.setEmail("manager01@starcinema.vn");
                manager.setRole(Role.MANAGER);
                manager.setStatus(true);
                userRepository.save(manager);
                System.out.println("✅ Tài khoản MANAGER: manager01 / 123123");
            }

            // 7. Khởi tạo tài khoản STAFF (Nhân viên bán vé)
            if (userRepository.findByUsername("staff01").isEmpty()) {
                User staff = new User();
                staff.setUsername("staff01");
                staff.setPassword(passwordEncoder.encode("123123"));
                staff.setEmail("staff01@starcinema.vn");
                staff.setRole(Role.STAFF);
                staff.setStatus(true);
                userRepository.save(staff);
                System.out.println("✅ Tài khoản STAFF: staff01 / 123123");
            }

            // 8. Khởi tạo CUSTOMER test
            if (userRepository.findByUsername("customer_test").isEmpty()) {
                User user = new User();
                user.setUsername("customer_test");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setEmail("test@gmail.com");
                user.setRole(Role.CUSTOMER);
                user.setStatus(true);
                User savedUser = userRepository.save(user);

                Customer customer = new Customer();
                customer.setUser(savedUser);
                customer.setFullName("Khách Hàng Thân Thiết");
                customer.setPhone("0988888888");
                customer.setMembershipTier(MembershipTier.GOLD);
                customer.setPoints(5000);
                customer.setTotalSpending(BigDecimal.valueOf(2500000));
                customerRepository.save(customer);
                System.out.println("✅ Tài khoản CUSTOMER: customer_test / 123456");
            }
        };
    }
}
