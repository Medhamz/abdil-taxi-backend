package com.abdil.taxi.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "licenses")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String licenseKey;

    @Column(nullable = false, length = 30)
    private String licenseType; // TRIAL, 1_YEAR, 2_YEARS, 3_YEARS, 4_YEARS, 5_YEARS, PERPETUAL

    private Integer durationDays; // -1 pour perpétuelle

    private Integer price;

    @Column(nullable = false, length = 20)
    private String appType; // CLIENT, DRIVER, BOTH

    private Long userId;

    private String userType; // CLIENT or DRIVER

    private String userEmail;

    private String userName;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, EXPIRED, REVOKED

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String createdBy; // ADMIN

    private LocalDateTime updatedAt;

    // Constructeurs
    public License() {}

    public License(String licenseKey, String licenseType, Integer durationDays, Integer price,
                   String appType, Long userId, String userType, String userEmail, String userName,
                   String status, LocalDateTime startDate, LocalDateTime endDate, String createdBy) {
        this.licenseKey = licenseKey;
        this.licenseType = licenseType;
        this.durationDays = durationDays;
        this.price = price;
        this.appType = appType;
        this.userId = userId;
        this.userType = userType;
        this.userEmail = userEmail;
        this.userName = userName;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }

    public String getLicenseType() { return licenseType; }
    public void setLicenseType(String licenseType) { this.licenseType = licenseType; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getAppType() { return appType; }
    public void setAppType(String appType) { this.appType = appType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}