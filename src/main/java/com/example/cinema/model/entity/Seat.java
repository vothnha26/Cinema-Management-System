package com.example.cinema.model.entity;

import com.example.cinema.model.enums.SeatType;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "row_char", nullable = false)
    private String rowChar;

    @Column(name = "col_num", nullable = false)
    private Integer colNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType type;

    private Boolean status = true;

    public Seat() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getRowChar() {
        return rowChar;
    }

    public void setRowChar(String rowChar) {
        this.rowChar = rowChar;
    }

    public Integer getColNum() {
        return colNum;
    }

    public void setColNum(Integer colNum) {
        this.colNum = colNum;
    }

    public SeatType getType() {
        return type;
    }

    public void setType(SeatType type) {
        this.type = type;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getSeatCode() {
        return rowChar + colNum;
    }
}
