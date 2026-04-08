package com.example.cinema.config;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.commerce.*;
import com.example.cinema.repository.room.*;
import com.example.cinema.repository.user.*;
import com.example.cinema.repository.movie.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@org.springframework.context.annotation.Configuration
@org.springframework.core.annotation.Order(1)
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
            CustomerRepository customerRepository,
            MembershipLevelRepository membershipLevelRepository,
            MembershipBenefitRepository membershipBenefitRepository,
            PromotionRepository promotionRepository,
            GenreRepository genreRepository,
            SeatPriceRepository seatPriceRepository,
            RoomTypeRepository roomTypeRepository,
            SeatTypeRepository seatTypeRepository,
            FormatRepository formatRepository,
            BranchRepository branchRepository,
            StaffRepository staffRepository,
            RoomRepository roomRepository,
            SeatRepository seatRepository,
            ComboRepository comboRepository,
            BranchComboRepository branchComboRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 0. Khởi tạo Branch mặc định
            if (branchRepository.count() == 0) {
                branchRepository.save(new Branch(null, "StarCinema Thủ Đức", "Số 1 Võ Văn Ngân", "TP. Hồ Chí Minh",
                        "0281234567", true));
                branchRepository.save(new Branch(null, "StarCinema Quận 1", "Lê Lợi, Bến Nghé", "TP. Hồ Chí Minh",
                        "0287654321", true));
                System.out.println("✅ Đã khởi tạo 2 Chi nhánh");
            }

            List<Branch> allBranches = branchRepository.findAll();
            Branch branchTD = allBranches.get(0);
            Branch branchQ1 = allBranches.get(1);

            // 0.1 Khởi tạo Định dạng phim (Formats)
            if (formatRepository.count() == 0) {
                formatRepository.save(new Format(1L, "2D", "Standard 2D Digital Cinema"));
                formatRepository.save(new Format(2L, "3D", "Advanced 3D Experience"));
                formatRepository.save(new Format(3L, "IMAX", "IMAX Premium Cinematic Technology"));
                formatRepository.save(new Format(4L, "4DX", "4DX Motion & Environmental Effects"));
                System.out.println("✅ Đã khởi tạo 4 Định dạng phim");
            }

            Format f2d = formatRepository.findById(1L).orElse(null);
            Format f3d = formatRepository.findById(2L).orElse(null);
            Format fImax = formatRepository.findById(3L).orElse(null);
            Format f4dx = formatRepository.findById(4L).orElse(null);

            // 0.2 Khởi tạo Master Data (RoomType & SeatType)
            if (roomTypeRepository.count() == 0) {
                RoomType rt2d = new RoomType("HALL_2D", "Standard 2D Hall");
                rt2d.getSupportedFormats().add(f2d);
                roomTypeRepository.save(rt2d);

                RoomType rt3d = new RoomType("HALL_3D", "Advanced 3D Cinema");
                rt3d.getSupportedFormats().addAll(Arrays.asList(f2d, f3d));
                roomTypeRepository.save(rt3d);

                RoomType rtImax = new RoomType("IMAX", "IMAX Premium Experience");
                rtImax.getSupportedFormats().addAll(Arrays.asList(f2d, f3d, fImax));
                roomTypeRepository.save(rtImax);

                RoomType rt4dx = new RoomType("HALL_4DX", "4DX Motion Dynamic");
                rt4dx.getSupportedFormats().addAll(Arrays.asList(f2d, f3d, f4dx));
                roomTypeRepository.save(rt4dx);

                RoomType rtGold = new RoomType("GOLD_CLASS", "Elite Gold Class Cinema");
                rtGold.getSupportedFormats().addAll(Arrays.asList(f2d, f3d));
                roomTypeRepository.save(rtGold);
                System.out.println("✅ Đã khởi tạo 5 Loại phòng");
            }

            if (seatTypeRepository.count() == 0) {
                seatTypeRepository.save(new SeatType("STANDARD", "Standard Ergonomic"));
                seatTypeRepository.save(new SeatType("VIP", "VIP Premium Leather"));
                seatTypeRepository.save(new SeatType("COUPLE", "Elite Sweetbox Couple"));
                seatTypeRepository.save(new SeatType("DELUXE", "Deluxe Recliner"));
                System.out.println("✅ Đã khởi tạo 4 Loại ghế");
            }

            // 1. Khởi tạo Thể loại (Genres)
            if (genreRepository.count() == 0) {
                Arrays.asList("Hành động", "Hài hước", "Kinh dị", "Tình cảm", "Viễn tưởng", "Hoạt hình")
                        .forEach(name -> genreRepository.save(new Genre(null, name)));
                System.out.println("✅ Đã khởi tạo danh sách Thể loại phim");
            }

            // 2. Khởi tạo Membership Levels & Benefits
            if (membershipLevelRepository.count() == 0) {
                MembershipLevel guest = membershipLevelRepository
                        .save(new MembershipLevel("GUEST", BigDecimal.ZERO, 0));
                MembershipLevel standard = membershipLevelRepository
                        .save(new MembershipLevel("STANDARD", BigDecimal.valueOf(1000000), 1));
                MembershipLevel silver = membershipLevelRepository
                        .save(new MembershipLevel("SILVER", BigDecimal.valueOf(5000000), 2));
                MembershipLevel gold = membershipLevelRepository
                        .save(new MembershipLevel("GOLD", BigDecimal.valueOf(15000000), 3));
                MembershipLevel platinum = membershipLevelRepository
                        .save(new MembershipLevel("PLATINUM", BigDecimal.valueOf(50000000), 4));

                // Benefits for GUEST
                membershipBenefitRepository.save(new MembershipBenefit(guest, "POINT_MULTIPLIER", "0.0"));

                // Benefits for STANDARD
                membershipBenefitRepository.save(new MembershipBenefit(standard, "POINT_MULTIPLIER", "1.0"));
                membershipBenefitRepository.save(new MembershipBenefit(standard, "DISCOUNT", "0.0"));

                // Benefits for SILVER
                membershipBenefitRepository.save(new MembershipBenefit(silver, "POINT_MULTIPLIER", "1.2"));
                membershipBenefitRepository.save(new MembershipBenefit(silver, "DISCOUNT", "5.0"));

                // Benefits for GOLD
                membershipBenefitRepository.save(new MembershipBenefit(gold, "POINT_MULTIPLIER", "1.5"));
                membershipBenefitRepository.save(new MembershipBenefit(gold, "DISCOUNT", "10.0"));

                // Benefits for PLATINUM
                membershipBenefitRepository.save(new MembershipBenefit(platinum, "POINT_MULTIPLIER", "2.0"));
                membershipBenefitRepository.save(new MembershipBenefit(platinum, "DISCOUNT", "15.0"));

                System.out.println("✅ Đã khởi tạo Membership Levels & Benefits (Rule-based)");
            }

            // 3. Khởi tạo PHÒNG CHIẾU và GHẾ cho TẤT CẢ CHI NHÁNH
            if (roomRepository.count() == 0) {
                RoomType rt2d = roomTypeRepository.findById("HALL_2D").orElse(null);
                RoomType rt3d = roomTypeRepository.findById("HALL_3D").orElse(null);
                RoomType rtImax = roomTypeRepository.findById("IMAX").orElse(null);
                RoomType rt4dx = roomTypeRepository.findById("HALL_4DX").orElse(null);
                RoomType rtGold = roomTypeRepository.findById("GOLD_CLASS").orElse(null);

                SeatType stStandard = seatTypeRepository.findById("STANDARD").orElse(null);
                SeatType stVip = seatTypeRepository.findById("VIP").orElse(null);
                SeatType stCouple = seatTypeRepository.findById("COUPLE").orElse(null);
                SeatType stDeluxe = seatTypeRepository.findById("DELUXE").orElse(null);

                for (Branch branch : allBranches) {
                    List<Room> rooms = new ArrayList<>();

                    Room r1 = new Room();
                    r1.setName("Phòng 1 (2D)");
                    r1.setBranch(branch);
                    r1.setRoomType(rt2d);
                    r1.setCapacity(100);
                    r1.setRows(10);
                    r1.setCols(10);
                    r1.setStatus(RoomStatus.ACTIVE);
                    rooms.add(r1);

                    Room r2 = new Room();
                    r2.setName("Phòng 2 (3D)");
                    r2.setBranch(branch);
                    r2.setRoomType(rt3d);
                    r2.setCapacity(80);
                    r2.setRows(8);
                    r2.setCols(10);
                    r2.setStatus(RoomStatus.ACTIVE);
                    rooms.add(r2);

                    if (branch.getName().contains("Thủ Đức")) {
                        Room r3 = new Room();
                        r3.setName("Phòng 3 (IMAX)");
                        r3.setBranch(branch);
                        r3.setRoomType(rtImax);
                        r3.setCapacity(150);
                        r3.setRows(10);
                        r3.setCols(15);
                        r3.setStatus(RoomStatus.ACTIVE);
                        rooms.add(r3);
                    } else {
                        Room r4 = new Room();
                        r4.setName("Phòng 4 (4DX)");
                        r4.setBranch(branch);
                        r4.setRoomType(rt4dx);
                        r4.setCapacity(60);
                        r4.setRows(6);
                        r4.setCols(10);
                        r4.setStatus(RoomStatus.ACTIVE);
                        rooms.add(r4);

                        Room r5 = new Room();
                        r5.setName("Phòng 5 (GOLD)");
                        r5.setBranch(branch);
                        r5.setRoomType(rtGold);
                        r5.setCapacity(30);
                        r5.setRows(5);
                        r5.setCols(6);
                        r5.setStatus(RoomStatus.ACTIVE);
                        rooms.add(r5);
                    }

                    List<Room> savedRooms = roomRepository.saveAll(rooms);
                    for (Room room : savedRooms) {
                        List<Seat> seats = new ArrayList<>();
                        for (int r = 0; r < room.getRows(); r++) {
                            char rowName = (char) ('A' + r);
                            for (int c = 1; c <= room.getCols(); c++) {
                                Seat seat = new Seat();
                                seat.setRoom(room);
                                seat.setRowChar(String.valueOf(rowName));
                                seat.setColNum(c);

                                if (room.getRoomType().getId().equals("GOLD_CLASS")) {
                                    seat.setSeatType(stDeluxe);
                                } else {
                                    if (r == room.getRows() - 1 && room.getRoomType().getId().equals("HALL_2D"))
                                        seat.setSeatType(stCouple);
                                    else if (r >= 3 && r <= 7)
                                        seat.setSeatType(stVip);
                                    else
                                        seat.setSeatType(stStandard);
                                }
                                seat.setStatus(true);
                                seats.add(seat);
                            }
                        }
                        seatRepository.saveAll(seats);
                    }
                }
                System.out.println("✅ Đã khởi tạo đa dạng phòng chiếu và ghế cho các rạp");
            }

            // 4. Khởi tạo Bảng giá nâng cao (Seat Prices)
            if (seatPriceRepository.count() == 0) {
                List<RoomType> rTypes = roomTypeRepository.findAll();
                List<SeatType> sTypes = seatTypeRepository.findAll();

                for (Branch branch : allBranches) {
                    for (RoomType rt : rTypes) {
                        for (SeatType st : sTypes) {
                            BigDecimal basePrice = BigDecimal.valueOf(80000);
                            if (rt.getId().equals("IMAX"))
                                basePrice = basePrice.add(BigDecimal.valueOf(70000));
                            else if (rt.getId().equals("HALL_4DX"))
                                basePrice = basePrice.add(BigDecimal.valueOf(90000));
                            else if (rt.getId().equals("GOLD_CLASS"))
                                basePrice = basePrice.add(BigDecimal.valueOf(150000));
                            else if (rt.getId().equals("HALL_3D"))
                                basePrice = basePrice.add(BigDecimal.valueOf(30000));

                            if (st.getId().equals("VIP"))
                                basePrice = basePrice.add(BigDecimal.valueOf(20000));
                            else if (st.getId().equals("COUPLE"))
                                basePrice = basePrice.add(BigDecimal.valueOf(100000));
                            else if (st.getId().equals("DELUXE"))
                                basePrice = basePrice.add(BigDecimal.valueOf(50000));

                            if (branch.getName().contains("Quận 1"))
                                basePrice = basePrice.add(BigDecimal.valueOf(10000));

                            seatPriceRepository.save(new SeatPrice(null, branch, rt, st, basePrice, LocalDate.now()));
                        }
                    }
                }
                System.out.println("✅ Đã khởi tạo bảng giá nâng cao cho tất cả chi nhánh");
            }

            // 5. Khởi tạo COMBO và PHÂN PHỐI
            if (comboRepository.count() == 0) {
                Combo c1 = new Combo(null, "Combo Single", "1 Bắp + 1 Nước", BigDecimal.valueOf(85000),
                        "https://res.cloudinary.com/dynd7id7f/image/upload/v1715421255/combo1.png", 100, true);
                Combo c2 = new Combo(null, "Combo Double", "1 Bắp + 2 Nước", BigDecimal.valueOf(115000),
                        "https://res.cloudinary.com/dynd7id7f/image/upload/v1715421255/combo2.png", 100, true);
                Combo c3 = new Combo(null, "Combo Family", "2 Bắp + 4 Nước", BigDecimal.valueOf(220000),
                        "https://res.cloudinary.com/dynd7id7f/image/upload/v1715421255/combo3.png", 100, true);
                List<Combo> savedCombos = comboRepository.saveAll(Arrays.asList(c1, c2, c3));

                for (Branch branch : allBranches) {
                    for (Combo combo : savedCombos) {
                        BranchCombo bc = new BranchCombo();
                        bc.setBranch(branch);
                        bc.setCombo(combo);
                        bc.setPrice(combo.getPrice());
                        bc.setStockQuantity(500);
                        bc.setIsActive(true);
                        branchComboRepository.save(bc);
                    }
                }
                System.out.println("✅ Đã khởi tạo danh sách Combo bắp nước");
            }

            // 6. Khởi tạo KHUYẾN MÃI
            if (promotionRepository.count() == 0) {
                MembershipLevel standard = membershipLevelRepository.findByName("STANDARD").orElse(null);

                promotionRepository.save(new Promotion.Builder("HELLO_STAR", "Giảm 20k cho thành viên mới",
                        DiscountType.FIXED, BigDecimal.valueOf(20000))
                        .validity(LocalDate.now(), LocalDate.now().plusMonths(6))
                        .minOrder(BigDecimal.valueOf(100000)).limit(1000).minLevel(standard).build());

                promotionRepository.save(new Promotion.Builder("HAPPY_WEEKEND", "Ưu đãi cuối tuần giảm 10%",
                        DiscountType.PERCENT, BigDecimal.valueOf(10))
                        .validity(LocalDate.now(), LocalDate.now().plusMonths(3))
                        .minOrder(BigDecimal.valueOf(200000)).limit(500).minLevel(standard).build());

                System.out.println("✅ Đã khởi tạo danh sách Khuyến mãi");
            }

            // 7. Khởi tạo tài khoản
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

            if (userRepository.findByUsername("manager01").isEmpty()) {
                User user = new User();
                user.setUsername("manager01");
                user.setPassword(passwordEncoder.encode("123123"));
                user.setEmail("manager01@starcinema.vn");
                user.setRole(Role.MANAGER);
                user.setStatus(true);
                User saved = userRepository.save(user);

                Staff staff = new Staff();
                staff.setUser(saved);
                staff.setBranch(branchTD);
                staff.setStaffCode("MGR01");
                staffRepository.save(staff);
                System.out.println("✅ Tài khoản MANAGER 01: manager01 / 123123");
            }
        };
    }
}