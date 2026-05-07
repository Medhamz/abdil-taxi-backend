package com.abdil.taxi.controller;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.*;
import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/driver")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<DriverAuthResponse> register(@RequestBody DriverRegisterRequest request) {
        try {
            System.out.println("=== INSCRIPTION CHAUFFEUR ===");
            System.out.println("Nom: " + request.getFullName());
            System.out.println("Email: " + request.getEmail());
            System.out.println("Téléphone: " + request.getPhone());
            System.out.println("Véhicule: " + request.getVehicleType());
            System.out.println("Plaque: " + request.getLicensePlate());

            if (driverRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(new DriverAuthResponse(null, "Email déjà utilisé", false, null));
            }

            if (driverRepository.existsByPhone(request.getPhone())) {
                return ResponseEntity.badRequest().body(new DriverAuthResponse(null, "Téléphone déjà utilisé", false, null));
            }

            Driver driver = new Driver();
            driver.setFullName(request.getFullName());
            driver.setEmail(request.getEmail());
            driver.setPhone(request.getPhone());
            driver.setPassword(request.getPassword());
            driver.setVehicleType(request.getVehicleType());
            driver.setLicensePlate(request.getLicensePlate());
            driver.setStatus("OFFLINE");

            Driver savedDriver = driverRepository.save(driver);
            System.out.println("✅ Chauffeur créé avec ID: " + savedDriver.getId());

            return ResponseEntity.ok(new DriverAuthResponse(null, "Inscription réussie", true, savedDriver));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DriverAuthResponse(null, "Erreur: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<DriverAuthResponse> login(@RequestBody DriverAuthRequest request) {
        try {
            System.out.println("=== TENTATIVE DE CONNEXION CHAUFFEUR ===");
            System.out.println("Email: " + request.getEmail());

            Driver driver = driverRepository.findByEmail(request.getEmail()).orElse(null);
            if (driver == null) {
                return ResponseEntity.badRequest().body(new DriverAuthResponse(null, "Email ou mot de passe incorrect", false, null));
            }

            if (!driver.getPassword().equals(request.getPassword())) {
                return ResponseEntity.badRequest().body(new DriverAuthResponse(null, "Email ou mot de passe incorrect", false, null));
            }

            System.out.println("✅ Connexion réussie pour: " + driver.getEmail());

            return ResponseEntity.ok(new DriverAuthResponse(null, "Connexion réussie", true, driver));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DriverAuthResponse(null, "Erreur: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/location")
    public ResponseEntity<Driver> updateLocation(@RequestParam Long driverId, @RequestParam double lat, @RequestParam double lng) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }
        driver.setLatitude(lat);
        driver.setLongitude(lng);
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @PostMapping("/status")
    public ResponseEntity<Driver> updateStatus(@RequestParam Long driverId, @RequestParam String status) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }
        driver.setStatus(status);
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @GetMapping("/rides/pending")
    public ResponseEntity<List<Ride>> getPendingRides() {
        List<Ride> pendingRides = rideRepository.findByStatus("PENDING");
        return ResponseEntity.ok(pendingRides);
    }

    @PutMapping("/ride/{rideId}/accept")
    public ResponseEntity<Ride> acceptRide(@PathVariable Long rideId, @RequestParam Long driverId) {
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }

        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        ride.setStatus("ACCEPTED");
        ride.setDriverName(driver.getFullName());
        Ride savedRide = rideRepository.save(ride);

        // Envoyer notification au client
        try {
            notificationService.sendNotificationToClient(
                    ride.getUserId(),
                    "✅ Course acceptée",
                    "Votre chauffeur " + driver.getFullName() + " a accepté votre course et arrive bientôt!"
            );
            System.out.println("📱 Notification envoyée au client " + ride.getUserId());
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification client: " + e.getMessage());
        }

        return ResponseEntity.ok(savedRide);
    }

    @PutMapping("/ride/{rideId}/status")
    public ResponseEntity<Ride> updateRideStatus(@PathVariable Long rideId, @RequestParam String status) {
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }
        ride.setStatus(status);
        return ResponseEntity.ok(rideRepository.save(ride));
    }

    @GetMapping("/driver/{driverId}/rides")
    public ResponseEntity<List<Ride>> getDriverRides(@PathVariable Long driverId) {
        List<Ride> rides = rideRepository.findAll().stream()
                .filter(r -> r.getDriverName() != null)
                .toList();
        return ResponseEntity.ok(rides);
    }
}