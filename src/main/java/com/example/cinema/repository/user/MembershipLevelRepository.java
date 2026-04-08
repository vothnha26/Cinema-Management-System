package com.example.cinema.repository.user;

import com.example.cinema.model.entity.MembershipLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MembershipLevelRepository extends JpaRepository<MembershipLevel, Long> {
    Optional<MembershipLevel> findByName(String name);
}
