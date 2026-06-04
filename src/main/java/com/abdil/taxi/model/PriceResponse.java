package com.abdil.taxi.model;

public class PriceResponse {
    private double distance;
    private String duration;
    private double estimatedPrice;
    private double basePrice;
    private double multiplier;
    private String breakdown;

    public PriceResponse() {}

    public PriceResponse(double distance, String duration, double estimatedPrice, double basePrice, double multiplier, String breakdown) {
        this.distance = distance;
        this.duration = duration;
        this.estimatedPrice = estimatedPrice;
        this.basePrice = basePrice;
        this.multiplier = multiplier;
        this.breakdown = breakdown;
    }

    // Getters et Setters
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }

    public String getBreakdown() { return breakdown; }
    public void setBreakdown(String breakdown) { this.breakdown = breakdown; }
}