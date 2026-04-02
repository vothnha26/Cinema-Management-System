package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.MembershipTier;
import java.math.BigDecimal;

public class CustomerResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private MembershipTier membershipTier;
    private BigDecimal totalSpending;
    private Integer points;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public MembershipTier getMembershipTier() { return membershipTier; }
    public void setMembershipTier(MembershipTier membershipTier) { this.membershipTier = membershipTier; }
    public BigDecimal getTotalSpending() { return totalSpending; }
    public void setTotalSpending(BigDecimal totalSpending) { this.totalSpending = totalSpending; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}
