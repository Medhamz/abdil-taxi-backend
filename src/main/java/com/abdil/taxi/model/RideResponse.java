package com.abdil.taxi.model;

public class RideResponse {
    private Long id;
    private String clientName;
    private String clientPhone;
    private String pickupAddress;
    private String destinationAddress;
    private Double distance;
    private Double estimatedPrice;
    private String status;
    private String createdAt;

    // ✅ CHAMPS POUR LE CHAUFFEUR
    private Long driverId;
    private String driverName;

    // ✅ CHAMPS POUR LA PAUSE
    private Boolean driverIsOnPause;
    private String driverPauseReason;

    // ✅ CHAMP POUR LE MODE DE PAIEMENT (TRÈS IMPORTANT)
    private String paymentMethod;

    public RideResponse() {}

    public RideResponse(Long id, String clientName, String clientPhone, String pickupAddress,
                        String destinationAddress, Double distance, Double estimatedPrice,
                        String status, String createdAt) {
        this.id = id;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.pickupAddress = pickupAddress;
        this.destinationAddress = destinationAddress;
        this.distance = distance;
        this.estimatedPrice = estimatedPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters et Setters existants
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(Double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // ✅ GETTERS ET SETTERS POUR LE CHAUFFEUR
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    // ✅ GETTERS ET SETTERS POUR LA PAUSE
    public Boolean getDriverIsOnPause() { return driverIsOnPause; }
    public void setDriverIsOnPause(Boolean driverIsOnPause) { this.driverIsOnPause = driverIsOnPause; }

    public String getDriverPauseReason() { return driverPauseReason; }
    public void setDriverPauseReason(String driverPauseReason) { this.driverPauseReason = driverPauseReason; }

    // ✅ GETTER ET SETTER POUR LE MODE DE PAIEMENT
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}