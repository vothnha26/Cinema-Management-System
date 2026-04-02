package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.SeatType;
import java.math.BigDecimal;

public class SeatResponse {
    private Long id;
    private String seatCode;
    private SeatType seatType;
    private String rowChar;
    private Integer colNum;
    
    // Is this seat available for booking in a specific showtime?
    private boolean isAvailable; 
    
    // The price to book this seat for the current showtime
    private BigDecimal price;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }
    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }
    public String getRowChar() { return rowChar; }
    public void setRowChar(String rowChar) { this.rowChar = rowChar; }
    public Integer getColNum() { return colNum; }
    public void setColNum(Integer colNum) { this.colNum = colNum; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
