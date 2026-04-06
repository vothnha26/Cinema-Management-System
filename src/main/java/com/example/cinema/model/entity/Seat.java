package com.example.cinema.model.entity;

import com.example.cinema.model.entity.SeatType;

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

    @ManyToOne
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatType seatType;

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

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public void setType(String typeId) {
        if (typeId != null) {
            this.seatType = new SeatType(typeId, null);
        }
    }

    public String getType() {
        return seatType != null ? seatType.getId() : null;
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
