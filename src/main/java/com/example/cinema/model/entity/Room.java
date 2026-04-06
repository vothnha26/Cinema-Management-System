package com.example.cinema.model.entity;

import com.example.cinema.model.enums.RoomStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "num_rows")
    private Integer rows;

    @Column(name = "num_cols")
    private Integer cols;

    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.ACTIVE;

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public void setType(String typeId) {
        if (typeId != null) {
            this.roomType = new RoomType(typeId, null);
        }
    }

    public String getType() {
        return roomType != null ? roomType.getId() : null;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getCols() {
        return cols;
    }

    public void setCols(Integer cols) {
        this.cols = cols;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public java.util.Set<Format> getSupportedFormats() {
        return roomType != null ? roomType.getSupportedFormats() : new java.util.HashSet<>();
    }
}
