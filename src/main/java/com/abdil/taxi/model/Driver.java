package com.abdil.taxi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String vehicleType;
    private String licensePlate;
    private String status;

    // Localisation
    private Double latitude = 0.0;
    private Double longitude = 0.0;

    // Notation
    private Double rating = 0.0;
    private Long ratingCount = 0L;

    // Champs pour la pause
    private Boolean isOnPause = false;
    private String pauseReason = null;
    private LocalDateTime pauseStartTime = null;

    // Mode femme uniquement
    private Boolean isFemaleOnly = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    // ==================== GETTERS ET SETTERS ====================

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getVehicleType() {
        return vehicleType;
    }
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude != null ? latitude : 0.0;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude != null ? longitude : 0.0;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRating() {
        return rating != null ? rating : 0.0;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getRatingCount() {
        return ratingCount != null ? ratingCount : 0L;
    }
    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsOnPause() {
        return isOnPause != null ? isOnPause : false;
    }
    public void setIsOnPause(Boolean isOnPause) {
        this.isOnPause = isOnPause;
    }

    public String getPauseReason() {
        return pauseReason;
    }
    public void setPauseReason(String pauseReason) {
        this.pauseReason = pauseReason;
    }

    public LocalDateTime getPauseStartTime() {
        return pauseStartTime;
    }
    public void setPauseStartTime(LocalDateTime pauseStartTime) {
        this.pauseStartTime = pauseStartTime;
    }

    public Boolean getIsFemaleOnly() {
        return isFemaleOnly != null ? isFemaleOnly : false;
    }
    public void setIsFemaleOnly(Boolean isFemaleOnly) {
        this.isFemaleOnly = isFemaleOnly;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    public boolean hasValidLocation() {
        return latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0;
    }

    public boolean isOnline() {
        return "ONLINE".equals(status) || "ON_TRIP".equals(status);
    }

    public boolean isOnTrip() {
        return "ON_TRIP".equals(status);
    }
}