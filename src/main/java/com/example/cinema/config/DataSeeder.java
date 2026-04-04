package com.example.cinema.config;

import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.MembershipTier;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tự động tạo dữ liệu mẫu (Seed Data) khi ứng dụng khởi chạy
 * nếu database đang trống.
 */
@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Tạo Admin
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@starcinema.vn");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setStatus(true);
                userRepository.save(admin);
                System.out.println("✅ Đã tạo tài khoản ADMIN: admin / admin123");
            }

            // 2. Tạo Staff (Nhân viên bán vé)
            if (!userRepository.existsByUsername("staff01")) {
                User staff = new User();
                staff.setUsername("staff01");
                staff.setEmail("staff01@starcinema.vn");
                staff.setPassword(passwordEncoder.encode("123123"));
                staff.setRole(Role.STAFF);
                staff.setStatus(true);
                userRepository.save(staff);
                System.out.println("✅ Đã tạo tài khoản STAFF: staff01 / 123123");
            }

            // 3. Tạo Manager (Quản lý)
            if (!userRepository.existsByUsername("manager01")) {
                User manager = new User();
                manager.setUsername("manager01");
                manager.setEmail("manager01@starcinema.vn");
                manager.setPassword(passwordEncoder.encode("123123"));
                manager.setRole(Role.MANAGER);
                manager.setStatus(true);
                userRepository.save(manager);
                System.out.println("✅ Đã tạo tài khoản MANAGER: manager01 / 123123");
            }

            // 4. Tạo Customer (Khách hàng thành viên)
            if (!userRepository.existsByUsername("khachhang")) {
                User user = new User();
                user.setUsername("khachhang");
                user.setEmail("khachhang@gmail.com");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRole(Role.CUSTOMER);
                user.setStatus(true);
                userRepository.save(user);

                Customer customer = new Customer();
                customer.setUser(user);
                customer.setFullName("Nguyễn Văn Khách");
                customer.setPhone("0901234567");
                customer.setMembershipTier(MembershipTier.GOLD);
                customer.setPoints(1500);
                customerRepository.save(customer);
                System.out.println("✅ Đã tạo tài khoản CUSTOMER: khachhang / 123456");
            }
        };
    }
}
