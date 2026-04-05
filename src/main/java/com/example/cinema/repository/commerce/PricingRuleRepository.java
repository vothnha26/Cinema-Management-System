package com.example.cinema.repository.commerce;

import com.example.cinema.model.entity.PricingRule;
import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    
    @Query("SELECT r FROM PricingRule r WHERE r.isActive = true " +
           "AND (r.applicableRoomType IS NULL OR r.applicableRoomType = :roomType) " +
           "AND (r.applicableSeatType IS NULL OR r.applicableSeatType = :seatType) " +
           "AND (r.applicableFormat IS NULL OR r.applicableFormat = :format) " +
           "ORDER BY r.priority ASC")
    List<PricingRule> findActiveRulesByContext(
        @Param("roomType") RoomType roomType,
        @Param("seatType") SeatType seatType,
        @Param("format") String format
    );
}
