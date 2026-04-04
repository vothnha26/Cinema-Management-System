package com.example.cinema.repository.user;

import com.example.cinema.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);
    Optional<Customer> findByUserUsername(String username);
    Optional<Customer> findByPhone(String phone);
}
