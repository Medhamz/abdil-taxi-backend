package com.abdil.taxi.model;

public class PriceRequest {
    private String pickupAddress;
    private String destinationAddress;
    private double distance;
    private String rideType = "STANDARD"; // STANDARD ou VIP

    public PriceRequest() {}

    public PriceRequest(String pickupAddress, String destinationAddress, double distance, String rideType) {
        this.pickupAddress = pickupAddress;
        this.destinationAddress = destinationAddress;
        this.distance = distance;
        this.rideType = rideType;
    }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getRideType() { return rideType; }
    public void setRideType(String rideType) { this.rideType = rideType; }
}