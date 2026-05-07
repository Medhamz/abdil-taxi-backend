package com.abdil.taxi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_tokens")
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    private String deviceId;

    private String userType; // CLIENT, DRIVER

    private LocalDateTime createdAt = LocalDateTime.now();

    public UserToken() {}

    public UserToken(Long userId, String token, String deviceId, String userType) {
        this.userId = userId;
        this.token = token;
        this.deviceId = deviceId;
        this.userType = userType;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}