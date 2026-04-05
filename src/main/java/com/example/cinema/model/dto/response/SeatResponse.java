package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.SeatType;
import java.math.BigDecimal;
import java.util.List;

public class SeatResponse {
    private Long id;
    private String rowChar;
    private Integer colNum;
    private String seatCode;
    private SeatType seatType;
    private BigDecimal price;
    private boolean available;
    private List<String> priceBreakdown; // Danh sách tên các rule đã áp dụng

    public SeatResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRowChar() { return rowChar; }
    public void setRowChar(String rowChar) { this.rowChar = rowChar; }
    public Integer getColNum() { return colNum; }
    public void setColNum(Integer colNum) { this.colNum = colNum; }
    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }
    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public List<String> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(List<String> priceBreakdown) { this.priceBreakdown = priceBreakdown; }
}
