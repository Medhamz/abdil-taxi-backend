package com.abdil.taxi.service;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaxiService {

    private static final double STANDARD_RATE_PER_KM = 150.0;
    private static final double VIP_RATE_PER_KM = 200.0;
    private static final double SPEED_KM_PER_HOUR = 30.0;

    @Autowired
    private RideRepository rideRepository;

    public PriceResponse calculatePrice(double distance, String rideType) {
        double durationHours = distance / SPEED_KM_PER_HOUR;
        int durationMinutes = (int) Math.ceil(durationHours * 60);
        String duration = durationMinutes + " min";

        double baseRate = "VIP".equals(rideType) ? VIP_RATE_PER_KM : STANDARD_RATE_PER_KM;
        double basePrice = distance * baseRate;
        double multiplier = calculateMultiplier();
        double estimatedPrice = basePrice * multiplier;
        String breakdown = getBreakdown(multiplier, basePrice, estimatedPrice, rideType, baseRate);

        return new PriceResponse(distance, duration, estimatedPrice, basePrice, multiplier, breakdown);
    }

    private double calculateMultiplier() {
        double multiplier = 1.0;
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int dayOfWeek = now.getDayOfWeek().getValue();
        int month = now.getMonthValue();

        if ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 20)) {
            multiplier *= 1.5;
        }
        if (hour >= 23 || hour <= 6) {
            multiplier *= 1.3;
        }
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            multiplier *= 1.2;
        }
        if (month == 7 || month == 8 || month == 12) {
            multiplier *= 1.25;
        }
        return multiplier;
    }

    private String getBreakdown(double multiplier, double basePrice, double finalPrice, String rideType, double baseRate) {
        double increase = finalPrice - basePrice;
        String typeLabel = "VIP".equals(rideType) ? "VIP (200 FCFA/km)" : "Standard (150 FCFA/km)";
        return String.format(
                "Type: %s\nPrix de base: %.0f FCFA\nMajoration: +%.0f FCFA (x%.1f)\nTotal: %.0f FCFA",
                typeLabel, basePrice, increase, multiplier, finalPrice
        );
    }

    public Ride createRide(RideRequest request) {
        Ride ride = new Ride();
        ride.setUserId(request.getUserId());
        ride.setClientName(request.getClientName());
        ride.setClientPhone(request.getClientPhone());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDestinationAddress(request.getDestinationAddress());
        ride.setDistance(request.getDistance());
        ride.setRideType(request.getRideType() != null ? request.getRideType() : "STANDARD");

        // ✅ AJOUT DU MODE DE PAIEMENT
        if (request.getPaymentMethod() != null) {
            ride.setPaymentMethod(request.getPaymentMethod());
        } else {
            ride.setPaymentMethod("CASH"); // Valeur par défaut
        }

        double baseRate = "VIP".equals(ride.getRideType()) ? VIP_RATE_PER_KM : STANDARD_RATE_PER_KM;
        double multiplier = calculateMultiplier();
        double basePrice = request.getDistance() * baseRate;
        double estimatedPrice = basePrice * multiplier;

        ride.setEstimatedPrice(estimatedPrice);
        ride.setStatus("PENDING");

        return rideRepository.save(ride);
    }

    public Ride getRideStatus(Long id) {
        return rideRepository.findById(id).orElse(null);
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    public Ride updateRideStatus(Long id, String status) {
        Ride ride = rideRepository.findById(id).orElse(null);
        if (ride != null) {
            ride.setStatus(status);
            if ("COMPLETED".equals(status)) {
                ride.setCompletedAt(LocalDateTime.now());
            }
            return rideRepository.save(ride);
        }
        return null;
    }
}