package com.abdil.taxi.model;

public class PriceResponse {
    private boolean success;
    private String message;
    private double estimatedPrice;
    private double distance;
    private String duration;

    public PriceResponse() {}

    public PriceResponse(boolean success, String message, double estimatedPrice, double distance, String duration) {
        this.success = success;
        this.message = message;
        this.estimatedPrice = estimatedPrice;
        this.distance = distance;
        this.duration = duration;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}