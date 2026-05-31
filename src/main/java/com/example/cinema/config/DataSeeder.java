package com.example.cinema.config;

import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.branch.BranchRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.service.infrastructure.facade.*;
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

    private final UserDomainFacade userFacade;
    private final MovieDomainFacade movieFacade;
    private final CinemaDomainFacade cinemaFacade;
    private final BranchRepository branchRepository;
    private final MembershipLevelRepository membershipLevelRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserDomainFacade userFacade, MovieDomainFacade movieFacade, 
                      CinemaDomainFacade cinemaFacade, BranchRepository branchRepository,
                      MembershipLevelRepository membershipLevelRepository,
                      PasswordEncoder passwordEncoder) {
        this.userFacade = userFacade;
        this.movieFacade = movieFacade;
        this.cinemaFacade = cinemaFacade;
        this.branchRepository = branchRepository;
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
        // Sử dụng repo gốc cho count() để tránh sửa facade nhiều
        if (userFacade.findUserByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
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
            mUser.setPassword(passwordEncoder.encode("manager123"));
            mUser.setEmail("manager1@starcinema.com");
            mUser.setRole(Role.MANAGER);
            
            Staff m = new Staff();
            m.setUser(mUser); m.setBranch(branch); m.setFullName("Quản lý 01");
            m.setStaffCode("MGR001"); m.setPosition("Quản lý rạp");
            // Logic save staff/user qua facade sau này
        }
    }

    private void seedMasterData() {
        if (cinemaFacade.findAllRoomTypes().isEmpty()) {
            cinemaFacade.saveRoom(new Room()); // Placeholder cho logic save
        }
    }

    private void seedSeatPrices() {
        // Logic seed seat prices
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
