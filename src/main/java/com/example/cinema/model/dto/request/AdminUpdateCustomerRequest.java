package com.example.cinema.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AdminUpdateCustomerRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String phone;
    private String email;

    @NotNull(message = "Hạng thành viên không được để trống")
    private String membershipLevel;

    @NotNull(message = "Điểm thưởng không được để trống")
    private Integer points;

    @NotNull(message = "Tổng chi tiêu không được để trống")
    private BigDecimal totalSpending;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMembershipLevel() { return membershipLevel; }
    public void setMembershipLevel(String membershipLevel) { this.membershipLevel = membershipLevel; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public BigDecimal getTotalSpending() { return totalSpending; }
    public void setTotalSpending(BigDecimal totalSpending) { this.totalSpending = totalSpending; }
}
