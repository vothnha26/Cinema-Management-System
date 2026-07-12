package com.example.cinema.repository.user;

import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = true")
    List<User> findAllByRoleAndStatusTrue(@Param("role") Role role);

    @Query("SELECT u FROM User u JOIN Staff s ON u.id = s.user.id " +
           "WHERE u.role = :role AND u.status = true " +
           "AND (:branchId IS NULL OR s.branch.id = :branchId)")
    List<User> findAllByRoleAndBranchAndStatusTrue(@Param("role") Role role, @Param("branchId") Long branchId);
}
