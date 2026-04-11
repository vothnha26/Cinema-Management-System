package com.example.cinema.config;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.movie.FormatRepository;
import com.example.cinema.repository.movie.GenreRepository;
import com.example.cinema.repository.movie.MovieRepository;
import com.example.cinema.repository.room.RoomRepository;
import com.example.cinema.repository.room.RoomTypeRepository;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.repository.user.StaffRepository;
import com.example.cinema.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final BranchRepository branchRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final SeatPriceRepository seatPriceRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final FormatRepository formatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MembershipLevelRepository membershipLevelRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, StaffRepository staffRepository,
            BranchRepository branchRepository, RoomTypeRepository roomTypeRepository,
            SeatTypeRepository seatTypeRepository, SeatPriceRepository seatPriceRepository,
            RoomRepository roomRepository, SeatRepository seatRepository,
            MovieRepository movieRepository, GenreRepository genreRepository,
            FormatRepository formatRepository, ShowtimeRepository showtimeRepository,
            MembershipLevelRepository membershipLevelRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.branchRepository = branchRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.seatTypeRepository = seatTypeRepository;
        this.seatPriceRepository = seatPriceRepository;
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.formatRepository = formatRepository;
        this.showtimeRepository = showtimeRepository;
        this.membershipLevelRepository = membershipLevelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            seedRolesAndUsers();
            seedMasterData();
            seedSeatPrices();
            seedMovies();
        } catch (Exception e) {
            log.error("Error during data seeding: {}", e.getMessage());
        }
    }

    private void seedRolesAndUsers() {
        if (userRepository.count() == 0) {
            // Admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@flashcinema.com");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // Branch & Manager
            Branch branch = new Branch();
            branch.setName("FlashCinema Thủ Đức");
            branch.setAddress("Võ Văn Ngân, Thủ Đức");
            branch.setCity("Hồ Chí Minh");
            branch.setPhone("0123456789");
            branch = branchRepository.save(branch);

            User managerUser = new User();
            managerUser.setUsername("manager1");
            managerUser.setPassword(passwordEncoder.encode("manager123"));
            managerUser.setEmail("manager1@flashcinema.com");
            managerUser.setRole(Role.MANAGER);
            userRepository.save(managerUser);

            Staff manager = new Staff();
            manager.setUser(managerUser);
            manager.setBranch(branch);
            manager.setFullName("Quản lý 01");
            manager.setStaffCode("MGR001");
            manager.setPosition("Quản lý rạp");
            staffRepository.save(manager);

            // Nhân viên quầy (STAFF)
            User staffUser = new User();
            staffUser.setUsername("staff1");
            staffUser.setPassword(passwordEncoder.encode("staff123"));
            staffUser.setEmail("staff1@flashcinema.com");
            staffUser.setRole(Role.STAFF);
            userRepository.save(staffUser);

            Staff staff = new Staff();
            staff.setUser(staffUser);
            staff.setBranch(branch);
            staff.setFullName("Nhân viên quầy 01");
            staff.setStaffCode("STF001");
            staff.setPosition("Nhân viên bán vé");
            staffRepository.save(staff);

            log.info("Seed users, branches and staff completed");
        }
    }

    private void seedMasterData() {
        if (roomTypeRepository.count() == 0) {
            roomTypeRepository.save(new RoomType("STANDARD", "Tiêu chuẩn"));
            roomTypeRepository.save(new RoomType("GOLD_CLASS", "Hạng thương gia"));
            roomTypeRepository.save(new RoomType("IMAX", "Phòng chiếu IMAX"));
        }

        if (seatTypeRepository.count() == 0) {
            seatTypeRepository.save(new SeatType("NORMAL", "Ghế thường"));
            seatTypeRepository.save(new SeatType("VIP", "Ghế VIP"));
            seatTypeRepository.save(new SeatType("COUPLE", "Ghế đôi"));
        }

        if (formatRepository.count() == 0) {
            formatRepository.save(new Format(null, "2D", "Phim 2D tiêu chuẩn"));
            formatRepository.save(new Format(null, "3D", "Phim 3D sống động"));
            formatRepository.save(new Format(null, "IMAX", "Công nghệ IMAX đỉnh cao"));
        }

        if (membershipLevelRepository.count() == 0) {
            MembershipLevel standard = new MembershipLevel();
            standard.setName("STANDARD");
            standard.setMinSpending(BigDecimal.ZERO);
            standard.setPriority(1);
            membershipLevelRepository.save(standard);

            MembershipLevel gold = new MembershipLevel();
            gold.setName("GOLD");
            gold.setMinSpending(new BigDecimal("5000000"));
            gold.setPriority(3);
            membershipLevelRepository.save(gold);
        }
    }

    private void seedSeatPrices() {
        if (seatPriceRepository.count() == 0) {
            RoomType standardRoom = roomTypeRepository.findById("STANDARD").orElse(null);
            RoomType goldRoom = roomTypeRepository.findById("GOLD_CLASS").orElse(null);

            SeatType normalSeat = seatTypeRepository.findById("NORMAL").orElse(null);
            SeatType vipSeat = seatTypeRepository.findById("VIP").orElse(null);
            SeatType coupleSeat = seatTypeRepository.findById("COUPLE").orElse(null);

            if (standardRoom != null && normalSeat != null) {
                seatPriceRepository.save(new SeatPrice(standardRoom, normalSeat, new BigDecimal("80000")));
                seatPriceRepository.save(new SeatPrice(standardRoom, vipSeat, new BigDecimal("110000")));
                seatPriceRepository.save(new SeatPrice(standardRoom, coupleSeat, new BigDecimal("200000")));
            }

            if (goldRoom != null && normalSeat != null) {
                seatPriceRepository.save(new SeatPrice(goldRoom, normalSeat, new BigDecimal("150000")));
                seatPriceRepository.save(new SeatPrice(goldRoom, vipSeat, new BigDecimal("200000")));
            }
            log.info("Seed seat prices completed");
        }
    }

    private void seedMovies() {
        if (movieRepository.count() == 0) {
            Movie movie = new Movie();
            movie.setTitle("Gặp Lại Chị Bầu");
            movie.setDuration(110);
            movie.setStatus(MovieStatus.SHOWING);
            movie.setAgeRating(AgeRating.T13);
            movie.setReleaseDate(LocalDate.now());
            movieRepository.save(movie);
            log.info("Seed movies completed");
        }
    }
}
