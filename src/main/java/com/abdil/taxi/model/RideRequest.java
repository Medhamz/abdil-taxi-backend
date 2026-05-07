package com.abdil.taxi.model;

public class RideRequest {
    private Long userId;
    private String clientName;
    private String clientPhone;
    private String pickupAddress;
    private String destinationAddress;
    private Double distance;

    public RideRequest() {}

    public RideRequest(Long userId, String clientName, String clientPhone, String pickupAddress, String destinationAddress, Double distance) {
        this.userId = userId;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.pickupAddress = pickupAddress;
        this.destinationAddress = destinationAddress;
        this.distance = distance;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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
}