package com.abdil.taxi.controller;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.DriverRepository;
import com.abdil.taxi.repository.RideRepository;
import com.abdil.taxi.service.NotificationService;
import com.abdil.taxi.service.TaxiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private DriverRepository driverRepository;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("🚖 Abdil Taxi API is running!");
    }

    @PostMapping("/price")
    public ResponseEntity<PriceResponse> calculatePrice(@RequestBody PriceRequest request) {
        String rideType = request.getRideType() != null ? request.getRideType() : "STANDARD";
        PriceResponse response = taxiService.calculatePrice(request.getDistance(), rideType);
        System.out.println("=== TARIFICATION DYNAMIQUE ===");
        System.out.println("Type: " + rideType);
        System.out.println("Distance: " + request.getDistance() + " km");
        System.out.println("Prix final: " + response.getEstimatedPrice() + " FCFA");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ride")
    public ResponseEntity<Ride> createRide(@RequestBody RideRequest request) {
        Ride ride = taxiService.createRide(request);
        System.out.println("=== COURSE CRÉÉE AVEC TARIFICATION DYNAMIQUE ===");
        System.out.println("Course ID: " + ride.getId());
        System.out.println("Prix final: " + ride.getEstimatedPrice() + " FCFA");
        System.out.println("Mode de paiement: " + ride.getPaymentMethod());

        try {
            // ✅ Gestion du mode "femme uniquement"
            if (request.getFemaleOnly() != null && request.getFemaleOnly()) {
                List<Driver> femaleDrivers = driverRepository.findByStatusAndIsFemaleOnly("ONLINE", true);
                if (femaleDrivers.isEmpty()) {
                    System.out.println("⚠️ Aucune chauffeur femme disponible pour course femme uniquement");
                } else {
                    for (Driver driver : femaleDrivers) {
                        notificationService.sendNotificationToDriver(
                                driver.getId(),
                                "🆕 Nouvelle course (femme uniquement)",
                                ride.getPickupAddress() + " → " + ride.getDestinationAddress()
                        );
                    }
                }
            } else {
                notificationService.sendNotificationToAllDrivers(
                        "🆕 Nouvelle course disponible",
                        ride.getPickupAddress() + " → " + ride.getDestinationAddress()
                );
            }
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

    @GetMapping("/client/active/{clientId}")
    public ResponseEntity<RideResponse> getActiveRideForClient(@PathVariable Long clientId) {
        System.out.println("=== getActiveRideForClient called for clientId: " + clientId);

        List<Ride> allRides = taxiService.getAllRides();

        List<Ride> clientActiveRides = allRides.stream()
                .filter(r -> r.getUserId() != null && r.getUserId().equals(clientId))
                .filter(r -> r.getStatus().equals("ACCEPTED") || r.getStatus().equals("STARTED"))
                .sorted(Comparator.comparing(Ride::getCreatedAt).reversed())
                .toList();

        if (clientActiveRides.isEmpty()) {
            System.out.println("❌ Aucune course active pour client " + clientId);
            return ResponseEntity.notFound().build();
        }

        Ride foundRide = clientActiveRides.get(0);
        System.out.println("✅ Course active trouvée: ID=" + foundRide.getId() +
                ", driverId=" + foundRide.getDriverId() +
                ", driverName=" + foundRide.getDriverName() +
                ", status=" + foundRide.getStatus());
        System.out.println("💰 Mode de paiement de la course: " + foundRide.getPaymentMethod());

        // Récupérer les informations de pause du chauffeur
        Boolean isOnPause = false;
        String pauseReason = null;

        if (foundRide.getDriverId() != null) {
            Driver driver = driverRepository.findById(foundRide.getDriverId()).orElse(null);
            if (driver != null) {
                isOnPause = driver.getIsOnPause() != null && driver.getIsOnPause();
                pauseReason = driver.getPauseReason();
            }
        }

        // ✅ Créer la réponse avec toutes les informations
        RideResponse response = new RideResponse(
                foundRide.getId(),
                foundRide.getClientName(),
                foundRide.getClientPhone(),
                foundRide.getPickupAddress(),
                foundRide.getDestinationAddress(),
                foundRide.getDistance(),
                foundRide.getEstimatedPrice(),
                foundRide.getStatus(),
                foundRide.getCreatedAt().toString()
        );

        // ✅ AJOUTER driverId ET driverName
        response.setDriverId(foundRide.getDriverId());
        response.setDriverName(foundRide.getDriverName());
        response.setDriverIsOnPause(isOnPause);
        response.setDriverPauseReason(pauseReason);

        // ✅ AJOUTER paymentMethod (TRÈS IMPORTANT POUR LE PAIEMENT PAR LIEN)
        response.setPaymentMethod(foundRide.getPaymentMethod());

        System.out.println("📤 Réponse envoyée: driverId=" + response.getDriverId() +
                ", driverName=" + response.getDriverName() +
                ", isOnPause=" + isOnPause +
                ", paymentMethod=" + response.getPaymentMethod());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}/rides")
    public ResponseEntity<List<Ride>> getClientRides(@PathVariable Long clientId) {
        System.out.println("=== HISTORIQUE CLIENT ID: " + clientId);

        List<Ride> clientRides = taxiService.getAllRides().stream()
                .filter(r -> r.getUserId() != null && r.getUserId().equals(clientId))
                .filter(r -> !r.getStatus().equals("CANCELLED"))
                .sorted(Comparator.comparing(Ride::getCreatedAt).reversed())
                .collect(Collectors.toList());

        System.out.println("📜 " + clientRides.size() + " courses trouvées (non annulées)");
        return ResponseEntity.ok(clientRides);
    }

    @DeleteMapping("/client/ride/{rideId}")
    public ResponseEntity<Void> deleteClientRide(@PathVariable Long rideId) {
        System.out.println("=== SUPPRESSION COURSE ID: " + rideId);

        if (rideRepository.existsById(rideId)) {
            rideRepository.deleteById(rideId);
            System.out.println("✅ Course supprimée");
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/client/rides/batch")
    public ResponseEntity<Void> deleteMultipleClientRides(@RequestBody List<Long> rideIds) {
        System.out.println("=== SUPPRESSION MULTIPLE: " + rideIds.size() + " courses");
        rideRepository.deleteAllById(rideIds);
        System.out.println("✅ Courses supprimées");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ride/{rideId}/cancel")
    public ResponseEntity<?> cancelRideByClient(@PathVariable Long rideId,
                                                @RequestParam Long clientId,
                                                @RequestParam(required = false) String reason) {
        System.out.println("=== ANNULATION COURSE PAR CLIENT ===");
        System.out.println("rideId: " + rideId);
        System.out.println("clientId: " + clientId);
        System.out.println("Raison: " + reason);

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }

        if (ride.getUserId() == null || !ride.getUserId().equals(clientId)) {
            return ResponseEntity.badRequest().body("Cette course ne vous appartient pas");
        }

        if (!ride.getStatus().equals("PENDING") && !ride.getStatus().equals("ACCEPTED")) {
            return ResponseEntity.badRequest().body("Impossible d'annuler cette course");
        }

        ride.setStatus("CANCELLED");
        ride.setCancellationReason(reason != null ? reason : "Annulé par le client");

        Ride cancelledRide = rideRepository.save(ride);
        System.out.println("✅ Course annulée par client: ID=" + cancelledRide.getId());

        if (ride.getDriverId() != null) {
            try {
                notificationService.sendNotificationToDriver(
                        ride.getDriverId(),
                        "❌ Course annulée",
                        "Le client a annulé la course. Raison: " + (reason != null ? reason : "Non spécifiée")
                );
            } catch (Exception e) {
                System.err.println("❌ Erreur envoi notification: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(cancelledRide);
    }
}