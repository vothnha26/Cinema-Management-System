package com.example.cinema.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "membership_levels")
public class MembershipLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "min_spending", precision = 15, scale = 2)
    private BigDecimal minSpending;

    @Column(nullable = false)
    private Integer priority;

    @Column(name = "min_points", nullable = false)
    private Integer minPoints = 0;

    public MembershipLevel() {
    }

    public MembershipLevel(String name, BigDecimal minSpending, Integer priority) {
        this.name = name;
        this.minSpending = minSpending;
        this.priority = priority;
        this.minPoints = 0;
    }

    public MembershipLevel(String name, BigDecimal minSpending, Integer priority, Integer minPoints) {
        this.name = name;
        this.minSpending = minSpending;
        this.priority = priority;
        this.minPoints = minPoints;
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

    public BigDecimal getMinSpending() {
        return minSpending;
    }

    public void setMinSpending(BigDecimal minSpending) {
        this.minSpending = minSpending;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getMinPoints() {
        return minPoints;
    }

    public void setMinPoints(Integer minPoints) {
        this.minPoints = minPoints;
    }
}
