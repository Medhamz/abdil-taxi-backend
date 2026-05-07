package com.abdil.taxi.service;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaxiService {

    private static final double PRICE_PER_KM = 300.0;

    @Autowired
    private RideRepository rideRepository;

    public PriceResponse calculatePrice(Double distance) {
        try {
            if (distance == null || distance <= 0) {
                PriceResponse response = new PriceResponse();
                response.setSuccess(false);
                response.setMessage("Distance invalide");
                return response;
            }

            double estimatedPrice = distance * PRICE_PER_KM;
            String duration = String.format("%.0f", distance * 2) + " minutes";

            PriceResponse response = new PriceResponse();
            response.setSuccess(true);
            response.setEstimatedPrice(estimatedPrice);
            response.setDistance(distance);
            response.setDuration(duration);
            response.setMessage("Prix calculé avec succès");

            System.out.println("✅ Prix calculé: " + estimatedPrice + " FCFA");
            return response;
        } catch (Exception e) {
            PriceResponse response = new PriceResponse();
            response.setSuccess(false);
            response.setMessage("Erreur de calcul: " + e.getMessage());
            return response;
        }
    }

    public Ride createRide(RideRequest request) {
        System.out.println("📝 Création d'une course pour: " + request.getClientName());

        double estimatedPrice = request.getDistance() * PRICE_PER_KM;

        Ride ride = new Ride();
        ride.setUserId(request.getUserId());
        ride.setClientName(request.getClientName());
        ride.setClientPhone(request.getClientPhone());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDestinationAddress(request.getDestinationAddress());
        ride.setDistance(request.getDistance());
        ride.setEstimatedPrice(estimatedPrice);
        ride.setStatus("PENDING");
        ride.setCreatedAt(LocalDateTime.now());

        Ride savedRide = rideRepository.save(ride);
        System.out.println("✅ Course créée avec ID: " + savedRide.getId() + " - Prix: " + estimatedPrice + " FCFA");

        return savedRide;
    }

    public Ride getRideStatus(Long rideId) {
        return rideRepository.findById(rideId).orElse(null);
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    public Ride updateRideStatus(Long rideId, String status) {
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) return null;

        ride.setStatus(status);
        if ("COMPLETED".equals(status)) {
            ride.setCompletedAt(LocalDateTime.now());
        }

        return rideRepository.save(ride);
    }
}