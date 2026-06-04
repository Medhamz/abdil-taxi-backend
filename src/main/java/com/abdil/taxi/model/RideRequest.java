package com.abdil.taxi.model;

public class RideRequest {
    private Long userId;
    private String clientName;
    private String clientPhone;
    private String pickupAddress;
    private String destinationAddress;
    private Double distance;
    private String rideType = "STANDARD";
    private Boolean femaleOnly = false;
    private Boolean passByMosque = false;
    private String paymentMethod = "CASH";
    private String paymentReference;

    public RideRequest() {}

    public RideRequest(Long userId, String clientName, String clientPhone, String pickupAddress,
                       String destinationAddress, Double distance, String rideType, Boolean femaleOnly) {
        this.userId = userId;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.pickupAddress = pickupAddress;
        this.destinationAddress = destinationAddress;
        this.distance = distance;
        this.rideType = rideType;
        this.femaleOnly = femaleOnly;
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

    public String getRideType() { return rideType; }
    public void setRideType(String rideType) { this.rideType = rideType; }

    public Boolean getFemaleOnly() { return femaleOnly; }
    public void setFemaleOnly(Boolean femaleOnly) { this.femaleOnly = femaleOnly; }

    public Boolean getPassByMosque() { return passByMosque; }
    public void setPassByMosque(Boolean passByMosque) { this.passByMosque = passByMosque; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
}