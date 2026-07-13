package com.example.cinema.repository.user;

import com.example.cinema.model.entity.User;
import com.example.cinema.model.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    
    Optional<VerificationCode> findTopByUserAndCodeAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            User user, String code, String purpose, LocalDateTime now);

    Optional<VerificationCode> findTopByCodeAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            String code, String purpose, LocalDateTime now);
}
