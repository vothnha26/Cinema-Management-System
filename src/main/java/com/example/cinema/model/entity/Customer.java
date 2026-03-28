package com.example.cinema.model.entity;

import com.example.cinema.model.enums.MembershipTier;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_tier")
    private MembershipTier membershipTier = MembershipTier.STANDARD;

    @Column(name = "total_spending", precision = 15, scale = 2)
    private BigDecimal totalSpending = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer points = 0;

    public Customer() {}

    public Customer(Long id, User user, String fullName, String phone, MembershipTier membershipTier, BigDecimal totalSpending, Integer points) {
        this.id = id;
        this.user = user;
        this.fullName = fullName;
        this.phone = phone;
        this.membershipTier = membershipTier;
        this.totalSpending = totalSpending;
        this.points = points;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MembershipTier getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(MembershipTier membershipTier) {
        this.membershipTier = membershipTier;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(BigDecimal totalSpending) {
        this.totalSpending = totalSpending;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
