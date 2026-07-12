package com.example.cinema.repository.user;

import com.example.cinema.model.entity.Staff;
import com.example.cinema.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUser(User user);
    Optional<Staff> findByUserId(Long userId);
    Optional<Staff> findByStaffCode(String staffCode);
}
