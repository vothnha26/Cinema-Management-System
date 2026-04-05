package com.example.cinema.model.entity;

import com.example.cinema.model.enums.PricingRuleType;
import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "pricing_rules")
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private PricingRuleType type; // ADDITIVE, PERCENTAGE

    private BigDecimal value; // Số tiền cộng thêm hoặc Tỷ lệ phần trăm (v.d: 1.2 cho +20%)

    private Integer priority; // Độ ưu tiên áp dụng

    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "pricing_rule_days", joinColumns = @JoinColumn(name = "rule_id"))
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> applicableDays;

    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private RoomType applicableRoomType;

    @Enumerated(EnumType.STRING)
    private SeatType applicableSeatType;

    private String applicableFormat; // 2D, 3D, IMAX

    private boolean isActive = true;

    public PricingRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PricingRuleType getType() { return type; }
    public void setType(PricingRuleType type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Set<DayOfWeek> getApplicableDays() { return applicableDays; }
    public void setApplicableDays(Set<DayOfWeek> applicableDays) { this.applicableDays = applicableDays; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public RoomType getApplicableRoomType() { return applicableRoomType; }
    public void setApplicableRoomType(RoomType applicableRoomType) { this.applicableRoomType = applicableRoomType; }

    public SeatType getApplicableSeatType() { return applicableSeatType; }
    public void setApplicableSeatType(SeatType applicableSeatType) { this.applicableSeatType = applicableSeatType; }

    public String getApplicableFormat() { return applicableFormat; }
    public void setApplicableFormat(String applicableFormat) { this.applicableFormat = applicableFormat; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
