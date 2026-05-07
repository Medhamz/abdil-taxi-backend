package com.abdil.taxi.model;

public class PriceRequest {
    private String pickupAddress;
    private String destinationAddress;
    private double distance;

    public PriceRequest() {}

    public PriceRequest(String pickupAddress, String destinationAddress, double distance) {
        this.pickupAddress = pickupAddress;
        this.destinationAddress = destinationAddress;
        this.distance = distance;
    }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
}