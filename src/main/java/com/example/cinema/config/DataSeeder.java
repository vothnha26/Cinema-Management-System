package com.example.cinema.config;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.MembershipTier;
import com.example.cinema.model.enums.DiscountType;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.repository.movie.GenreRepository;
import com.example.cinema.repository.movie.FormatRepository;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      CustomerRepository customerRepository,
                                      MembershipBenefitRepository membershipBenefitRepository,
                                      PromotionRepository promotionRepository,
                                      GenreRepository genreRepository,
                                      SeatPriceRepository seatPriceRepository,
                                      RoomTypeRepository roomTypeRepository,
                                      SeatTypeRepository seatTypeRepository,
                                      FormatRepository formatRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 0. Khởi tạo Formats trước
            Format f2d = formatRepository.findById(1L).orElse(null);
            if (f2d == null) {
                f2d = formatRepository.save(new Format(1L, "2D", "Standard 2D"));
                formatRepository.save(new Format(2L, "3D", "Advanced 3D"));
                formatRepository.save(new Format(3L, "IMAX", "IMAX Premium"));
                formatRepository.save(new Format(4L, "4DX", "4DX Motion"));
            }
            Format f3d = formatRepository.findById(2L).orElse(null);
            Format fImax = formatRepository.findById(3L).orElse(null);
            Format f4dx = formatRepository.findById(4L).orElse(null);

            // 0.1 Khởi tạo Master Data (RoomType & SeatType)
            if (roomTypeRepository.count() == 0) {
                RoomType rt2d = new RoomType("HALL_2D", "Standard 2D Hall");
                rt2d.getSupportedFormats().add(f2d);
                roomTypeRepository.save(rt2d);

                RoomType rtImax = new RoomType("IMAX", "IMAX Premium Experience");
                rtImax.getSupportedFormats().addAll(Arrays.asList(f2d, f3d, fImax));
                roomTypeRepository.save(rtImax);

                RoomType rt3d = new RoomType("HALL_3D", "Advanced 3D Cinema");
                rt3d.getSupportedFormats().addAll(Arrays.asList(f2d, f3d));
                roomTypeRepository.save(rt3d);

                RoomType rt4dx = new RoomType("HALL_4DX", "4DX Motion Dynamic");
                rt4dx.getSupportedFormats().addAll(Arrays.asList(f2d, f3d, f4dx));
                roomTypeRepository.save(rt4dx);

                RoomType rtGold = new RoomType("GOLD_CLASS", "Elite Gold Class Cinema");
                rtGold.getSupportedFormats().addAll(Arrays.asList(f2d, f3d));
                roomTypeRepository.save(rtGold);
            }

            if (seatTypeRepository.count() == 0) {
                seatTypeRepository.save(new SeatType("STANDARD", "Standard Ergonomic"));
                seatTypeRepository.save(new SeatType("VIP", "VIP Premium Leather"));
                seatTypeRepository.save(new SeatType("COUPLE", "Elite Sweetbox Couple"));
                seatTypeRepository.save(new SeatType("DELUXE", "Deluxe Recliner"));
                seatTypeRepository.save(new SeatType("EMPTY", "Empty Space/Aisle"));
            }

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
                RoomType room2d = roomTypeRepository.findById("HALL_2D").orElse(null);
                RoomType roomImax = roomTypeRepository.findById("IMAX").orElse(null);
                SeatType sStandard = seatTypeRepository.findById("STANDARD").orElse(null);
                SeatType sVip = seatTypeRepository.findById("VIP").orElse(null);
                SeatType sCouple = seatTypeRepository.findById("COUPLE").orElse(null);

                if (room2d != null && sStandard != null) {
                    seatPriceRepository.save(new SeatPrice(null, room2d, sStandard, BigDecimal.valueOf(85000), LocalDate.now()));
                    seatPriceRepository.save(new SeatPrice(null, room2d, sVip, BigDecimal.valueOf(110000), LocalDate.now()));
                    seatPriceRepository.save(new SeatPrice(null, room2d, sCouple, BigDecimal.valueOf(180000), LocalDate.now()));
                    seatPriceRepository.save(new SeatPrice(null, roomImax, sStandard, BigDecimal.valueOf(150000), LocalDate.now()));
                    seatPriceRepository.save(new SeatPrice(null, roomImax, sVip, BigDecimal.valueOf(210000), LocalDate.now()));
                    System.out.println("✅ Đã khởi tạo bảng giá vé mặc định");
                }
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
