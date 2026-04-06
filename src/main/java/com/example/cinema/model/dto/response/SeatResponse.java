package com.example.cinema.model.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class SeatResponse {
    private Long id;
    private String rowChar;
    private Integer colNum;
    private String seatCode;
    private String seatTypeId;
    private String seatTypeName;
    private String seatType;
    private BigDecimal price;
    private boolean available;
    private List<String> priceBreakdown;

    public SeatResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRowChar() { return rowChar; }
    public void setRowChar(String rowChar) { this.rowChar = rowChar; }
    public Integer getColNum() { return colNum; }
    public void setColNum(Integer colNum) { this.colNum = colNum; }
    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }
    public String getSeatTypeId() { return seatTypeId; }
    public void setSeatTypeId(String seatTypeId) { this.seatTypeId = seatTypeId; }
    public String getSeatTypeName() { return seatTypeName; }
    public void setSeatTypeName(String seatTypeName) { this.seatTypeName = seatTypeName; }
    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public List<String> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(List<String> priceBreakdown) { this.priceBreakdown = priceBreakdown; }
}
