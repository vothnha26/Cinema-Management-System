package com.example.cinema.repository.room;

import com.example.cinema.model.entity.Room;
import com.example.cinema.model.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByBranchId(Long branchId);
    List<Room> findByBranchIdAndStatus(Long branchId, RoomStatus status);
}
