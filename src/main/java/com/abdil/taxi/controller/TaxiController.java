package com.abdil.taxi.controller;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.RideRepository;
import com.abdil.taxi.service.NotificationService;
import com.abdil.taxi.service.TaxiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/taxi")
@CrossOrigin(origins = "*")
public class TaxiController {

    @Autowired
    private TaxiService taxiService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("🚖 Abdil Taxi API is running!");
    }

    @PostMapping("/price")
    public ResponseEntity<PriceResponse> calculatePrice(@RequestBody PriceRequest request) {
        System.out.println("=== REQUÊTE /price reçue ===");
        System.out.println("pickupAddress: " + request.getPickupAddress());
        System.out.println("destinationAddress: " + request.getDestinationAddress());
        System.out.println("distance: " + request.getDistance());
        System.out.println("===========================");

        PriceResponse response = taxiService.calculatePrice(request.getDistance());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ride")
    public ResponseEntity<Ride> createRide(@RequestBody RideRequest request) {
        System.out.println("=== REQUÊTE /ride reçue ===");
        System.out.println("userId: " + request.getUserId());
        System.out.println("clientName: " + request.getClientName());
        System.out.println("clientPhone: " + request.getClientPhone());
        System.out.println("pickupAddress: " + request.getPickupAddress());
        System.out.println("destinationAddress: " + request.getDestinationAddress());
        System.out.println("distance: " + request.getDistance());
        System.out.println("=========================");

        Ride ride = taxiService.createRide(request);

        try {
            notificationService.sendNotificationToAllDrivers(
                    "🆕 Nouvelle course disponible",
                    ride.getPickupAddress() + " → " + ride.getDestinationAddress() + " | " + ride.getDistance() + " km | " + ride.getEstimatedPrice() + " FCFA"
            );
            System.out.println("📱 Notification envoyée aux chauffeurs");
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ride);
    }

    @GetMapping("/ride/{id}")
    public ResponseEntity<Ride> getRideStatus(@PathVariable Long id) {
        Ride ride = taxiService.getRideStatus(id);
        if (ride != null) {
            return ResponseEntity.ok(ride);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides() {
        List<Ride> rides = taxiService.getAllRides();
        System.out.println("📜 " + rides.size() + " courses trouvées");
        return ResponseEntity.ok(rides);
    }

    @PutMapping("/ride/{id}/status")
    public ResponseEntity<Ride> updateRideStatus(@PathVariable Long id, @RequestParam String status) {
        Ride ride = taxiService.updateRideStatus(id, status);
        if (ride != null) {
            return ResponseEntity.ok(ride);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}