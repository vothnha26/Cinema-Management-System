package com.example.cinema.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "branch_combos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"branch_id", "combo_id"})
})
public class BranchCombo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Giá bán riêng tại chi nhánh này

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0; // Tồn kho riêng tại chi nhánh này

    @Column(name = "is_active")
    private Boolean isActive = true;

    public BranchCombo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public Combo getCombo() { return combo; }
    public void setCombo(Combo combo) { this.combo = combo; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
