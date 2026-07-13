package com.example.cinema.config;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.entity.RoomType;
import com.example.cinema.model.entity.SeatType;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.room.SeatTypeRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.service.infrastructure.facade.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Order(2)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserDomainFacade userFacade;
    private final MovieDomainFacade movieFacade;
    private final CinemaDomainFacade cinemaFacade;
    private final BranchRepository branchRepository;
    private final MembershipLevelRepository membershipLevelRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserDomainFacade userFacade, MovieDomainFacade movieFacade, 
                      CinemaDomainFacade cinemaFacade, BranchRepository branchRepository,
                      MembershipLevelRepository membershipLevelRepository,
                      SeatTypeRepository seatTypeRepository,
                      PasswordEncoder passwordEncoder) {
        this.userFacade = userFacade;
        this.movieFacade = movieFacade;
        this.cinemaFacade = cinemaFacade;
        this.branchRepository = branchRepository;
        this.membershipLevelRepository = membershipLevelRepository;
        this.seatTypeRepository = seatTypeRepository;
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
        // Sử dụng repo gốc cho count() để tránh sửa facade nhiều
        if (userFacade.findUserByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(System.getProperty("SEED_ADMIN_PASSWORD", "admin_default_pass")));
            admin.setEmail("admin@flashcinema.com");
            admin.setRole(Role.ADMIN);
            // Lưu qua repo/facade
            
            Branch branch = new Branch();
            branch.setName("StarCinema Thủ Đức");
            branch.setAddress("Võ Văn Ngân, Thủ Đức");
            branch.setCity("Hồ Chí Minh");
            branch.setPhone("0123456789");
            branch = branchRepository.save(branch);

            User mUser = new User();
            mUser.setUsername("manager1");
            mUser.setPassword(passwordEncoder.encode(System.getProperty("SEED_MANAGER_PASSWORD", "manager_default_pass")));
            mUser.setEmail("manager1@starcinema.com");
            mUser.setRole(Role.MANAGER);
            
            Staff m = new Staff();
            m.setUser(mUser); m.setBranch(branch); m.setFullName("Quản lý 01");
            m.setStaffCode("MGR001"); m.setPosition("Quản lý rạp");
            // Logic save staff/user qua facade sau này
        }
    }

    private void seedMasterData() {
        // Seed Room Types if not present
        if (cinemaFacade.findRoomType("STANDARD").isEmpty()) {
            RoomType rt = new RoomType("STANDARD", "Standard Room");
            cinemaFacade.saveRoomType(rt);
        }
        if (cinemaFacade.findRoomType("VIP").isEmpty()) {
            RoomType rt = new RoomType("VIP", "VIP Room");
            cinemaFacade.saveRoomType(rt);
        }
        if (cinemaFacade.findRoomType("IMAX").isEmpty()) {
            RoomType rt = new RoomType("IMAX", "IMAX Room");
            cinemaFacade.saveRoomType(rt);
        }

        // Seed Seat Types if not present
        if (cinemaFacade.findSeatType("NORMAL").isEmpty()) {
            SeatType st = new SeatType("NORMAL", "Normal Seat");
            cinemaFacade.saveSeatType(st);
        }
        if (cinemaFacade.findSeatType("VIP").isEmpty()) {
            SeatType st = new SeatType("VIP", "VIP Seat");
            cinemaFacade.saveSeatType(st);
        }
        if (cinemaFacade.findSeatType("SWEETBOX").isEmpty()) {
            SeatType st = new SeatType("SWEETBOX", "Sweetbox Double Seat");
            cinemaFacade.saveSeatType(st);
        }
    }

    private void seedSeatPrices() {
        RoomType standardType = cinemaFacade.findRoomType("STANDARD").orElse(null);
        RoomType vipType = cinemaFacade.findRoomType("VIP").orElse(null);
        RoomType imaxType = cinemaFacade.findRoomType("IMAX").orElse(null);

        SeatType normalSeat = cinemaFacade.findSeatType("NORMAL").orElse(null);
        SeatType vipSeat = cinemaFacade.findSeatType("VIP").orElse(null);
        SeatType sweetboxSeat = cinemaFacade.findSeatType("SWEETBOX").orElse(null);

        if (standardType != null && normalSeat != null) {
            seedSingleSeatPrice(standardType, normalSeat, new BigDecimal("80000"));
        }
        if (standardType != null && vipSeat != null) {
            seedSingleSeatPrice(standardType, vipSeat, new BigDecimal("100000"));
        }
        if (standardType != null && sweetboxSeat != null) {
            seedSingleSeatPrice(standardType, sweetboxSeat, new BigDecimal("180000"));
        }

        if (vipType != null && normalSeat != null) {
            seedSingleSeatPrice(vipType, normalSeat, new BigDecimal("120000"));
        }
        if (vipType != null && vipSeat != null) {
            seedSingleSeatPrice(vipType, vipSeat, new BigDecimal("150000"));
        }
        if (vipType != null && sweetboxSeat != null) {
            seedSingleSeatPrice(vipType, sweetboxSeat, new BigDecimal("250000"));
        }

        if (imaxType != null && normalSeat != null) {
            seedSingleSeatPrice(imaxType, normalSeat, new BigDecimal("150000"));
        }
        if (imaxType != null && vipSeat != null) {
            seedSingleSeatPrice(imaxType, vipSeat, new BigDecimal("180000"));
        }
        if (imaxType != null && sweetboxSeat != null) {
            seedSingleSeatPrice(imaxType, sweetboxSeat, new BigDecimal("300000"));
        }
    }

    private void seedSingleSeatPrice(RoomType rt, SeatType st, BigDecimal price) {
        if (cinemaFacade.findActiveSeatPrice(rt, st).isEmpty()) {
            SeatPrice sp = new SeatPrice();
            sp.setRoomType(rt);
            sp.setSeatType(st);
            sp.setPrice(price);
            sp.setIsActive(true);
            cinemaFacade.saveSeatPrice(sp);
        }
    }

    private void seedMovies() {
        if (movieFacade.findAllMovies().isEmpty()) {
            Movie movie = new Movie();
            movie.setTitle("Gặp Lại Chị Bầu");
            movie.setDuration(110);
            movie.setStatus(MovieStatus.SHOWING);
            movie.setAgeRating(AgeRating.T13);
            movie.setReleaseDate(LocalDate.now());
            movieFacade.saveMovie(movie);
        }
    }
}
