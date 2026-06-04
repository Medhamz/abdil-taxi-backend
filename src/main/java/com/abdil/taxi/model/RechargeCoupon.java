package com.abdil.taxi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recharge_coupons")
public class RechargeCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private Double amount;
    private String status; // ACTIVE, USED, EXPIRED
    private String createdBy;

    @Column(name = "used_by_user_id")
    private Long usedByUserId;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public RechargeCoupon() {}

    // Getters
    public Long getId() { return id; }
    public String getCode() { return code; }
    public Double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public Long getUsedByUserId() { return usedByUserId; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUsedByUserId(Long usedByUserId) { this.usedByUserId = usedByUserId; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}